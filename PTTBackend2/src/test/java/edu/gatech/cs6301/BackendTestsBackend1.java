package edu.gatech.cs6301;

import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.skyscreamer.jsonassert.JSONAssert;

public class BackendTestsBackend1 {

    private String baseUrl = "http://localhost:8080/ptt";
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpclient;
    private boolean setupdone;
    private String BAD_USER_ID = "BAD_USER_ID";
    private String resGlobal;

    @Before
    public void runBefore() {
        if (!setupdone) {
            System.out.println("*** SETTING UP TESTS ***");
            // Increase max total connection to 100
            cm.setMaxTotal(100);
            // Increase default max connection per route to 20
            cm.setDefaultMaxPerRoute(10);
            // Increase max connections for localhost:80 to 50
            HttpHost localhost = new HttpHost("locahost", 8080);
            cm.setMaxPerRoute(new HttpRoute(localhost), 10);
            httpclient = HttpClients.custom().setConnectionManager(cm).build();
            setupdone = true;
        }
        System.out.println("*** STARTING TEST ***");
    }

    @After
    public void runAfter() {
        System.out.println("*** ENDING TEST ***");
    }

    // *** YOU SHOULD NOT NEED TO CHANGE ANYTHING ABOVE THIS LINE ***

    /*
     * Tests for POST /users
     */

    
    @Test
    public void createUserTest() throws Exception {
        try {
            deleteUsers();
            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createUserWithIdTest() throws Exception {
        try {
            deleteUsers();
            String input_id = "IGNORE_ID";
            CloseableHttpResponse response =
                    createUserWithID(input_id,"John", "Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);
            Assert.assertNotEquals(id, input_id);

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }
    

    /*
     * For this test, we are trying to ensure that the server only uses the userId as the primary key.
     * So, we send two createUser() requests to the server with identical bodies and compare the userIds
     * returned by the server to ensure they are unique.
     */
    @Test
    public void createConflictingUsersTest() throws Exception {
        try {
            deleteUsers();
            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Creating second user with identical body

            CloseableHttpResponse response2 =
                    createUserWithID(id, "John", "Doe", "john@doe.org");

            int status2 = response2.getStatusLine().getStatusCode();
            Assert.assertEquals(409, status2);

            deleteUser(id);
            response2.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createUsersBadRequest() throws Exception {
        try {

            // Creating a User with FirstName missing

            CloseableHttpResponse response =
                    createUserWithMissingFirstName("Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);
            response.close();

            // Create a user with LastName missing

            response = createUserWithMissingLastName("John", "john@doe.org");
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);
            response.close();

            // Create a user with Email missing

            response = createUserWithMissingEmail("John", "Doe");
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);
            response.close();

        } finally {
            httpclient.close();
        }
    }

    /*
     * Tests for GET /users
     */

    @Test
    public void getAllUsersEmptySetTest() throws Exception {
        try {
            deleteUsers();
            CloseableHttpResponse response = getAllUsers();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());

            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllUsersTest() throws Exception {
        try {
            deleteUsers();
            // Adding two users to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id1 = getIdFromResponse(response);
            String expectedJson1 = "{\"id\":" + id1 + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("James", "Doe", "james@doe.org");
            String id2 = getIdFromResponse(response);
            String expectedJson2 = "{\"id\":" + id2 + ",\"firstName\":\"James\",\"lastName\":\"Doe\",\"email\":\"james@doe.org\"}";
            response.close();


            // Perform GET /users and ensure that both users are returned correctly.

            response = getAllUsers();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[" + expectedJson1 + ", " + expectedJson2 + "]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(id1);
            deleteUser(id2);
            response.close();
        } finally {
            httpclient.close();
        }
    }


    /*
     * Tests for GET /users/{userId}
     */

    @Test
    public void getUserByIdTest() throws Exception {
        try {

            // Adding a user to the PTT
            deleteUsers();

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            // Perform GET /users/{userId} and ensure that the user is returned correctly.

            response = getUserById(id);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserByIdWithInvalidIdTest() throws Exception {
        try {

            // Adding a user to the PTT
            deleteUsers();

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();


            // Perform GET /users/{userId} and ensure that the user is returned correctly.

            response = getUserById(id + id + "12345");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 404);

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserByIdWithBadUserIdTest() throws Exception {
        try {
            deleteUsers();
            // Adding a user to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();


            // Perform GET /users/{userId} and ensure that the user is returned correctly.

            response = getUserById(BAD_USER_ID);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    /*
     * Tests for PUT /users/{userId}
     */

    @Test
    public void updateUserTest() throws Exception {
        try {

            // Adding a user to the PTT
            deleteUsers();

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            // Perform PUT /users/{userId} and ensure that the user is accurately updated.

            response = updateUser(id, "Tom", "James", "tom@james.org");
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"Tom\",\"lastName\":\"James\",\"email\":\"john@doe.org\"}";

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserWithInvalidUserIdTest() throws Exception {
        try {
            deleteUsers();

            // Adding a user to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            // Perform PUT /users/{userId} with Invalid UserId and ensure that the request is cleanly handled.

            response = updateUser(id + id + "12345", "Tom", "James", "tom@james.org");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 404);

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserWithBadUserIdTest() throws Exception {
        try {
            deleteUsers();

            // Adding a user to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            // Perform PUT /users/{userId} with Invalid UserId and ensure that the request is cleanly handled.

            response = updateUser(BAD_USER_ID, "Tom", "James", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);

            deleteUser(id);
            response.close();
        } finally {
            httpclient.close();
        }
    }


    /*
     * Tests for DELETE /users/{userId}
     */

    @Test
    public void deleteUserTest() throws Exception {
        try {
            deleteUsers();

            // Adding a user to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();


            // Perform DELETE /users/{userId} and ensure that the user body returned is as expected.

            response = deleteUser(id);
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());


            // Check using GET /user/{userId} to make sure user does not exist

            response = getUserById(id);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 404);

            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserWithInvalidIdTest() throws Exception {
        try {

            // Adding a user to the PTT
            deleteUsers();

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            /*
             * Perform DELETE /users/{userId} with Invalid UserId and ensure that the server handles it cleanly and
             * returns back a 404.
             */

            response = deleteUser(id + id + "12345");
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 404);

            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserWithBadUserIdTest() throws Exception {
        try {
            deleteUsers();

            // Adding a user to the PTT

            CloseableHttpResponse response =
                    createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            /*
             * Perform DELETE /users/{userId} with an Empty UserId ("") to ensure that the server handles it cleanly and
             * returns back a 400.
             */

            response = deleteUser(BAD_USER_ID);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(status, 400);

            response.close();
        } finally {
            httpclient.close();
        }
    }

    /*
     * Tests for GET /users/{userId}/projects
     */

    
    @Test
    public void getProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = getIdFromResponse(response);
            response.close();

            response = createProject("pj1", userid);
            System.out.println(response);
            String projectid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = getAllProjects(userid);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[{\"id\":" + projectid + ",\"projectname\":\"pj1\",\"userId\":" + userid + "}]";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(userid);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectBadRequestTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = getIdFromResponse(response);
            response.close();

            response = createProject("pj1", userid);
            // String projectid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            userid = "abc";
            response = getAllProjects(userid);

            status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectUserNotFoundTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = getIdFromResponse(response);
            response.close();

            response = createProject("pj1", userid);
            // String projectid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            userid = userid + "1";
            response = getAllProjects(userid);

            status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

     /*
      * Tests for POST /users/{userId}/projects
      */

    @Test
    public void createProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = createProject("pj1", userid);
            String projectid = getIdFromResponse(response);

            status = response.getStatusLine().getStatusCode();
            if (status != 201) {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }

            System.out.println("*** String response " + resGlobal + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":" + projectid + ",\"projectname\":\"pj1\",\"userId\":" + userid + "}";
            JSONAssert.assertEquals(expectedJson,resGlobal, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(userid);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createProjectBadRequestTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = "1234abc";
            String userid1 = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

           // Wrong userId
           response = createProject("pj1", userid);
           // String projectid = getIdFromResponse(response);

           status = response.getStatusLine().getStatusCode();
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }

            // Missing body variables
            response = createProject("", userid1);
            // String projectid = getIdFromResponse(response);

            status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                System.out.println("*** Correct! Expected response status: 400 since projectname was not sent to the server.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());

            deleteUser(userid1);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createProjectConflictTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = createProject("pj1", userid);
            // String projectid = getIdFromResponse(response);
            response.close();

            response = createProject("pj1", userid);

            status = response.getStatusLine().getStatusCode();
            if (status == 409) {
                System.out.println("*** Correct! Expected response status: 409 since projectname already exists.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());

            deleteUser(userid);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectByIdTest() throws Exception {
        deleteUsers();
        httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
                String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = getProject(id, userId);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson =
                    "{\"id\":" + id + ",\"projectname\":\"PTT Test case\",\"userId\":" + userId + "}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getInvalidProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject("PTT Test case 2", userId);
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "xyz" + id1 + id2; // making sure the ID is not present

            response = getProject(userId, missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject("PTT Test case 2", userId);
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = id1 + id2;

            response = getProject(userId, missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // // Test cases for /users/{userId}/projects/{projectId} PUT
    @Test
    public void updateProjectTest() throws Exception {
        deleteUsers();
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
            String id = getIdFromResponse(response);
            response.close();

            response = updateProject(userId, id,"PTT Test");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"PTT Test\",\"userId\":" + userId + "}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateInvalidProjectTest() throws Exception {
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
            String id = getIdFromResponse(response);
            response.close();

            String invalidId = "abc" + id;

            response = updateProject(invalidId, userId, "PTT Test");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateMissingProjectTest() throws Exception {
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("PTT Test case", userId);
            String id = getIdFromResponse(response);
            response.close();

            String invalidId = id + "00";

            response = updateProject(invalidId, userId, "PTT Test");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

     // Test cases for /users/{userId}/projects/{projectId} DELETE
     @Test
     public void deleteProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();
         CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
         String userId = getIdFromResponse(response);
         response.close();
         String expectedJson = null;

         try {
             response = createProject("PTT Test case", userId);
             String deleteid = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject(userId, deleteid);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             expectedJson = "{\"id\":" + deleteid + ",\"projectname\":\"PTT Test case\",\"userId\":" + userId +
                     "}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);

            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllProjects(userId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteMissingProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();
         CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
         String userId = getIdFromResponse(response);
         response.close();

         try {
             response = createProject("PTT Test case", userId);
             String id1 = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String id2 = getIdFromResponse(response);
             response.close();

             String missingId = id1 + id2; // making sure the ID is not present

             response = deleteProject(userId, missingId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteInvalidProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();
         CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
         String userId = getIdFromResponse(response);
         response.close();

         try {
             response = createProject("PTT Test case", userId);
             String id1 = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String id2 = getIdFromResponse(response);
             response.close();

             String missingId = "xyz" + id1 + id2; // making sure the ID is invalid

             response = deleteProject(userId, missingId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             EntityUtils.consume(response.getEntity());
             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void createPomodoroSessionTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
        String userId = getIdFromResponse(response);
        response.close();

        response = createProject("PTT Test case 2", userId);
        String projectId = getIdFromResponse(response);
        response.close();

        try {

            response = createPomodoroSession(userId, projectId);
            int status = response.getStatusLine().getStatusCode();

            //returns 201
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }

            String strResponse = EntityUtils.toString(entity);
            String id = getIdFromStringResponse(strResponse);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            String expectedJson = "{\"id\":" + id + ",\"startTime\": \"2019-02-18T20:00Z\"," +
                    "\"endTime\": \"2019-02-18T20:00Z\",\"counter\": 0}";

            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSession() throws Exception {
        try {
            deleteUsers();
            httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String id = getIdFromResponse(response);
            String sessionId = id;
            response.close();

            String startTime = "2019-01-18T20:00Z";
            String endTime = "2019-01-18T22:00Z";
            int counter = 1;
            response = updatePomodoro(userId, projectId, sessionId, startTime, endTime, counter);

            int status = response.getStatusLine().getStatusCode();

            //returns 200
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }

            String strResponse = EntityUtils.toString(entity);
            id = getIdFromStringResponse(strResponse);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            String expectedJson = "{\"id\":" + id + ",\"startTime\": \"" + startTime +
                    "\",\"endTime\":\"" + endTime + "\",\"counter\":" + counter + "}";

            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

     @Test
     public void getReports() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);;
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             //create pomodoro
             response = createPomodoroSession(userId, projectId);
             int status = response.getStatusLine().getStatusCode();
             if (status != 201){
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             String id1 = getIdFromResponse(response);
             response.close();

             response = getSessionReports(userId, projectId);
             status = response.getStatusLine().getStatusCode();

             //returns 201
             HttpEntity entity;
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }

             String strResponse = EntityUtils.toString(entity);
             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
             String expectedJson = "{\"sessions\":[{\"startingTime\":\"2019-02-18T20:00Z\",\"endingTime\":\"2019-02-18T20:00Z\"," +
                     "\"hoursWorked\": 0}],\"completedPomodoros\":0,\"totalHoursWorkedOnProject\":0}";

             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());

         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createPomodoroSessionWithInvalidUserIdTest() throws Exception {
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             response = createPomodoroSession(userId + "1234", projectId);

             HttpEntity entity;
             int status = response.getStatusLine().getStatusCode();

             if (status == 404) {
                 entity = response.getEntity();
                 EntityUtils.consume(entity);
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }

             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createPomodoroSessionBadRequestTest() throws Exception {
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             response = createPomodoroWithInvalidInputs(userId, projectId,"");

             HttpEntity entity;
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createPomodoroSessionWithInvalidProjectIdTest() throws Exception {
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             response = createPomodoroWithInvalidInputs(userId, projectId + projectId + "abc","2019-02-18T20:00Z");

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void updatePomodoroSessionWithBadUserId() throws Exception {
        httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String sessionId = getIdFromResponse(response);
            response.close();

            String badUserId = "BAD_USER_ID";
            response = updatePomodoro(badUserId, projectId, sessionId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSessionWithBadProjectId() throws Exception {
        httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String sessionId = getIdFromResponse(response);
            response.close();

            String badProjectId = "BAD_PROJECT_ID";

            response = updatePomodoro(userId, badProjectId, sessionId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSessionWithBadSessionId() throws Exception {
        httpclient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            response.close();

            String badSessionId = "BAD_SESSION_ID";
            response = updatePomodoro(userId, projectId, badSessionId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSessionWithInvalidUserId() throws Exception {
        httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String sessionId = getIdFromResponse(response);
            response.close();

            response = updatePomodoro(userId + userId + "1234", projectId, sessionId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSessionWithInvalidProjectId() throws Exception {
        httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String sessionId = getIdFromResponse(response);
            response.close();

            response = updatePomodoro(userId, projectId + projectId + "1234", sessionId,
                    "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updatePomodoroSessionWithInvalidSessionId() throws Exception {
        httpclient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
            String userId = getIdFromResponse(response);;
            response.close();

            response = createProject("PTT Test case 2", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createPomodoroSession(userId, projectId);
            String sessionId = getIdFromResponse(response);
            response.close();

            response = updatePomodoro(userId, projectId, sessionId + sessionId + "1234",
                    "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

     @Test
     public void getReportWithInvalidProjectId() throws Exception {
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);;
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             //create pomodoro
             response = createPomodoroSession(userId, projectId);
             response.close();

             // Get report
             response = getSessionReports(userId, projectId + projectId + "1234");
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void getReportWithInvalidUserIdTest() throws Exception {
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("James", "Doe", "james@gatech.edu");
             String userId = getIdFromResponse(response);
             response.close();

             response = createProject("PTT Test case 2", userId);
             String projectId = getIdFromResponse(response);
             response.close();

             response = getSessionReports(userId + userId + "1234", projectId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             deleteUser(userId);
             response.close();
         } finally {
             httpclient.close();
         }
     }


    // Request functions to create and send the HTTP Requests to the server

    private void deleteUsers() throws IOException {
        // Get all users
        CloseableHttpResponse response = getAllUsers();
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity;
        String strResponse;
        if (status == 200) {
            entity = response.getEntity();
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
        strResponse = EntityUtils.toString(entity);
        EntityUtils.consume(response.getEntity());
        response.close();

        try {
            // Get Ids from the response
            JSONArray jsonUsers = new JSONArray(strResponse);

            // For each item in array, parse the id and issue delete
            for (int i = 0; i < jsonUsers.length(); i++) {
                long deleteID = getIdFromJSONObject(jsonUsers.getJSONObject(i));
                response = deleteUser(Long.toString(deleteID));
                EntityUtils.consume(response.getEntity());
                response.close();
            }
        } catch (JSONException je){
            throw new ClientProtocolException("JSON PROBLEM: " + je);
        }

    }

    private CloseableHttpResponse getAllUsers() throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUser(String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUserWithID(String id, String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUserWithMissingFirstName(String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{" +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUserWithMissingLastName(String firstname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{" +
                "\"firstName\":\"" + firstname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUserWithMissingEmail(String firstname, String lastname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{" +
                "\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getUserById(String id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateUser(String id, String firstname, String lastname, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse deleteUser(String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");

        return response;
    }

    private CloseableHttpResponse getProject(String projectId, String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateProject(String userId, String id, String projectName) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"userId\":\"" + userId + "\"," +
                "\"projectname\":\"" + projectName + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createProject(String projectName, String userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"userId\":" + userId + "," +
                "\"projectname\":\"" + projectName + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse deleteProject(String userId, String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");

        return response;
    }

    private CloseableHttpResponse getAllProjects(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createPomodoroSession(String userId, String projectId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"2019-02-18T20:00Z\"," +
                "\"endTime\":\"2019-02-18T20:00Z\",\"counter\": 0}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createPomodoroWithInvalidInputs(String userId, String projectId, String startTime) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");

        StringEntity input = new StringEntity("{\"startTime\":\"2019-02-18T20:00Z\",\"startTime\":\"" + startTime +
                "\",\"endTime\":\"2019-02-18T20:00Z\",\"counter\":0}");

        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updatePomodoro(String userId, String projectId, String sessionId, String startTime,
                                                 String endTime, int counter) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");

        StringEntity input = new StringEntity("{\"id\":\"" + projectId + "\",\"startTime\":\"" + startTime +
                "\",\"endTime\":\"" + endTime + "\",\"counter\":" + counter + "}");

        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getSessionReports(String userId, String projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report?from=2019-02-18T20:00Z&to=2019-02-18T21:00Z&includeCompletedPomodoros=true&includeTotalHoursWorkedOnProject=true");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // private long getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
    //     HttpEntity entity = response.getEntity();
    //     String strResponse = EntityUtils.toString(entity);
    //     String id = getIdFromStringResponse(strResponse);
    //     return Long.parseLong(id);
    // }

    private String getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        String id = getIdFromStringResponse(strResponse);
        resGlobal = strResponse;
        return id;
    }

    private String getIdFromStringResponse(String strResponse) throws JSONException {
        JSONObject object = new JSONObject(strResponse);

        String id = null;
        Iterator<String> keyList = object.keys();
        while (keyList.hasNext()){
            String key = keyList.next();
            if (key.equals("id")) {
                id = object.get(key).toString();
            }
        }
        return id;
    }

    private long getIdFromJSONObject(JSONObject object) throws JSONException {
        String id = null;
        Iterator<String> keyList = object.keys();
        while (keyList.hasNext()){
            String key = keyList.next();
            if (key.equals("id")) {
                id = object.get(key).toString();
            }
        }
        return Long.parseLong(id);
    }
}
