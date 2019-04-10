package edu.gatech.cs6301;

import java.io.IOException;
import java.util.*;

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
import static org.junit.Assert.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.skyscreamer.jsonassert.JSONAssert;

public class BackendTestsDevOps12 {

    private String baseUrl = ""; // I changed this to 8081/ptt to match yaml spec
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpclient;
    private boolean setupdone;
    
    @Before
    public void runBefore() {
	if (!setupdone) {
	    System.out.println("*** SETTING UP TESTS ***");

        // Read environment parameter
        Map<String, String> env = System.getenv();
        if(env.containsKey("PTT_URL")){
            baseUrl = env.get("PTT_URL");
        }else{
            baseUrl = "http://localhost:8080/ptt";
        }

	    // Increase max total connection to 100
	    cm.setMaxTotal(100);
	    // Increase default max connection per route to 20
	    cm.setDefaultMaxPerRoute(10);
	    // Increase max connections for localhost:80 to 50
	    HttpHost localhost = new HttpHost("locahost", 8080); // I changed this to 8081 to match yaml spec
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
    
	/* USER */
   // GET /users by Joey
   @Test
   public void getUserSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           // test for success
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");
           id = getIdFromResponse(response);
           expectedJson += "[{\"id\":" + id + ",\"firstName\":\"firstname1\",\"lastName\":\"lastname1\",\"email\":\"email1\"}";
           response.close();

           response = addUser("firstname2", "lastname2", "email2");
           id = getIdFromResponse(response);
           expectedJson += ",{\"id\":" + id + ",\"firstName\":\"firstname2\",\"lastName\":\"lastname2\",\"email\":\"email2\"}]";
           response.close();

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
           JSONAssert.assertEquals(expectedJson, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // POST /users by Joey
   @Test
   public void postUserSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           // test for success
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");

           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 201) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           
           String strResponse = EntityUtils.toString(entity);
           id = getIdFromStringResponse(strResponse);
           expectedJson += "{\"id\":" + id + ",\"firstName\":\"firstname1\",\"lastName\":\"lastname1\",\"email\":\"email1\"}";

           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           JSONAssert.assertEquals(expectedJson, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // POST /users by Joey
   @Test
   public void postUserSuccessEmptyTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           // test for success
           CloseableHttpResponse response = addUser("", "", ""); // all input empty

           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }

           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   @Test
   public void postUserInvalidTest() throws Exception { // 400
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           CloseableHttpResponse response = addBadUser("firstname1", "lastname1", "email1"); // invalid JSON

           int status = response.getStatusLine().getStatusCode();

           Assert.assertEquals(400, status);

           EntityUtils.consume(response.getEntity());
           response.close();
       } finally {
           httpclient.close();
       }
   }
//
   // POST /users by Joey -> duplicate email
   @Test
   public void postUserDuplicateTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           // test for success
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");
           response.close();
           
           response = addUser("firstname1", "lastname1", "email1"); // add same email -> resource conflict
           int status = response.getStatusLine().getStatusCode();

           Assert.assertEquals(409, status); // resource conflict

           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // GET /users/{userId} by Joey
   @Test
   public void getUserIdSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");
           id = getIdFromResponse(response);
           expectedJson += "{\"id\":" + id + ",\"firstName\":\"firstname1\",\"lastName\":\"lastname1\",\"email\":\"email1\"}";
           response.close();

           response = getUserbyId(id);
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
           response.close();


       } finally {
           httpclient.close();
       }
   }
//
   // GET /users/{userId} by Joey
   @Test
   public void addMultiplegetUserIdSuccessTest() throws Exception { // multiple userID added
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id1 = null;
       String id2 = null;
       String id3 = null;
       String expectedJson1 = "";
       String expectedJson2 = "";
       String expectedJson3 = "";
       try {
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");
           id1 = getIdFromResponse(response);
           expectedJson1 += "{\"id\":" + id1 + ",\"firstName\":\"firstname1\",\"lastName\":\"lastname1\",\"email\":\"email1\"}";
           response.close();

           response = addUser("firstname2", "lastname2", "email2");
           id2 = getIdFromResponse(response);
           expectedJson2 += "{\"id\":" + id2 + ",\"firstName\":\"firstname2\",\"lastName\":\"lastname2\",\"email\":\"email2\"}";
           response.close();

           response = addUser("firstname3", "lastname3", "email3");
           id3 = getIdFromResponse(response);
           expectedJson3 += "{\"id\":" + id3 + ",\"firstName\":\"firstname3\",\"lastName\":\"lastname3\",\"email\":\"email3\"}";
           response.close();

           response = getUserbyId(id2);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           String strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           JSONAssert.assertEquals(expectedJson2, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           response = getUserbyId(id1);
           status = response.getStatusLine().getStatusCode();
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           JSONAssert.assertEquals(expectedJson1, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
//
   // GET /users/{userId} by Joey
   @Test
   public void getUserIDMissingTest() throws Exception { // get missing user id
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           CloseableHttpResponse response = addUser("firstname1", "lastname1", "email1");
           id = getIdFromResponse(response);
           expectedJson += "{\"id\":\"" + id + "\",\"firstName\":\"firstname1\",\"lastName\":\"lastname1\",\"email\":\"email1\"}";
           response.close();

           response = getUserbyId("9999" + id + id);
           int status = response.getStatusLine().getStatusCode();
           Assert.assertEquals(404, status);

           EntityUtils.consume(response.getEntity());
           response.close();


       } finally {
           httpclient.close();
       }
   }
//
//
   // PUT /users/{userId} by Lee Sun
   // Case 1: Successful modification of single user
   @Test
   public void putUsersSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       try {
           
           deleteAllUsers();
           CloseableHttpResponse response = addUser("putUser", "One", "pusr1@example.com");
           String userId = getIdFromResponse(response);
           response.close();

           response = updateUser(userId, "putUserMod", "OneMod", "pusr1Mod@example.com");
           int status = response.getStatusLine().getStatusCode();
           //userId = getIdFromResponse(response);
           String strResponse;

           // Check response code
           
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + " while it should be 200");
           }
           strResponse = EntityUtils.toString(entity);

           String id = getIdFromStringResponse(strResponse);

           // Check modified user content
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"putUserMod\",\"lastName\":\"OneMod\",\"email\":\"pusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           //response = deleteUser(userId);
           //response.close();

       } finally {
           httpclient.close();
       }
   }

   // PUT /users/{userId} by Lee Sun
   // Case 2: Successful modification of single user when multiple users are added
   @Test
   public void putMultiUsersSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
     
       try {
           deleteAllUsers();
           CloseableHttpResponse response = addUser("putUser", "One", "pusr1@example.com");
           String userId = getIdFromResponse(response);
           response.close();

           response = addUser("putUser", "Two", "pusr2@example.com");
           String otherUserId = getIdFromResponse(response);
           response.close();

           response = updateUser(userId, "putUserMod", "OneMod", "pusr1Mod@example.com");

           // Check response code
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);

           String id = getIdFromStringResponse(strResponse);

           // Check modified user content
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"putUserMod\",\"lastName\":\"OneMod\",\"email\":\"pusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check that other user was not modified
           response = getUserbyId(otherUserId);
           status = response.getStatusLine().getStatusCode();
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           expectedJson = "{\"id\":" + otherUserId + ",\"firstName\":\"putUser\",\"lastName\":\"Two\",\"email\":\"pusr2@example.com\"}";
           JSONAssert.assertEquals(expectedJson,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();


       } finally {
           httpclient.close();
       }
   }
//
   // PUT /users/{userId} by Lee Sun
   // Case 3: Unsuccessful modification - Invalid user
   @Test
   public void putUsersNoUserTest() throws Exception {
       try {
           // test for badrequest: No such user
           deleteAllUsers();
           CloseableHttpResponse response = updateUser("-1", "putUserMod", "OneMod", "pusr1Mod@example.com");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "User not found";
           assertEquals("actual: " + strResponse + ", expect: " + expected, expected, strResponse);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // PUT /users/{userId} by Lee Sun
   // Case 4: Unsuccessful modification - Invalid request
   @Test
   public void putUsersInvalidRequestTest() throws Exception {
       try {
           // test for badrequest: No such user
           deleteAllUsers();
           CloseableHttpResponse response = invalidUpdateUser("-1", 5, "OneMod", "pusr1Mod@example.com");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "User not found";
           assertEquals("actual: " + strResponse + ", expect: " + expected, expected, strResponse);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//    // DELETE /users/{userId} by Lee Sun
//    // Case 1: Successful Deletion of User w/ no projects
   @Test
   public void deleteUsersSuccessTest() throws Exception {
    httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       try {
            deleteAllUsers();
           // Create user to delete
           CloseableHttpResponse response = addUser("deleteUser", "One", "dusr1@example.com");
           String id = getIdFromResponse(response);
           response.close();

           int status;
            HttpEntity entity;
            String strResponse;

           // Delete user
           response = deleteUser(id);
        //    String userId = getIdFromResponse(response);
        //    response.close();

           // Check response code
           status = response.getStatusLine().getStatusCode();
        //    HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           strResponse = EntityUtils.toString(entity);

           // Check deleted user content
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"deleteUser\",\"lastName\":\"One\",\"email\":\"dusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check user is deleted
           response = getUserbyId(id);
           status = response.getStatusLine().getStatusCode();
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }

   // DELETE /users/{userId} by Lee Sun
   // Case 2: Successful Deletion of User w/ projects
   @Test
   public void deleteUsersProjectsSuccessTest() throws Exception {
    httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       try {
        deleteAllUsers();
           // Create user to delete
           CloseableHttpResponse response = addUser("deleteUser", "One", "dusr1@example.com");
           String id = getIdFromResponse(response);
           response.close();

           // Add project
           response = addProject(id, "Project1");
           String projectId = getIdFromResponse(response);
           response.close();

           // Delete user
           response = deleteUser(id);
        //    response.close();

           // Check response code
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);

           // Check modified user content
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"deleteUser\",\"lastName\":\"One\",\"email\":\"dusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check project is deleted
           response = getProject(id, projectId);
           status = response.getStatusLine().getStatusCode();
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check user is deleted
           response = getUserbyId(id);
           status = response.getStatusLine().getStatusCode();
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }

   // DELETE /users/{userId} by Lee Sun
   // Case 3: Unsuccessful delete - Invalid User
   @Test
   public void deleteUsersNoUserTest() throws Exception {
    httpclient = HttpClients.createDefault();
       try {
        deleteAllUsers();
           // test for badrequest: No such user
           // Create user
           CloseableHttpResponse response = addUser("deleteUser", "One", "dusr1@example.com");
           String id = getIdFromResponse(response);
           response.close();

           response = deleteUser(id + id);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
           }
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check that user is still there
           response = getUserbyId(id);
           status = response.getStatusLine().getStatusCode();
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           String strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"deleteUser\",\"lastName\":\"One\",\"email\":\"dusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

        //    response = deleteAllUsers();
           response.close();
       } finally {
           httpclient.close();
       }
   }

   // DELETE /users/{userId} by Lee Sun
   // Case 4: Unsuccessful delete - Invalid Request
   @Test
   public void deleteUsersInvalidRequestTest() throws Exception {
    httpclient = HttpClients.createDefault();
       try {
        deleteAllUsers();
           // test for badrequest: No such user
           // Create user
           CloseableHttpResponse response = addUser("deleteUser", "One", "dusr1@example.com");
           String id = getIdFromResponse(response);
           response.close();

        //    response = invalidDeleteUser(-1);
            response = deleteUser("xyz");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
           }
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check that user is still there
           response = getUserbyId(id);
           status = response.getStatusLine().getStatusCode();
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           String strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           String expectedJson = "{\"id\":" + id + ",\"firstName\":\"deleteUser\",\"lastName\":\"One\",\"email\":\"dusr1@example.com\"}";
           JSONAssert.assertEquals(expectedJson, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();
       } finally {
           httpclient.close();
       }
   }

	/* PROJECT */
    // POST /users/{userId}/projects by Jiaying He
    @Test
    public void postProjectSuccessTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // test for success
            CloseableHttpResponse response = addUser("firstname", "lastname", "email");
            String userId = getIdFromResponse(response);
            response.close();

            response = addProject(userId, "project");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 201");
            }
            String strResponse = EntityUtils.toString(entity);

            String id = getIdFromStringResponse(strResponse);

            String expected = "{\"id\":" + id + ",\"projectname\":\"project\",\"userId\":" + userId +"}";
            JSONAssert.assertEquals(expected,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // delete user and project
            response = deleteProject(userId, id);
            response.close();

            response = deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }

    }

    @Test
    public void postProjectBadRequestTest1() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // test for badrequest: Invalid userId
            CloseableHttpResponse response = addProjectFormat("abc", "\"project\"");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 400) {
                System.out.println(
                        "*** Correct! Expected response status: 400 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void postProjectBadRequestTest2() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // test for badrequest: Invalid projectname
            CloseableHttpResponse response = addProjectFormat("0", "project");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 400) {
                System.out.println(
                        "*** Correct! Expected response status: 400 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
            }
            
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void postProjectResourceConflictTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // test for resource conflict
            CloseableHttpResponse response = addUser("firstname", "lastname", "email");
            String userId = getIdFromResponse(response);
            response.close();

            response = addProject(userId, "project");
            response.close();

            response = addProject(userId, "project");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 409) {
                System.out.println(
                        "*** Correct! Expected response status: 409 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 409");
            }
        
            EntityUtils.consume(response.getEntity());
            response.close();
            deleteUser(userId);
        } finally {
            httpclient.close();
        }
    }

   // GET /users/{userId}/projects by Jiaying He
   @Test
   public void getAllProjectsOfUserBadRequestTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test bad request
           CloseableHttpResponse response = getAllProjects("invalidId");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
           }
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }

   @Test
   public void getAllProjectsOfUserUserNotFoundTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test user not found
           CloseableHttpResponse response = getAllProjects("0");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
           }
           EntityUtils.consume(response.getEntity());
           response.close();
       } finally {
           httpclient.close();
       }
   }

   @Test
   public void getAllProjectsOfUserNoProjectTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test if user has no projects
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = getAllProjects(userId);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "[]";
           assertEquals("actual: " + strResponse + ", expect: " + expected, expected, strResponse);
           EntityUtils.consume(response.getEntity());
           response.close();

           // delete user
           response = deleteUser(userId);
           response.close();
       } finally {
           httpclient.close();
       }
   }

   @Test
   public void getAllProjectsOfUserOneProjectTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test if user has only one project
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = getAllProjects(userId);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "[{\"id\":" + projectId + ",\"projectname\":\"project\",\"userId\":" + userId +"}]";
           JSONAssert.assertEquals(expected, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           // delete user and project
           response = deleteProject(userId, projectId);
           response.close();

           response = deleteUser(userId);
           response.close();
       } finally {
           httpclient.close();
       }
   }

   @Test
   public void getAllProjectsOfUserMultipleProjectsTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test if user has multiple projects
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project1");
           String projectId1 = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project2");
           String projectId2 = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project3");
           String projectId3 = getIdFromResponse(response);
           response.close();

           response = getAllProjects(userId);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "[{\"id\":" + projectId1 + ",\"projectname\":\"project1\",\"userId\":" + userId +"},{\"id\":" + projectId2 + ",\"projectname\":\"project2\",\"userId\":" + userId +"},{\"id\":" + projectId3 + ",\"projectname\":\"project3\",\"userId\":" + userId +"}]";
           JSONAssert.assertEquals(expected, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();


           // delete user and project
           response = deleteProject(userId, projectId1);
           response.close();
           response = deleteProject(userId, projectId2);
           response.close();
           response = deleteProject(userId, projectId3);
           response.close();

           response = deleteUser(userId);
           response.close();
       } finally {
           httpclient.close();
       }
   }

   // GET /users/{userId}/projects/{projectId} by Jiaying He
   @Test
   public void getProjectBadRequestTest1() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           // test for badrequest: Invalid userId
           CloseableHttpResponse response = getProject("invalidId", "0");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "Bad request";
           assertEquals("actual: " + strResponse + ", expect: " + expected, expected, strResponse);
           EntityUtils.consume(response.getEntity());
           response.close();
       } finally {
           httpclient.close();
       }
   }
//
   // PUT /users/{userId}/projects/{projectId} by Lee Sun
   // Case 3: Unsuccessful modification - Invalid user
   @Test
   public void putProjectsInvalidUserTest() throws Exception {
       try {
           deleteAllUsers();
           // test for badrequest: Invalid UserId
           CloseableHttpResponse response = addUser("putProject", "One", "pprj1@example.com");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "Project1");
           String projectId = getIdFromResponse(response);
           response.close();

           response = updateProject("-1", projectId, "Project1Mod");
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "User not found";
           assertEquals(strResponse, expected);
           EntityUtils.consume(response.getEntity());
           response.close();

           // Check project object content
           response = getProject(userId, projectId);
           entity = response.getEntity();
           strResponse = EntityUtils.toString(entity);
           expected = "{\"id\":" + projectId + ",\"projectname\":\"Project1\",\"userId\":" + userId +"}";
           JSONAssert.assertEquals(expected,strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

           response = deleteUser(userId);
           response.close();
       } finally {
           httpclient.close();
       }
   }
   

    // DELETE /users/{userId}/projects/{projectId} by Lee Sun
    // Case 1: Successful deletion
    @Test
    public void deleteProjectSuccessTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // Create user to hold project
            CloseableHttpResponse response = addUser("deleteProject", "One", "dltprj1@example.com");
            String userId = getIdFromResponse(response);
            response.close();

            // Create Project for User
            response = addProject(userId, "Project1");
            String projectId = getIdFromResponse(response);
            response.close();

            // Delete project
            response = deleteProject(userId, projectId);

            // Check response code
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
            }
            String strResponse = EntityUtils.toString(entity);

            // Check project object content
            String expected = "{\"id\":" + projectId + ",\"projectname\":\"Project1\",\"userId\":" + userId +"}";
            JSONAssert.assertEquals(expected,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Check project is deleted
            response = getProject(userId, projectId);
            status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                System.out.println(
                        "*** Correct! Expected response status: 404 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());
            response.close();

            response = deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // DELETE /users/{userId}/projects/{projectId} by Lee Sun
    // Case 2: Unsuccessful deletion - Invalid User
    @Test
    public void deleteProjectInvalidUserTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // Create user to hold project
            CloseableHttpResponse response = addUser("deleteProject", "One", "dltprj1@example.com");
            String userId = getIdFromResponse(response);
            response.close();

            // Create Project for User
            response = addProject(userId, "Project1");
            String projectId = getIdFromResponse(response);
            response.close();

            // Issue delete for invalid user
            response = deleteProject("-1", projectId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 404) {
                System.out.println(
                        "*** Correct! Expected response status: 404 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
            }

            EntityUtils.consume(response.getEntity());
            response.close();

            // Check that project is still there
            response = getProject(userId, projectId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
            }
            String strResponse = EntityUtils.toString(entity);
            String expected = "{\"id\":" + projectId + ",\"projectname\":\"Project1\",\"userId\":" + userId +"}";
            JSONAssert.assertEquals(expected,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();


            // delete user and project
            response = deleteProject(userId, projectId);
            response.close();

            response = deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // DELETE /users/{userId}/projects/{projectId} by Lee Sun
    // Case 3: Unsuccessful deletion - Invalid Project
    @Test
    public void deleteProjectInvalidProjectTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // Create user to hold project
            CloseableHttpResponse response = addUser("deleteProject", "One", "dltprj1@example.com");
            String userId = getIdFromResponse(response);
            response.close();

            // Create Project for User
            response = addProject(userId, "Project1");
            String projectId = getIdFromResponse(response);
            response.close();
            response = deleteProject(userId, "-1");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 404) {
                System.out.println(
                        "*** Correct! Expected response status: 404 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
            }

            EntityUtils.consume(response.getEntity());
            response.close();

            // Check that project is still there
            response = getProject(userId, projectId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
            }
            String strResponse = EntityUtils.toString(entity);
            String expected = "{\"id\":" + projectId + ",\"projectname\":\"Project1\",\"userId\":" + userId +"}";
            JSONAssert.assertEquals(expected,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();


            // delete user and project
            response = deleteProject(userId, projectId);
            response.close();

            response = deleteUser(userId);
            response.close();
        } finally {

            httpclient.close();
        }
    }

    // DELETE /users/{userId}/projects/{projectId} by Lee Sun
    // Case 4: Unsuccessful deletion - Invalid Request
    @Test
    public void deleteProjectInvalidRequestTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            // Create user to hold project
            CloseableHttpResponse response = addUser("deleteProject", "One", "dltprj1@example.com");
            String userId = getIdFromResponse(response);
            response.close();

            // Create Project for User
            response = addProject(userId, "Project1");
            String projectId = getIdFromResponse(response);
            response.close();

            // Delete project
            response = invalidDeleteProject(userId, "abc");

            // Check response code
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 400) {
                System.out.println(
                        "*** Correct! Expected response status: 400 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();

            // Check project is not deleted
            response = getProject(userId, projectId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            response = deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

//	/* SESSION */

    // POST /users/{userId}/projects/{projectId}/sessions - yizhang
    // case 1: valid userId and projectId (201)
    @Test
    public void postProjectSessionAddSuccessTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            CloseableHttpResponse response = addUser("firstname", "lastname", "email");
            String userId = getIdFromResponse(response);
            response.close();

            response = addProject(userId, "project");
            String projectId = getIdFromResponse(response);
            response.close();

            response = addSession(userId, projectId);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            String sessionId = getIdFromStringResponse(strResponse);
            String expectedJson = "{\"id\":" + sessionId + ",\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
                    "\"endTime\":\"" + "2019-02-18T21:00Z" + "\", " +
                    "\"counter\":" + 1 + "}";

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = deleteUser(userId);
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // POST /users/{userId}/projects/{projectId}/sessions - yizhang
    // case 2: 400
    @Test
    public void postProjectSessionBadRequestTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            CloseableHttpResponse response = addSession("userId", "projectId"); //string input - bad

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 400) {
                System.out.println(
                        "*** Correct! Expected response status: 400 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }
    //POST /users/{userId}/projects/{projectId}/sessions - yizhang
    // case 3: invalid userId or invalid projectId (404)
    @Test
    public void postProjectSessionInvalidTest() throws Exception {
        httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
        deleteAllUsers();
        try {
            CloseableHttpResponse response = addSession("99999", "99999");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 404) {
                System.out.println(
                        "*** Correct! Expected response status: 404 ***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 404");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

   //POST /users/{userId}/projects/{projectId}/sessions - yizhang
   // case 4: server error (500) ??? Not sure ??? cant be implemented

   // Put /users/{userId}/projects/{projectId}/sessions/{sessionId} - yizhang
   // case 1: valid userId and projectId (200)
   @Test
   public void putProjectSessionUpdateSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           deleteAllUsers();
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = addSession(userId, projectId);
           String sessionId = getIdFromResponse(response);

           response = updateSession(userId, projectId, sessionId);

           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           
           String strResponse = EntityUtils.toString(entity);
           sessionId = getIdFromStringResponse(strResponse);
           expectedJson += "{\"id\":" + sessionId + ",\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
                                             "\"endTime\":\"" + "2019-02-18T22:00Z" + "\", " +
                                             "\"counter\":" + 2 + "}";

           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           JSONAssert.assertEquals(expectedJson, strResponse, false);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // Put /users/{userId}/projects/{projectId}/sessions/{sessionId} - yizhang
   // case 2: bad request (400)
   @Test
   public void putProjectSessionUpdateBadRequestTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       // deleteAllUsers();
       String id = null;
       String expectedJson = "";
       deleteAllUsers();
       try {
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = addSession(userId, projectId);
           String sessionId = getIdFromResponse(response);

           response = updateSession(userId, projectId, "invalid");
           // sessionId = getIdFromResponse(response);

           // expectedJson += "{\"id\":" + sessionId + ",\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
           //                                   "\"endTime\":\"" + "2019-02-18T22:00Z" + "\", " +
           //                                   "\"counter\":" + 2 + "}";
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           String strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // Put /users/{userId}/projects/{projectId}/sessions/{sessionId} - yizhang
   // case 2: Not Found (404)
   @Test
   public void putProjectSessionUpdateNotFoundTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       String id = null;
       String expectedJson = "";
       try {
           CloseableHttpResponse response = updateSession("99999", "999999", "99999");

           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           String strResponse = EntityUtils.toString(entity);
           System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
//    // case: 500: cant be implemented
//
   // Get /users/{userId}/projects/{projectId}/report:
   // case: 200
   @Test
   public void getReportSuccessTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = addSession(userId, projectId);
           String sessionId = getIdFromResponse(response);

           String startTime = "2019-02-18T19:00Z";
           String endTime = "2019-02-18T24:00Z";
           response = getReport(userId, projectId, startTime, endTime, false,false);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 200) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status + "while it should be 200");
           }
           String strResponse = EntityUtils.toString(entity);
           String expected = "{\"sessions\": [{\"startingTime\":\"2019-02-18T20:00Z\", \"endingTime\": \"2019-02-18T21:00Z\", \"hoursWorked\": 1}], \"completedPomodoros\": 1, \"totalHoursWorkedOnProject\": 1}";
           JSONAssert.assertEquals(strResponse,expected,false);
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // Get /users/{userId}/projects/{projectId}/report:
   // case: 400
	@Test
   public void getReportBadRequestTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = addSession(userId, projectId);
           String sessionId = getIdFromResponse(response);

           String startTime = "2019-02-18T19:00Z";
           String endTime = "2019-02-18T24:00Z";
           response = getReport("invalid", "invalid", startTime, endTime,false,false);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 400) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
//
   // Get /users/{userId}/projects/{projectId}/report:
   // case: 404
	@Test
   public void getReportNotFoundTest() throws Exception {
       httpclient = HttpClients.createDefault(); // Creates CloseableHttpClient instance with default configuration.
       deleteAllUsers();
       try {
           CloseableHttpResponse response = addUser("firstname", "lastname", "email");
           String userId = getIdFromResponse(response);
           response.close();

           response = addProject(userId, "project");
           String projectId = getIdFromResponse(response);
           response.close();

           response = addSession(userId, projectId);
           String sessionId = getIdFromResponse(response);

           String startTime = "2019-02-18T19:00Z";
           String endTime = "2019-02-18T24:00Z";
           response = getReport("99999999", "999999999", startTime, endTime,false,false);
           int status = response.getStatusLine().getStatusCode();
           HttpEntity entity;
           if (status == 404) {
               entity = response.getEntity();
           } else {
               throw new ClientProtocolException("Unexpected response status: " + status);
           }
           
           EntityUtils.consume(response.getEntity());
           response.close();

       } finally {
           httpclient.close();
       }
   }
    
    
    /*  helper functions */
	// User Functions
    private CloseableHttpResponse addUser(String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"firstName\":\"" + firstname + "\"," +
                        "\"lastName\":\"" + lastname + "\"," +
                        "\"email\":\"" + email + "\"}"
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse addBadUser(String firstName, String lastName, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{[[[[[\"firstName123\":\"" + firstName + "\"," +
                "\"lastName\":\"" + lastName + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

	private CloseableHttpResponse updateUser(String id, String firstName, String lastName, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
		        "\"firstName\":\"" + firstName + "\"," +
                "\"lastName\":\"" + lastName + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse invalidUpdateUser(String id, int firstName, String lastName, String email) throws IOException {
        // firstName changed to int to break update schema
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"firstName\":\"" + firstName + "\"," +
                "\"lastName\":\"" + lastName + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getAllUsers() throws IOException {

        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");

        return response;
    }

    private CloseableHttpResponse getUserbyId(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private void deleteAllUsers() throws IOException {
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
    
    private CloseableHttpResponse deleteUser(String userId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId);
        httpDelete.addHeader("accept", "application/json");
        
        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse invalidDeleteUser(int userId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }
	
	// Project Functions
    private CloseableHttpResponse addProject(String userId, String projectname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"userId\":" + userId + "," +
                "\"projectname\":\"" + projectname + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

	private CloseableHttpResponse addProjectFormat(String userId, String projectname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"userId\":" + userId + "," +
                                              "\"projectname\":" + projectname + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
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

	private CloseableHttpResponse getProject(String userId, String projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }
	
	private CloseableHttpResponse deleteProject(String userId, String projectId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpDelete.addHeader("accept", "application/json");
        
        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse invalidDeleteProject(String userId, String projectId) throws IOException {
        // Breaks project schema with int project id
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

	private CloseableHttpResponse updateProject(String id, String projectId, String projectname) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"projectname\":\"" + projectname + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse invalidUpdateProject(String id, String projectId, int projectname) throws IOException {
        // Breaks project schema with int project name
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"projectname\":\"" + projectname + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }
	
	// helper function for session related yizhang 
    private CloseableHttpResponse addSession(String userId, String projectId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
                                              "\"endTime\":\"" + "2019-02-18T21:00Z" + "\", " + 
                                              "\"counter\":" + 1 + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateSession(String userId, String projectId, String sessionId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
                                              "\"endTime\":\"" + "2019-02-18T22:00Z" + "\", " + 
                                              "\"counter\":" + 2 + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getReport(String userId, String projectId, String fromTime, String toTime, boolean includePomo, boolean includeHours) throws IOException {
        HttpGet httpRequest = null;
        if(!includeHours && !includePomo){
            httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report" + "?from=" + fromTime + "&to=" + toTime + "&includeCompletedPomodoros=false" + "&includeTotalHoursWorkedOnProject=false");
        }else if(includePomo && !includeHours){
            httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report" + "?from=" + fromTime + "&to=" + toTime + "&includeCompletedPomodoros=true" + "&includeTotalHoursWorkedOnProject=false");
        }else if(!includePomo && includeHours){
            httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report" + "?from=" + fromTime + "&to=" + toTime + "&includeCompletedPomodoros=false" + "&includeTotalHoursWorkedOnProject=true");
        }else{
            httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report" + "?from=" + fromTime + "&to=" + toTime + "&includeCompletedPomodoros=true" + "&includeTotalHoursWorkedOnProject=true");
        }
        httpRequest.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

	/* JSON/HTTP Helper Functions */
    private String getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        String id = getIdFromStringResponse(strResponse);
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
	
	/* Template Functions */
	// TODO - Delete before submission
    // private CloseableHttpResponse createContact(String firstname, String familyname, String phonenumber, String email) throws IOException {
    //     HttpPost httpRequest = new HttpPost(baseUrl + "/api/contacts");
    //     httpRequest.addHeader("accept", "application/json");
    //     StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
    //             "\"familyname\":\"" + familyname + "\"," +
    //             "\"phonenumber\":\"" + phonenumber + "\"," +
    //             "\"email\":\"" + email + "\"}");
    //     input.setContentType("application/json");
    //     httpRequest.setEntity(input);

    //     System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpRequest);
    //     System.out.println("*** Raw response " + response + "***");
    //     return response;
    // }

    // private CloseableHttpResponse updateContact(String id, String firstname, String familyname, String phonenumber, String email) throws IOException {
    //     HttpPut httpRequest = new HttpPut(baseUrl + "/api/contacts/" + id);
    //     httpRequest.addHeader("accept", "application/json");
    //     StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
    //             "\"familyname\":\"" + familyname + "\"," +
    //             "\"phonenumber\":\"" + phonenumber + "\"," +
    //             "\"email\":\"" + email + "\"}");
    //     input.setContentType("application/json");
    //     httpRequest.setEntity(input);

    //     System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpRequest);
    //     System.out.println("*** Raw response " + response + "***");
    //     return response;
    // }

    // private CloseableHttpResponse getContact(String id) throws IOException {
    //     HttpGet httpRequest = new HttpGet(baseUrl + "/api/contacts/" + id);
    //     httpRequest.addHeader("accept", "application/json");

    //     System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpRequest);
    //     System.out.println("*** Raw response " + response + "***");
    //     return response;
    // }

    // private CloseableHttpResponse getAllContacts() throws IOException {
    //     HttpGet httpRequest = new HttpGet(baseUrl + "/api/contacts");
    //     httpRequest.addHeader("accept", "application/json");

    //     System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpRequest);
    //     System.out.println("*** Raw response " + response + "***");
    //     return response;
    // }

    // private CloseableHttpResponse deleteContact(String id) throws IOException {
    //     HttpDelete httpDelete = new HttpDelete(baseUrl + "/api/contacts/" + id);
    //     httpDelete.addHeader("accept", "application/json");

    //     System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpDelete);
    //     System.out.println("*** Raw response " + response + "***");
    //     // EntityUtils.consume(response.getEntity());
    //     // response.close();
    //     return response;
    // }

    // private CloseableHttpResponse deleteContacts() throws IOException {
	// HttpDelete httpDelete = new HttpDelete(baseUrl + "/api/contacts");
    //     httpDelete.addHeader("accept", "application/json");

    //     System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpDelete);
    //     System.out.println("*** Raw response " + response + "***");
    //     // EntityUtils.consume(response.getEntity());
    //     // response.close();
    //     return response;
    // }

}
