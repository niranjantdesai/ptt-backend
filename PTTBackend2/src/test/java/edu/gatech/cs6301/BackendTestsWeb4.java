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

public class BackendTestsWeb4 {

    private String baseUrl = "http://localhost:8080/ptt";
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpclient;
    private boolean setupdone;

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


    @Test
    // When adding a user under various valid conditions, the server should return code 201 and the appropriate body
    public void addOneValidUserTest() throws Exception {
        System.out.println("Running Test: addOneValidUserTest");

        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
            // Create user
            int status;
            long uId;
            HttpEntity entity;
            String strResponse;
            CloseableHttpResponse response = createUser("John", "Doe" , "john@doe.org");

            // Verify we get code 201
            status = response.getStatusLine().getStatusCode();
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);
            uId = Long.parseLong(getIdFromStringResponse(strResponse));
            // Verify return body is as expected
            //System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":" + uId + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Do a get to make sure it persisted
            response = getUser(uId);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(200, status);
            entity = response.getEntity();
            strResponse = EntityUtils.toString(entity);
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            response.close();


        } finally {
            httpclient.close();
        }
    }

    @Test
    // When adding a user under various valid conditions, the server should return code 201 and the appropriate body
    public void addManyValidUsersTest() throws Exception {
        System.out.println("Running Test: addManyValidUsersTest");
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
            for (int i=0; i < 100; ++i) {
                // Create user
                int status;
                long uId;
                HttpEntity entity;
                String strResponse;
                CloseableHttpResponse response = createUser("John", "Doe", "john" + Integer.toString(i) + "@doe.org");

                // Verify we get code 201
                status = response.getStatusLine().getStatusCode();
                if (status == 201){
                    entity = response.getEntity();
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
                strResponse = EntityUtils.toString(entity);
                uId = Long.parseLong(getIdFromStringResponse(strResponse));
                // Verify return body is as expected
                // System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

                expectedJson = "{\"id\":" + uId + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john" + Integer.toString(i) + "@doe.org\"}";
                JSONAssert.assertEquals(expectedJson, strResponse, false);
                EntityUtils.consume(response.getEntity());
                response.close();

                // Do a get to make sure it persisted
                response = getUser(uId);
                status = response.getStatusLine().getStatusCode();
                Assert.assertEquals(200, status);
                entity = response.getEntity();
                strResponse = EntityUtils.toString(entity);
                JSONAssert.assertEquals(expectedJson, strResponse, false);
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    // When adding a user, if the body email parameter isn't valid, the server should repsond 400 code and no input.
    public void addUserInvalidEmailTest() throws Exception {
        System.out.println("Running Test: addUserInvalidEmailTest");
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            // Try to create user without email address
            CloseableHttpResponse response = createUser("John", "Doe", "");
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Try to create user without properly formatted email address
            // response = createUser("John", "Doe", "nodomain");
            // status = response.getStatusLine().getStatusCode();
            // Assert.assertEquals(400, status);
            // EntityUtils.consume(response.getEntity());
            // response.close();

        } finally {
            httpclient.close();
        }
    }

    // // no such specification in the API
    // @Test
    // // When adding a user, if the body email parameter isn't valid, the server should repsond 400 code and no input.
    // public void addUserValueTooLongTest() throws Exception {
    //     System.out.println("Running Test:  addUserValueTooLongTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {

    //         // Create a string that is longer than the maximum  length accomodated by the backend.  For now this is anything greater than 1024 characters.
    //         String tooLong = "";
    //         for(int i=0; i<1030; ++i){
    //             tooLong += "a";
    //         }

    //         // Try to create user with firstname that is too long
    //         CloseableHttpResponse response = createUser(tooLong, "Doe", "long@doe.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Try to create user with lastname that is too long
    //         response = createUser("John", tooLong, "john@long.com");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Try to create user with lastname that is too long
    //         response = createUser("John", "Doe", tooLong + "@toolong.com");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // // When adding a user, if a user with the same email exists, then the server should respond with with 409.
    // public void addUserAlreadyExistsTest() throws Exception {
    //     System.out.println("Running Test:  addUserAlreadyExistsTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Try to create user with firstname that is too long
    //         CloseableHttpResponse response = createUser("John", "Doe", "" + "John@doe.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Try to create user with lastname that is too long
    //         response = createUser("John", "Doe", "" + "John@doe.com");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(409, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // // When getting a user that exists, the server should return 200 and the correct user corresponding to the id we specified.
    // public void getExistingUserByIDTest() throws Exception {
    //     System.out.println("Running Test:  getExistingUserByIDTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     String expectedJson = null;

    //     try {
    //         // Create a user
    //         CloseableHttpResponse response = createUser("John", "Doe" , "john@doe.org");
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         long uId = getIdFromResponse(response);
    //         response.close();

    //         // Get the user
    //         HttpEntity entity;
    //         String strResponse;
    //         response = getUser(uId);

    //         // Verify we get code 200
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         // Verify return body is as expected
    //         //System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"id\":" + uId + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // // When getting a user that exists, the server should return 200 and the correct user corresponding to the id we specified.
    // // This should work when the DB has only one user or when there are many users.
    // public void getExistingUserByIDFullDBTest() throws Exception {
    //     System.out.println("Running Test:  getExistingUserByIDFullDBTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     String expectedJson = null;

    //     try {
    //         // Create three users, record second id
    //         CloseableHttpResponse response = createUser("Johnny", "Doe" , "johnny@doe.org");
    //         response.close();
    //         response = createUser("John", "Doe" , "john@doe.org");
    //         long uId = getIdFromResponse(response);
    //         response.close();
    //         response = createUser("Thomas", "Doe" , "tom@doe.org");
    //         response.close();

    //         // Get the user
    //         int status;
    //         HttpEntity entity;
    //         String strResponse;
    //         response = getUser(uId);

    //         // Verify we get code 200
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         // Verify return body is as expected
    //         //System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"id\":" + uId + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    @Test
    // When getting a user that is not found in the DB, the server should report user not found.
    // This test checks for this in both an empty DB and a DB containing users.
    public void userNotFoundTest() throws Exception {
        System.out.println("Running Test:  userNotFoundTest");
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            // Get on an empty DB
            CloseableHttpResponse response = getUser(999);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();

            // Get on DB with users.
            response = createUser("John", "Doe", "john@doe.org");
            long id1 = getIdFromResponse(response);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(201, status);
            response.close();

            response = createUser("Jane", "Doe", "jane@doe.com");
            long id2 = getIdFromResponse(response);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(201, status);
            response.close();

            long findID = id1 + id2 + 42;

            response = getUser(findID);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    @Test
    // When getting a user, if the ID isn't a long int the server should report bad request.
    // Tests a regular alphabetical string and one with special characters not used in URLs
    public void userBadRequestTest() throws Exception {
        System.out.println("Running Test:  userBadRequestTest");
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + "astring");
            httpRequest.addHeader("accept", "application/json");

            //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
            CloseableHttpResponse response = httpclient.execute(httpRequest);
            //System.out.println("*** Raw response " + response + "***");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            response.close();

            httpRequest = new HttpGet(baseUrl + "/users/" + "()-+=");
            httpRequest.addHeader("accept", "application/json");

            //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
            response = httpclient.execute(httpRequest);
            //System.out.println("*** Raw response " + response + "***");

            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    // @Test
    // // When deleting a user that is not found in the DB, the server should report user not found.
    // public void deleteUserNotFoundTest() throws Exception {
    //     System.out.println("Running Test:  deleteUserNotFoundTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response =
    //                 deleteUser(999);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //When deleting a user, if the ID isn't a long int the server should report bad request.
    // public void deleteUserBadRequestTest() throws Exception {
    //     System.out.println("Running Test:  deleteUserBadRequestTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         HttpDelete httpRequest = new HttpDelete(baseUrl + "/users/" + "astring");
    //         httpRequest.addHeader("accept", "application/json");

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         httpRequest = new HttpDelete(baseUrl + "/users/" + "()-+=");
    //         httpRequest.addHeader("accept", "application/json");

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void deleteUserTest() throws Exception {
    //     System.out.println("Running Test:  deleteUserTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     String expectedJson = null;

    //     try {
    //         // Create a user
    //         CloseableHttpResponse response = createUser("John", "Doe" , "john@doe.org");
    //         long deleteid = getIdFromResponse(response);
    //         response.close();

    //         // Create a project for this user
    //         response = createProject("JohnProject", deleteid);
    //         long projid = getIdFromResponse(response);
    //         response.close();

    //         // Delete the user
    //         int status;
    //         HttpEntity entity;
    //         String strResponse;
    //         response = deleteUser(deleteid);

    //         // Verify we get code 200
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         // Verify return body is as expected
    //         //System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"id\":" + deleteid + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Try to get user, verify 404
    //         response = getUser(deleteid);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         response.close();

    //         // Try to get project, verify 404
    //         response = getProject(deleteid, projid);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void deleteUserFromFullDBTest() throws Exception {
    //     System.out.println("Running Test:  deleteUserFromFullDBTest");
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     String expectedJson = null;

    //     try {
    //         long[] userIds = new long[3];
    //         long[] projectIds = new long[6];
    //         int status;

    //         // Create three users
    //         CloseableHttpResponse response = createUser("John", "Doe" , "john@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         userIds[0] = getIdFromResponse(response);
    //         response.close();
    //         response = createUser("Johnny", "Doe" , "johnny@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         userIds[1] = getIdFromResponse(response);
    //         response.close();
    //         response = createUser("Thomas", "Doe" , "tom@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         userIds[2] = getIdFromResponse(response);
    //         response.close();

    //         // Create projects for these users
    //         response = createProject("JohnProject1", userIds[0]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[0]= getIdFromResponse(response);
    //         response.close();
    //         response = createProject("JohnProject2", userIds[0]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[1]= getIdFromResponse(response);
    //         response.close();
    //         response = createProject("JohnnyProject1", userIds[1]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[2]= getIdFromResponse(response);
    //         response.close();
    //         response = createProject("JohnnyProject2", userIds[1]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[3]= getIdFromResponse(response);
    //         response.close();
    //         response = createProject("TomProject1", userIds[2]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[4]= getIdFromResponse(response);
    //         response.close();
    //         response = createProject("TomProject2", userIds[2]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         projectIds[5]= getIdFromResponse(response);
    //         response.close();

    //         // Delete the user
    //         HttpEntity entity;
    //         String strResponse;

    //         response = deleteUser(userIds[1]);

    //         // Verify we get code 200
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         // Verify return body is as expected
    //         //System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"id\":" + userIds[1] + ",\"firstName\":\"Johnny\",\"lastName\":\"Doe\",\"email\":\"johnny@doe.org\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Try to get user, verify 404
    //         response = getUser(userIds[1]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         response.close();

    //         // Try to get project, verify 404
    //         response = getProject(userIds[1], projectIds[2]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         response.close();
    //         response = getProject(userIds[1], projectIds[3]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         response.close();

    //         // Try to get other users and propjects, make sure they are found (code 200)
    //         response = getUser(userIds[0]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();
    //         response = getUser(userIds[2]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();
    //         response = getProject(userIds[0], projectIds[0]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();
    //         response = getProject(userIds[0], projectIds[1]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();
    //         response = getProject(userIds[2], projectIds[4]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();
    //         response = getProject(userIds[2], projectIds[5]);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    @Test
    public void getAllUsersFromEmptyDB() throws Exception {
        System.out.println("Running Test:  getAllUsersFromEmptyDB");
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
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
                Assert.assertEquals(0, jsonUsers.length());
            } catch (JSONException je) {
                throw new ClientProtocolException("JSON PROBLEM: " + je);
            }

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getOneOrMoreUsersFromEmptyDB() throws Exception {
        System.out.println("Running Test:  getOneOrMoreUsersFromEmptyDB");
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
            for (int i = 0; i < 100; ++i) {

                // Create a user, verify 201
                CloseableHttpResponse response = createUser("John", "Doe" , "john" + Integer.toString(i) + "@doe.org");
                int status = response.getStatusLine().getStatusCode();
                Assert.assertEquals(201, status);
                long thisID = getIdFromResponse(response);
                response.close();

                // Get all users
                response = getAllUsers();
                status = response.getStatusLine().getStatusCode();
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
                    // Verify size of users returned, search through to ensure our user is added.
                    JSONArray jsonUsers = new JSONArray(strResponse);
                    Assert.assertEquals(i+1, jsonUsers.length());

                    // For each item in array, parse the id and check it against the currently inserted id
                    for (int j = 0; j < jsonUsers.length(); j++) {
                        long jId = getIdFromJSONObject(jsonUsers.getJSONObject(j));
                        if(jId == thisID){
                            // Verify that this user matches our expectations
                            Assert.assertEquals("John", getKeyFromJSONObject(jsonUsers.getJSONObject(j), "firstName"));
                            Assert.assertEquals("Doe", getKeyFromJSONObject(jsonUsers.getJSONObject(j), "lastName"));
                            Assert.assertEquals("john" + Integer.toString(i) + "@doe.org", getKeyFromJSONObject(jsonUsers.getJSONObject(j), "email"));
                        }
                    }

                } catch (JSONException je) {
                    throw new ClientProtocolException("JSON PROBLEM: " + je);
                }
            }
        } finally {
            httpclient.close();
        }
    }

    // @Test
    // // When adding a project, if the userId is not in DB, or userId isn't of Long (int64) type
    // // or the body is missing, the server should report bad request (400)
    // public void addProjectBadRequestTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // addProject on an empty DB
    //         CloseableHttpResponse response = createProject( "test project", 222);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // create a user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // addProject on non-empty DB but with non-exist userId
    //         response = createProject( "test project", userId+1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // sent addProject request without body
    //         HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When adding a project, if the project body contains conflicting content with
    // // the existing projects, the server should report Resource conflictt (409)
    // public void addProjectResourceConflictTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add first project
    //         response = createProject( "test project", userId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add second project with the same userId and projectname
    //         response = createProject( "test project", userId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(409, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When adding a project with valid parameter, the server should return 201 and the correct project body
    // public void addOneValidProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add project
    //         response = createProject( "test project", userId);
    //         HttpEntity entity = response.getEntity();
    //         String strResponse = EntityUtils.toString(entity);
    //         long projectId = Long.parseLong(getIdFromStringResponse(strResponse));
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         String projectname = getProjectnameFromResponse(strResponse);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // get project by id to make sure the addProject operation successes
    //         response = getProject(userId, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         projectname = getProjectnameFromResponse(response);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When adding a project with valid parameter, the server should return 201 and the correct project body
    // public void addManyValidProjectToSameUserTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         long[] projectIds = new long[100];
    //         // add 100 projects
    //         for (int i = 0; i < 100; i++) {
    //             String projectname = "test project " + i;
    //             response = createProject( projectname, userId);
    //             HttpEntity entity = response.getEntity();
    //             String strResponse = EntityUtils.toString(entity);
    //             projectIds[i] = Long.parseLong(getIdFromStringResponse(strResponse));
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(201, status);
    //             String return_projectname = getProjectnameFromResponse(strResponse);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //         // get project by id to make sure the addProject operation successes
    //         for (int i = 0; i < 100; i++) {
    //             String projectname = "test project " + i;
    //             response = getProject(userId, projectIds[i]);
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(200, status);
    //             String return_projectname = getProjectnameFromResponse(response);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When adding a project with valid parameter, the server should return 201 and the correct project body
    // public void addManyValidProjectToManyUsersTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // create user2
    //         response = createUser("Jane", "Doe", "jane@doe.com");
    //         long userId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         long[] projectIds1 = new long[50];
    //         long[] projectIds2 = new long[100];

    //         // add 50 projects to user1
    //         for (int i = 0; i < 50; i++) {
    //             String projectname = "test project " + i;
    //             response = createProject( projectname, userId1);
    //             HttpEntity entity = response.getEntity();
    //             String strResponse = EntityUtils.toString(entity);
    //             projectIds1[i] = Long.parseLong(getIdFromStringResponse(strResponse));
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(201, status);
    //             String return_projectname = getProjectnameFromResponse(strResponse);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //         // add 50 projects to user2
    //         for (int i = 50; i < 100; i++) {
    //             String projectname = "test project " + i;
    //             response = createProject( projectname, userId2);
    //             HttpEntity entity = response.getEntity();
    //             String strResponse = EntityUtils.toString(entity);
    //             projectIds2[i] = Long.parseLong(getIdFromStringResponse(strResponse));
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(201, status);
    //             String return_projectname = getProjectnameFromResponse(strResponse);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //         // get projects of user1
    //         for (int i = 0; i < 50; i++) {
    //             String projectname = "test project " + i;
    //             response = getProject(userId1, projectIds1[i]);
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(200, status);
    //             String return_projectname = getProjectnameFromResponse(response);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //         // get projects of user1
    //         for (int i = 50; i < 100; i++) {
    //             String projectname = "test project " + i;
    //             response = getProject(userId2, projectIds2[i]);
    //             status = response.getStatusLine().getStatusCode();
    //             Assert.assertEquals(200, status);
    //             String return_projectname = getProjectnameFromResponse(response);
    //             Assert.assertEquals(projectname, return_projectname);
    //             EntityUtils.consume(response.getEntity());
    //             response.close();
    //         }

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When deleting a project, if the userId or projectId isn't Long (int64) type
    // // the server should report bad request (400)
    // public void deleteProjectBadRequestTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // deleteProject with string userId
    //         HttpDelete httpRequest = new HttpDelete(baseUrl + "/users/" + "astring" + "/projects/" + 222);
    //         httpRequest.addHeader("accept", "application/json");

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // deleteProject with string projectId
    //         httpRequest = new HttpDelete(baseUrl + "/users/" + 222 + "/projects/" + "()-+=");
    //         httpRequest.addHeader("accept", "application/json");

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When deleting a project that is not found in the DB, the server should report user or project not found (404)
    // public void deleteProjectNotFoundTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // delete on an empty DB
    //         CloseableHttpResponse response = deleteProject(111, 222);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // delete on DB with user of no project
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = deleteProject(userId, 222);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // delete with non-existing projectId
    //         response = createProject("test project", userId);
    //         long projectId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = deleteProject(userId, projectId+1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // delete with non-existing userId
    //         response = deleteProject(userId+1, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When adding a project with valid parameter, the server should return 201 and the correct project body
    // public void deleteValidProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // create a project
    //         response = createProject( "test project", userId);
    //         HttpEntity entity = response.getEntity();
    //         String strResponse = EntityUtils.toString(entity);
    //         long projectId = Long.parseLong(getIdFromStringResponse(strResponse));
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         String projectname = getProjectnameFromResponse(strResponse);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // get project by id to make sure the addProject operation successes
    //         response = getProject(userId, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         projectname = getProjectnameFromResponse(response);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // delete the project
    //         response = deleteProject(userId, projectId);
    //          entity = response.getEntity();
    //          strResponse = EntityUtils.toString(entity);
    //         long returned_projectId = Long.parseLong(getIdFromStringResponse(strResponse));
    //         Assert.assertEquals(returned_projectId, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         projectname = getProjectnameFromResponse(strResponse);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // confirm the deletion success on the server side
    //         response = getProject(userId, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When getting a project that is not found in the DB, the server should report user or project not found (404)
    // // This test checks for this in both an empty DB and a DB containing users.
    // public void getProjectByIDNotFoundTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getProject(111, 222);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Get on DB with user of no project
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = getProject(userId, 222);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Get on DB with user that contains project
    //         response = createProject("test project", userId);
    //         long projectId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = getProject(userId, projectId+1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When getting a project that exists in server DB with correct projectID and userID
    // // the server should return the correspondin project body (200)
    // public void getValidProjectByIDTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add project
    //         response = createProject( "test project", userId);
    //         long projectId = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // getProjectByID, check if the server really store the correct project
    //         response = getProject(userId, projectId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String projectname = getProjectnameFromResponse(response);
    //         Assert.assertEquals("test project", projectname);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // // When getting a project, if the userId or projectId isn't Long (int64) type
    // // the server should report bad request (400)
    // public void getProjectByIDBadRequestTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // get project with string userId
    //         HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + "astring" + "/projects/" + 222);
    //         httpRequest.addHeader("accept", "application/json");

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // get project with string projectId
    //         httpRequest = new HttpGet(baseUrl + "/users/" + 222 + "/projects/" + "()-+=");
    //         httpRequest.addHeader("accept", "application/json");

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportUserOrProjectNotFound() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getUser(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = getReport(100, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", false, false);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = getReport(userId1, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", false, false);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = getReport(100, projectId1, "2013-03-01T23:59:59", "2013-03-01T23:59:59", false , false);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportInvalidParameterType() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + "aa" + "/projects/" + "aa" + "/report?from=" +
    //                 "2013-03-01T23:59:59" + "&to=" + "2013-03-02T23:59:59" + "&includeCompletedPomodoros=" + false + "&includeTotalHoursWorkedOnProject=" +
    //                 false);
    //         httpRequest.addHeader("accept", "application/json");

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpGet(baseUrl + "/users/" + userId1 + "/projects/" + "aa" + "/report?from=" +
    //                 "2013-03-01T23:59:59" + "&to=" + "2013-03-02T23:59:59" + "&includeCompletedPomodoros=" + false + "&includeTotalHoursWorkedOnProject=" +
    //                 false);
    //         httpRequest.addHeader("accept", "application/json");

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject("testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpGet(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/report?from=" +
    //                 "2013-03/01T23:59:59" + "&to=" + "2013-03-01T23:59:59" + "&includeCompletedPomodoros=" + "false" + "&includeTotalHoursWorkedOnProject=" +
    //                 false);
    //         httpRequest.addHeader("accept", "application/json");

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // // getReportInvalidParameterLength removed - we do not need to test for an integer overflow.  Invalid lengths apply to strings only.

    // @Test
    // public void getReportSuccessfully() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getUser(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one project
    //         response = createProject( "aa", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         HttpEntity entity;

    //         response = getReport(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", false, false);
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }

    //         String expectedJson = "{\"sessions\":\"" + "[]" + "\",\"completedPomodoros\":\"0\",\"totalHoursWorkedOnProject\":\"24\"}";
    //         String strResponse = EntityUtils.toString(entity);
    //         // Check that the record is correct in the response
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void addSessionUserOrProjectNotFound() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getUser(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = createSession(999, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = createSession(userId1, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "aa", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = createSession(100, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // public void addSessionInvalidParameterType() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + "aa" + "/projects/" + "aa" + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPost(baseUrl + "/users/" + userId1 + "/projects/" + "aa" + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPost(baseUrl + "/users/" + "aa" + "/projects/" + projectId1 + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // check for invalid type of time
    //         httpRequest = new HttpPost(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013/03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013/03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void addSessionInvalidParameterLength() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add one user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + (Long.MAX_VALUE + 12) + "/projects/" + projectId1 + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // check for invalid type of time
    //         httpRequest = new HttpPost(baseUrl + "/users/" + userId1 + "/projects/" + (Long.MAX_VALUE + 12) + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013/03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013/03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void addSessionParameterBody() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add one user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013==03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // check for invalid type of time
    //         httpRequest = new HttpPost(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions");
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013/03-02T23:59:59" +
    //                 "\"counter\":\"" + 1 + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // public void addSessionSuccessfully() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getUser(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject("aa", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         HttpEntity entity;

    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 201) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         // Check that the session is correct in the response
    //         String expectedJson = "{\"id\":\"" + sessionId1 + "\",\"startTime\":\"2013-03-01T23:59:59\",\"endTime\":\"2013-03-02T23:59:59\", \"counter\":\"1\"}";
    //         String strResponse = EntityUtils.toString(entity);
    //         // Check that the record is correct in the response
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void updateSessionUserOrProjectNotFound() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // Get on an empty DB
    //         CloseableHttpResponse response = getUser(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = updateSession(100, 100, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = updateSession(userId1, 100, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "aa", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = updateSession(100, projectId1, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one session
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         response = updateSession(userId1, projectId1, 100, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void updateSessionInvalidParameterType() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + "aa" + "/projects/" + "aa" + "/sessions/" + "aa");
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + "aa" + "/sessions/" + 100);
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + "aa" + "/projects/" + projectId1 + "/sessions/" + 100);
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one session
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions/" + 100);
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void updateSessionInvalidParameterLength() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + (Long.MAX_VALUE + 12) + "/projects/" + (Long.MAX_VALUE + 12) + "/sessions/" + (Long.MAX_VALUE + 12));
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         CloseableHttpResponse response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one user
    //         response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + (Long.MAX_VALUE + 12) + "/sessions/" + (Long.MAX_VALUE + 12));
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions/" + (Long.MAX_VALUE + 12));
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add one session
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         httpRequest = new HttpPut(baseUrl + "/users/" + (Long.MAX_VALUE + 12) + "/projects/" + projectId1 + "/sessions/" + sessionId1);
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // public void updatessionInvalidParameterBody() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add one user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one session
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // check the type of startTime
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions/" + sessionId1);
    //         httpRequest.addHeader("accept", "application/json");
    //         StringEntity input = new StringEntity("{\"startTime\":\"" + "2013-=03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013-03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // check the type of endTime
    //         httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + projectId1 + "/sessions/" + sessionId1);
    //         httpRequest.addHeader("accept", "application/json");
    //         input = new StringEntity("{\"startTime\":\"" + "2013-03-01T23:59:59" + "\"," +
    //                 "\"endTime\":\"" + "2013+03-02T23:59:59" +
    //                 "\"counter\":\"" + "1" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);


    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    // @Test
    // public void updateSessionSuccessfully() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add one user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one project
    //         response = createProject( "testProject", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         // add one session
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         response.close();

    //         HttpEntity entity;

    //         response = updateSession(userId1, projectId1, sessionId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 8);
    //         sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         // Check that the session is correct in the response
    //         String expectedJson = "{\"id\":\"" + sessionId1 + "\",\"startTime\":\"2013-03-01T23:59:59\",\"endTime\":\"2013-03-02T23:59:59\", \"counter\":\"8\"}";
    //         String strResponse = EntityUtils.toString(entity);
    //         // Check that the record is correct in the response
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = updateSession(userId1, projectId1, sessionId1, "2013-03-01T23:59:59", "2013-04-02T23:59:59", 8);
    //         sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         // Check that the session is correct in the response
    //         expectedJson = "{\"id\":\"" + sessionId1 + "\",\"startTime\":\"2013-03-01T23:59:59\",\"endTime\":\"2013-04-02T23:59:59\", \"counter\":\"8\"}";
    //         strResponse = EntityUtils.toString(entity);
    //         // Check that the record is correct in the response
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // //TESTS FOR UPDATE USER
    // @Test
    // //update the user with the userId isn't a long int, the server should return code 400 and no body
    // public void updateUserBadUserIdFormat() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update the user with the userId isn't a long int
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + "astring");
    //         httpRequest.addHeader("accept", "application/json");

    //         //add body content sent to server
    //         StringEntity input = new StringEntity("{\"id\":\"" + "astring" + "\"," +
    //                 "\"firstName\":\"" + "John" + "\"," + "\"lastName\":\"" + "Lee" + "\"," +
    //                 "\"email\":\"" + "john@doe.org" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         //verify if server returns 400 and no body
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //userId in request is not 64 bits long
    //         httpRequest = new HttpPut(baseUrl + "/users/" + "()-+=");
    //         httpRequest.addHeader("accept", "application/json");
    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         //add body content sent to server
    //         input = new StringEntity("{\"id\":\"" + "()-+=" + "\"," +
    //                 "\"firstName\":\"" + "John" + "\"," + "\"lastName\":\"" + "Lee" + "\"," +
    //                 "\"email\":\"" + "john@doe.org" + "\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);

    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when update a user, if the userId doesn’t exist, the server should report request(404)
    // public void updateUserNotFound() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response = updateUser(999, "shuangke", "lastName", "Shuangke@gmail.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //update user with firstname exceed 1024 characters
    // public void updateUserWithInvalidFirstName() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         //create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         String tooLongFirstName = "";
    //         for(int i=0; i<1030; ++i){
    //             tooLongFirstName += "a";
    //         }
    //         response = updateUser(userId, tooLongFirstName, "Doe", "john@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //update user with lastname exceed 1024 characters
    // public void updateUserWithInvalidLastName() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         //create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         String tooLongLastName = "";
    //         for(int i=0; i<1030; ++i){
    //             tooLongLastName += "a";
    //         }
    //         response = updateUser(userId, "John", tooLongLastName, "john@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //Update multiple users
    // public void updateMultipleUsers() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         //create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create user2
    //         response = createUser("Shuangke", "Li", "shuangke@.org");
    //         long userId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update user1
    //         response = updateUser(userId1, "David", "Doe", "john@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String returnedBody1 = EntityUtils.toString(response.getEntity());

    //         //check if server returns the expected body
    //         String expectedBody1 = "{\"id\":" + userId1 + ",\"firstName\":\"David\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
    //         JSONAssert.assertEquals(expectedBody1,returnedBody1, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update user2
    //         response = updateUser(userId2, "Shuangke", "Zhang", "shuangke@.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String returnedBody2 = EntityUtils.toString(response.getEntity());

    //         //check if server returns the expected body
    //         String expectedBody2 = "{\"id\":" + userId2 + ",\"firstName\":\"Shuangke\",\"lastName\":\"Zhang\",\"email\":\"shuangke@.org\"}";
    //         JSONAssert.assertEquals(expectedBody2,returnedBody2, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //Update user successfully
    // public void updateUserSuccessful() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         //create user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update user
    //         response = updateUser(userId1, "David", "Doe", "john@doe.org");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String returnedBody1 = EntityUtils.toString(response.getEntity());

    //         //check if server returns the expected body
    //         String expectedBody1 = "{\"id\":" + userId1 + ",\"firstName\":\"David\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
    //         JSONAssert.assertEquals(expectedBody1, returnedBody1, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }
    // //TESTS FOR RETURNING ALL PROJECTS OWNED BY A GIVEN USER
    // @Test
    // //return all projects associated with a given user, userId not found
    // public void returnProjectsUserNotFound() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         //create user
    //         CloseableHttpResponse response = getAllProjects(999);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //return all projects associated with a given user in bad userId format
    // public void returnProjectsBadUserIdFormat() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create a project
    //         response = createProject("webProject", userId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //         //return projects with the userId isn't a long int
    //         HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + "astring" + "/projects"); //put userId as string format not a long int
    //         httpRequest.addHeader("accept", "application/json");
    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //return projects with the userId isn't a long int
    //         httpRequest = new HttpGet(baseUrl + "/users/" + "()-+=" + "/projects");
    //         httpRequest.addHeader("accept", "application/json");
    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when you retrieve a project list of a user who has no project associated with him,
    // // the server should return code 200 and an empty body
    // public void returnProjectsNoProject() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = getAllProjects(userId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String returnedBody = EntityUtils.toString(response.getEntity());
    //         Assert.assertEquals("[]", returnedBody);
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when you try to retrieve project lists from multiple users with valid userId,
    // // the server should return code 200 and a body for each user
    // public void  returnProjectsFromMultipleUsers() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create user2
    //         response = createUser("Shuangke", "Li", "shuangke@doe.org");
    //         long userId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject( "project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project2 for user1
    //         response = createProject("project2", userId1);
    //         long project2Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project3 for user2
    //         response = createProject("project3", userId2);
    //         long project3Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //retrieve all projects owned by user1
    //         response = getAllProjects(userId1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String expectedBody = "[{\"id\":"+ project1Id + ",\"projectname\":\"project1\",\"userId\":" + userId1 +"},"+
    //                 "{\"id\":"+ project2Id + ",\"projectname\":\"project2\",\"userId\":" + userId1 + "}]";
    //         String returnedBody = EntityUtils.toString(response.getEntity());
    //         Assert.assertEquals(expectedBody, returnedBody);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //retrieve all projects owned by user2
    //         response = getAllProjects(userId2);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         expectedBody = "[{\"id\":"+ project3Id + ",\"projectname\":\"project3\",\"userId\":"+ userId2 +"}]";
    //         returnedBody = EntityUtils.toString(response.getEntity());
    //         Assert.assertEquals(expectedBody, returnedBody);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when retrieve all projects owned by a user with correct input,
    // //the server should return all projects related to the user and request (200)
    // public void returnProjectSuccessful() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject( "project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project2 for user1
    //         response = createProject( "project2", userId1);
    //         long project2Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //retrieve all projects owned by user1
    //         response = getAllProjects(userId1);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         String expectedBody = "[{\"id\":"+ project1Id + ",\"projectname\":\"project1\",\"userId\":"+ userId1 +"},"+
    //                 "{\"id\":"+ project2Id + ",\"projectname\":\"project2\",\"userId\":" + userId1 + "}]";
    //         String returnedBody = EntityUtils.toString(response.getEntity());
    //         Assert.assertEquals(expectedBody, returnedBody);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // //*****************************************UPDATE PROJECT****************************
    // @Test
    // //when update a project with both userId and projectId do not exist,
    // // the server should return request(404) and no body
    // public void updateProjectUserIdProjectIdNotFound()throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response = updateProject(999, 998, "updatedProject");
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when update a project with projectId do not exist,
    // // the server should return request(404) and no body
    // public void updateProjectProjectIdNotFound()throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create a user
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update a project with a not existing project Id
    //         response = updateProject(userId, 999, "updatedProject");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when update a project with a userId isn't Long (int64) type,
    // //the server should return request(400) and no body
    // public void updateProjectBadUserIdFormat() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject( "project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //         //update the project with the userId isn't a long int
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + "astring" + "/projects/" + project1Id);
    //         httpRequest.addHeader("accept", "application/json");

    //         //add body content sent to server
    //         StringEntity input = new StringEntity("{\"id\":" + project1Id + "," +
    //                 "\"projectname\":\"" + "updatedProject1" + "\"," + "\"userId\":" + "\"astring\"}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);

    //         //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         //System.out.println("*** Raw response " + response + "***");

    //         //verify if server returns 400 and no body
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when update a project with a projectId isn't Long (int64) type,
    // //the server should return request(400) and no body
    // public void updateProjectBadProjectIdFormat() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject( "project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //         //update the project with the projectId isn't a long int
    //         HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId1 + "/projects/" + "astring");
    //         httpRequest.addHeader("accept", "application/json");

    //         //add body content sent to server
    //         StringEntity input = new StringEntity("{\"id\":" + "astring" + "," +
    //                 "\"projectname\":\"" + "\"updatedProject1\"" + "," + "\"userId\":" + userId1 + "}");
    //         input.setContentType("application/json");
    //         httpRequest.setEntity(input);

    //         System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //         response = httpclient.execute(httpRequest);
    //         System.out.println("*** Raw response " + response + "***");

    //         //verify if server returns 400 and no body
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // //when update a project with all correct inputs,
    // //the server should return request(200) and a body which contains updated project info
    // public void updateProjectSuccessful() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create user2
    //         response = createUser("Shuangke", "Li", "shuangke@doe.org");
    //         long userId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject("project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project2 for user1
    //         response = createProject( "project2", userId1);
    //         long project2Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project3 for user2
    //         response = createProject( "project3", userId2);
    //         long project3Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update project1
    //         response = updateProject(userId1, project1Id, "updatedProject1");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         //verify returned body
    //         String returnedBody1 = EntityUtils.toString(response.getEntity());
    //         String expectedBody1 = "{\"id\":"+ project1Id + "," + "\"projectname\":\"updatedProject1\"," + "\"userId\":" + userId1 + "}";
    //         Assert.assertEquals(expectedBody1, returnedBody1);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update project2
    //         response = updateProject(userId1, project2Id, "updatedProject2");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         //verify returned body
    //         String returnedBody2 = EntityUtils.toString(response.getEntity());
    //         String expectedBody2 = "{\"id\":"+ project2Id + "," + "\"projectname\":\"updatedProject2\"," + "\"userId\":" + userId1 + "}";
    //         Assert.assertEquals(expectedBody2, returnedBody2);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update project3
    //         response = updateProject(userId2, project3Id, "updatedProject3");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(200, status);
    //         //verify returned body
    //         String returnedBody3 = EntityUtils.toString(response.getEntity());
    //         String expectedBody3 = "{\"id\":"+ project3Id + "," + "\"projectname\":\"updatedProject3\"," + "\"userId\":" + userId2 + "}";
    //         Assert.assertEquals(expectedBody2, returnedBody2);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // // update project with duplicated project name
    // //server should return code 409
    // public void updateProjectWithDuplicateName() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         // create user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project1 for user1
    //         response = createProject("project1", userId1);
    //         long project1Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //create project2 for user1
    //         response = createProject( "project2", userId1);
    //         long project2Id = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //update project2 with duplicated project name
    //         response = updateProject(userId1, project2Id, "project1");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(409, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // // user/uid/projects/pid  requests such that uid doesn’t own pid, should return 400
    // public void requestOthersResourceTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();

    //     try {
    //         // add user1
    //         CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
    //         long userId1 = getIdFromResponse(response);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add project1 to user1
    //         response = createProject("test project1", userId1);
    //         long projectId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add user2
    //         response = createUser("Jacky", "Lee", "jlee@lee.org");
    //         long userId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add project2 to user2
    //         response = createProject("test project2", userId2);
    //         long projectId2 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to delete project2 using userId1
    //         response = deleteProject(userId1, projectId2);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to get project2 using userId1
    //         response = getProject(userId1, projectId2);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to update project1 using userId2
    //         response = updateProject(userId2, projectId1, "new project name");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // add session1 to project1
    //         response = createSession(userId1, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", 1);
    //         long sessionId1 = getIdFromResponse(response);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(201, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to create session on project1 using userId2
    //         response = createSession(userId2, projectId1, "2014-03-01T23:59:59", "2014-03-02T23:59:59", 2);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to update session on project1 using userId2
    //         response = updateSession(userId2, projectId1, sessionId1, "2015-03-01T23:59:59", "2015-03-02T23:59:59", 3);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // try to get report of projectId1 using userId2
    //         response = getReport(userId2, projectId1, "2013-03-01T23:59:59", "2013-03-02T23:59:59", true, true);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }



    // Helper functions located below this line //

    // Delete all users (also deletes all projects
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

    // update a user
    private CloseableHttpResponse updateUser(long userId, String firstName, String lastName, String email) throws IOException{
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + userId + "," +  "\"firstName\":\"" + firstName + "\"," +
                "\"lastName\":\"" + lastName + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Delete a user
    private CloseableHttpResponse deleteUser(String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Get all Users
    private CloseableHttpResponse getAllUsers() throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");

        //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Gets a user by ID
    private CloseableHttpResponse getUser(long id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");

        //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Creates a User
    private CloseableHttpResponse createUser(String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        // System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Delete a project
    private CloseableHttpResponse deleteProject(long userId, long projectId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpDelete.addHeader("accept", "application/json");

        //System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Get all projects
    private CloseableHttpResponse getAllProjects(long userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");

        //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Creates a project
    private CloseableHttpResponse createProject(String projectName, long userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectName + "\"," +
                "\"userId\":\"" + Long.toString(userId) + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
       // System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //update the project by userId and projectId
    private CloseableHttpResponse updateProject(long userId, long projectId, String projectName) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + projectId + "," +
                "\"projectname\":\"" + projectName + "\"," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Gets a project by ID
    private CloseableHttpResponse getProject(long userId, long projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");

        //System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        //System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Gets ID from a response object
    private long getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        String id = getIdFromStringResponse(strResponse);
        return Long.parseLong(id);
    }

    // Gets Id from string representation of a response
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

    // Gets an arbitrary key from string representation of a response
    private String getKeyFromJSONObject(JSONObject object, String searchKey) throws JSONException {
        String value = null;
        Iterator<String> keyList = object.keys();
        while (keyList.hasNext()){
            String key = keyList.next();
            if (key.equals(searchKey)) {
                value = object.get(key).toString();
            }
        }
        return value;
    }

    // Gets Id from a JSON Object
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

    private String getProjectnameFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);

        String projectname = null;
        try {
            // Get Ids from the response
            JSONObject project = new JSONObject(strResponse);
            Iterator<String> keyList = project.keys();
            while (keyList.hasNext()){
                String key = keyList.next();
                if (key.equals("projectname")) {
                    projectname = project.get(key).toString();
                    break;
                }
            }
        } catch (JSONException je) {
            throw new ClientProtocolException("JSON PROBLEM: " + je);
        }
        return projectname;
    }
    private String getProjectnameFromResponse(String strResponse) throws IOException, JSONException {
        String projectname = null;
        try {
            // Get Ids from the response
            JSONObject project = new JSONObject(strResponse);
            Iterator<String> keyList = project.keys();
            while (keyList.hasNext()){
                String key = keyList.next();
                if (key.equals("projectname")) {
                    projectname = project.get(key).toString();
                    break;
                }
            }
        } catch (JSONException je) {
            throw new ClientProtocolException("JSON PROBLEM: " + je);
        }
        return projectname;
    }

    // Creates a Session
    private CloseableHttpResponse createSession(long userId, long projectId, String startTime, String endTime, long counter) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":\"" + Long.toString(counter) + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Update a Session
    private CloseableHttpResponse updateSession(long userId, long projectId, long sessionId, String startTime, String endTime, long counter) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + Long.toString(counter) + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Get a report
    private CloseableHttpResponse getReport(long userId, long projectId, String from, String to,
                                            boolean includeCompletedPomodoros, boolean includeTotalHoursWorkedOnProject) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report?from=" +
                from + "&to=" + to + "&includeCompletedPomodoros=" + includeCompletedPomodoros + "&includeTotalHoursWorkedOnProject=" +
                includeTotalHoursWorkedOnProject);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

}
