package edu.gatech.cs6301;
import java.util.*;
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

public class BackendTestsWeb2 {

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

    //Test Cases for Users------------------------------------------------------------------------------------------
    private void POSTTest(Map<String, Object> inputBody, int expectedStatus, Map<String, Object> expectedResponse, boolean strict) throws Exception{
        CloseableHttpResponse response  = createUser(inputBody);
        int status = response.getStatusLine().getStatusCode();

        if (status == expectedStatus) {
            HttpEntity entity = response.getEntity();
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            if (expectedStatus == 201) {
                expectedResponse.put("id", Long.parseLong(getIdFromStringResponse(strResponse)));
                JSONObject json = new JSONObject(expectedResponse);
                JSONAssert.assertEquals(json.toString() ,strResponse, strict);
            } else {
                // // shouldnt be comaring response for an invalid request
                // if (!"".equals(strResponse)){
                //     throw new ClientProtocolException("Unexpected response body: " + strResponse);
                // }
            }   

        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        EntityUtils.consume(response.getEntity());
        response.close();
    }

    @Test
    public void createUserTest() throws Exception {
        deleteUsers();
        try {
            // POST //  Covered user cases 1.1
            Map<String, Object> inputBody = new HashMap<String, Object>();
            inputBody.put( "firstName", "John" );
            inputBody.put( "lastName", "Doe" );
            inputBody.put( "email", "john@doe.org" );
            Map<String, Object> expectedResponse = new HashMap<String, Object>(inputBody);
            POSTTest(inputBody, 201, expectedResponse, false);

            // POST Duplicate //  Covered user cases 1.2 
            expectedResponse = new HashMap<String, Object>();
            POSTTest(inputBody, 409, expectedResponse, false);

            // POST Invalid Input
            inputBody = new HashMap<String, Object>();
            inputBody.put( "lastName", "Doe" );
            POSTTest(inputBody, 400, expectedResponse, false);

        } finally {
            httpclient.close();
        }
    }

    private void PUTTest(String id, Map<String, Object> inputBody, int expectedStatus, Map<String, Object> expectedResponse, boolean strict) throws Exception{
        CloseableHttpResponse response  = updateUser(id, inputBody);
        int status = response.getStatusLine().getStatusCode();

        if (status == expectedStatus) {
            HttpEntity entity = response.getEntity();
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            if (expectedStatus != 200){
                // // shouldnt be comaring response for an invalid request
                // if (!"".equals(strResponse)){
                //     throw new ClientProtocolException("Unexpected response body: " + strResponse);
                // }
                
            } else {
                expectedResponse.put("id", Long.parseLong(getIdFromStringResponse(strResponse)));
                JSONObject json = new JSONObject(expectedResponse);
                JSONAssert.assertEquals(json.toString() ,strResponse, strict);
            }   

        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        EntityUtils.consume(response.getEntity());
        response.close();
    }


    @Test
    public void updateUserTest() throws Exception {
        deleteUsers();

        try {
            //Create User
            Map<String, Object> inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "John");
            inputBody.put("lastName", "Doe");
            inputBody.put("email", "john@doe.org");

            CloseableHttpResponse response = createUser(inputBody);
            String id = getIdFromResponse(response);
            response.close();

            // PUT //Covered usercases 2
            inputBody.put("firstName", "Tom");
            Map<String, Object> expectedResponse = new HashMap<String, Object>(inputBody);
            inputBody.put("id", "0");
            expectedResponse.put("id", Long.parseLong(id));
            PUTTest(id, inputBody, 200, expectedResponse, false);

            // PUT Id doesn't exist
            expectedResponse = new HashMap<String, Object>();
            PUTTest(id + "1", inputBody, 404, expectedResponse, false);

            //PUT Valid Partial Modify Input
            inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "John");
            PUTTest(id, inputBody, 200, expectedResponse, false);

            //PUT modify email: 
            //  User story say we only want to be able to edit user's first and last name.
            //  if changed email, it won't change the email
            inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "Tom");
            inputBody.put("lastName", "Doe");
            inputBody.put("email", "tom@doe.org");
            inputBody.put("id", "0");
            expectedResponse = new HashMap<String, Object>(inputBody);
            expectedResponse.put("email", "john@doe.org");
            PUTTest(id, inputBody, 200, expectedResponse, false);

        } finally {
            httpclient.close();
        }
    }

    private void GETTest(String id, int expectedStatus, Map<String, Object> expectedResponse, boolean strict) throws Exception{
        CloseableHttpResponse response  = getUser(id);
        int status = response.getStatusLine().getStatusCode();

        if (status == expectedStatus) {
            HttpEntity entity = response.getEntity();
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            if (expectedStatus == 200){
                JSONObject json = new JSONObject(expectedResponse);
                JSONAssert.assertEquals(json.toString() ,strResponse, strict);
            } else {
                // // shouldnt be comaring response for an invalid request
                // if (!"".equals(strResponse)){
                //     throw new ClientProtocolException("Unexpected response body: " + strResponse);
                // }
            }   

        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        EntityUtils.consume(response.getEntity());
        response.close();
    }

    @Test
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            //Create User
            Map<String, Object> inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "John");
            inputBody.put("lastName", "Doe");
            inputBody.put("email", "john@doe.org");

            CloseableHttpResponse response = createUser(inputBody);
            String id = getIdFromResponse(response);
            response.close();

            // GET Single
            Map<String, Object> expectedResponse = new HashMap<String, Object>(inputBody);
            expectedResponse.put("id", Long.parseLong(id));
            GETTest(id, 200, expectedResponse, false);

            // GET Id doesn't exist
            expectedResponse = new HashMap<String, Object>();
            GETTest(id + "1999999", 404, expectedResponse, false);

            // GET Id that is not legitimate
            expectedResponse = new HashMap<String, Object>();
            GETTest(id + "abc", 400, expectedResponse, false);

        } finally {
            httpclient.close();
        }
    }

    private void GETallTest(int expectedStatus, ArrayList<Object> expectedResponse, boolean strict) throws Exception{
        CloseableHttpResponse response  = getAllUsers();
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (status == expectedStatus) {
            String strResponse = EntityUtils.toString(entity);
                    System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            if (expectedStatus == 200){
                JSONAssert.assertEquals(expectedResponse.toString(), strResponse, strict);
            } else {
                // // shouldnt be comaring response for an invalid request
            }

        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        EntityUtils.consume(response.getEntity());
        response.close();
    }

    @Test
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            //Create User
            Map<String, Object> inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "John");
            inputBody.put("lastName", "Doe");
            inputBody.put("email", "john@doe.org");

            CloseableHttpResponse response = createUser(inputBody);
            String id = getIdFromResponse(response);
            inputBody.put("id", Long.parseLong(id));
            response.close();

            Map<String, Object> inputBody2 = new HashMap<String, Object>();
            inputBody2.put("firstName", "Jane");
            inputBody2.put("lastName", "Wall");
            inputBody2.put("email", "jane@wall.org");

            response = createUser(inputBody2);
            String id2 = getIdFromResponse(response);
            inputBody2.put("id", Long.parseLong(id2));
            response.close();

            // Get All Users
            ArrayList<Object> expectedResponse = new ArrayList<Object>();
            expectedResponse.add(inputBody);
            expectedResponse.add(inputBody2);

            GETallTest(200, expectedResponse, false);

        } finally {
            httpclient.close();
        }
    }

    private void DELETETest(String id, int expectedStatus, Map<String, Object> expectedResponse, boolean strict) throws Exception{
        CloseableHttpResponse response  = deleteUser(id);
        int status = response.getStatusLine().getStatusCode();

        if (status == expectedStatus) {
            HttpEntity entity = response.getEntity();
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            if (expectedStatus == 200){
                JSONObject json = new JSONObject(expectedResponse);
                JSONAssert.assertEquals(json.toString(), strResponse, strict);
            }   

        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        EntityUtils.consume(response.getEntity());
        response.close();
    }

    @Test
    public void DeleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            //Create User
            Map<String, Object> inputBody = new HashMap<String, Object>();
            inputBody.put("firstName", "John");
            inputBody.put("lastName", "Doe");
            inputBody.put("email", "john@doe.org");

            CloseableHttpResponse response = createUser(inputBody);
            String id = getIdFromResponse(response);
            response.close();

            // Delete and check if it still exists // Covered user cases 3.2.1
            Map<String, Object> expectedResponse = new HashMap<String, Object>(inputBody);
            expectedResponse.put("id", Long.parseLong(id));
            DELETETest(id, 200, expectedResponse, false);

            response = getAllUsers();
            HttpEntity entity;
            String expectedJson;
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Delete id that doesn't exist //Covered user cases 3.1
            expectedResponse = new HashMap<String, Object>();
            DELETETest(id + "1", 404, expectedResponse, false);

            // Delete an user with projects// Covered user cases 3.2.2
            response = createUser(inputBody);
            String userId = getIdFromResponse(response);
            response.close();
            response = createProject("testProjectName", userId);
            response.close();
            expectedResponse = new HashMap<String, Object>(inputBody);
            expectedResponse.put("id", Long.parseLong(userId));
            DELETETest(userId, 200, expectedResponse, false);

        } finally {
            httpclient.close();
        }
    }
    // //Test Cases for Porjects---------------------------------------------------------------------------------------
     // To test code 201 and id, create project successfully.
     @Test
     public void createProjectTest() throws Exception {
         //set up user
         deleteUsers();
         String userId = createTestUser();
	     //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

         try {
             // Covered user cases 5.1
             CloseableHttpResponse response = createProject("testProjectName", userId);

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

             String expectedJson = "{\"id\":" + id + ",\"projectname\":\"testProjectName\",\"userId\":" + userId + "}";
	     JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

             // create duplicate name project // Covered user cases 5.2
             response = createProject("testProjectName", userId);
             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(409, status);
             EntityUtils.consume(response.getEntity());
             response.close();

             // 400 bad request
             response = createProject("testProjectName1", userId + "abc");
             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);
             EntityUtils.consume(response.getEntity());
             response.close();
             // 404 user not found
             response = createProject("testProjectName2", userId + "666");
             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);
             EntityUtils.consume(response.getEntity());
             response.close();

         } finally {
             httpclient.close();
         }
     }

    // // To test code 200 and id, update project successfully.
    // @Test
    // public void updateProjectTest() throws Exception {
    //     //set up user
    //     deleteUsers();
    //     String userId = createTestUser();

    //     //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

    //     try {
    //         CloseableHttpResponse response = createProject("testProjectName", userId);
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         response = updateProject(id, "newProjectName", userId);

    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"newProjectName\",\"userId\":\"" + userId + "\"}";
	//     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // 400 bad request
    //         response = updateProject(id, "newProjectName", userId + "abc");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         // 404 User or project not found
    //         response = updateProject(id, "newProjectName", userId + "666");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = updateProject(id, "newProjectName666", userId);
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // To test code 200 and id, get one project successfully.
    @Test
    public void getProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //set up user
        deleteUsers();

        String userId = createTestUser();

        //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

        try {
            CloseableHttpResponse response = createProject("testProjectName", userId);
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = getProject(userId, id);

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

            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"testProjectName\",\"userId\":" + userId + "}";
	        JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // 400 bad request
            response = getProject(userId + "abc", id);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);
            EntityUtils.consume(response.getEntity());
            response.close();
            // 404 User not found
            response = getProject(userId + "666", id);
            status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);
            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    // // To test code 200 and id, get all projects successfully.
    // @Test
    // public void getAllProjectsTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     //set up user
    //     deleteUsers();

    //     String userId = createTestUser();

    //     //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

    //     String id = null;
    //     String expectedJson = "";

    //     try {
    //         CloseableHttpResponse response = createProject("testProjectName1", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson += "[{\"id\":\"" + id + "\",\"projectname\":\"testProjectName1\",\"userId\":\"" + userId + "\"}";
    //         response.close();

    //         response = createProject("testProjectName2", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson += ",{\"id\":\"" + id + "\",\"projectname\":\"testProjectName2\",\"userId\":\"" + userId + "\"}]";
    //         response.close();

    //         response = getAllProjects(userId);

    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

	//     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // 400 bad request
    //         response = getAllProjects(userId + "abc");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         // 404 User or project not found
    //         response = getAllProjects(userId + "666");
    //         status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // // To test code 200 and id, delete one project for a user successfully.
     @Test
     public void DeleteProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         //set up user
         deleteUsers();

         String userId = createTestUser();

         //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();
         String expectedJson = null;

         try {
             CloseableHttpResponse response = createProject("testProjectName", userId);
             // EntityUtils.consume(response.getEntity());
             String deleteid = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;
            

             //Covered user cases 6.2.1
             response = deleteProject(userId, deleteid);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             expectedJson = "{\"id\":" + deleteid + ",\"projectname\":\"testProjectName\",\"userId\":" + userId + "}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

             // // TODO: uncomment after implementing getAllProjects which is for the next deliverable
//             response = getAllProjects(userId);
//             status = response.getStatusLine().getStatusCode();
//             if (status == 200) {
//                 entity = response.getEntity();
//             } else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);
//
//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//
//             expectedJson = "[]";
//	     JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();

//             //delete project with Session connected// Covered user cases 6.2.2
//             response = createProject("testProjectName", userId);
//             deleteid = getIdFromResponse(response);
//             response.close();
//             response = createSession(userId, deleteid, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "0");
//             response.close();
//
//             status = response.getStatusLine().getStatusCode();
//             if (status == 200) {
//                 entity = response.getEntity();
//             } else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);
//
//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//
//             expectedJson = "{\"id\":\"" + deleteid + "\",\"projectname\":\"testProjectName\",\"userId\":\"" + userId + "\"}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//
//             response = getAllProjects(userId);
//             status = response.getStatusLine().getStatusCode();
//             if (status == 200) {
//                 entity = response.getEntity();
//             } else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);
//
//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//
//             expectedJson = "[]";
//         JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();

             // 400 bad request
             response = createProject("testProjectName", userId);
             deleteid = getIdFromResponse(response);
             response.close();
             response = deleteProject(userId + "abc", deleteid);
             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);
             EntityUtils.consume(response.getEntity());
             response.close();
             // 404 User or project not found
             response = createProject("testProjectName2", userId);
             deleteid = getIdFromResponse(response);
             response.close();
             response = deleteProject(userId + "666", deleteid);
             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);
             EntityUtils.consume(response.getEntity());
             response.close();

         } finally {
             httpclient.close();
         }
     }
    // // ok
     @Test
     public void CreateMultipleDeleteOneProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         //set up user
         deleteUsers();

         String userId = createTestUser();

         //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();
         String expectedJson = "";

         try {
             CloseableHttpResponse response = createProject("testProjectName1", userId);
             // EntityUtils.consume(response.getEntity());
             String deleteId = getIdFromResponse(response);
             response.close();

             response = createProject("testProjectName2", userId);
             // EntityUtils.consume(response.getEntity());
             String id = getIdFromResponse(response);
             expectedJson += "[{\"id\":" + id + ",\"projectname\":\"testProjectName2\",\"userId\":" + userId + "}]";
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject(userId, deleteId);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             String expectedJson2 = "{\"id\":" + deleteId + ",\"projectname\":\"testProjectName1\",\"userId\":" + userId + "}";
             JSONAssert.assertEquals(expectedJson2,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

             // // TODO: uncomment after implementing getAllProjects which is for the next deliverable
//             response = getAllProjects(userId);
//             status = response.getStatusLine().getStatusCode();
//             if (status == 200) {
//                 entity = response.getEntity();
//             } else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);
//
//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//
//             // expectedJson = "[]";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
         } finally {
             httpclient.close();
         }
     }

    // // ok
    // @Test
    // public void CreateMultipleUpdateOneProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
        
    //     deleteUsers();
        
    //     String userId = createTestUser();

    //     //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

    //     try {
    //         CloseableHttpResponse response = createProject("testProjectName1", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         response = createProject("testProjectName2", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         String updatedId = getIdFromResponse(response);
    //         response.close();

    //         int status;
    //         HttpEntity entity;
    //         String strResponse;

    //         response = updateProject(updatedId, "testProjectName3", userId);
    //         String expectedJson = "{\"id\":\"" + updatedId + "\",\"projectname\":\"testProjectName3\",\"userId\":\"" + userId + "\"}";

    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         // Check that the record is correct in the response
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = getProject(userId, updatedId);

    //         status = response.getStatusLine().getStatusCode();
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         // Check that the record was correctly updated in the addressbook
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // To test code 404
    @Test
    public void getMissingProjectTest() throws Exception {// Covered user cases 6.1
        httpclient = HttpClients.createDefault();

        deleteUsers();
        
        String userId = createTestUser();

        //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

        try {
            CloseableHttpResponse response = createProject("testProjectName1", userId);
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject("testProjectName2", userId);
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "1234" + id1 + id2; // making sure the ID is not present

            response = getProject(userId, missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // // To test code 404
     @Test
     public void deleteMissingProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
        
         deleteUsers();
        
         String userId = createTestUser();

         //deleteProjects(userId);//all projects have already been deleted by server since deleteUsers();

         try {
             CloseableHttpResponse response = createProject("testProjectName1", userId);
             // EntityUtils.consume(response.getEntity());
             String id1 = getIdFromResponse(response);
             response.close();

             response = createProject("testProjectName2", userId);
             // EntityUtils.consume(response.getEntity());
             String id2 = getIdFromResponse(response);
             response.close();

             String missingId = id1 + id2; // making sure the ID is not present

             response = deleteProject(userId, missingId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }


    // // Test cases for Session and Report-----------------------------------------------------------
    // @Test
    // public void createSessionTest() throws Exception {

    //     try {
    //         //create user & project first
    //         deleteUsers();
    //         String userId = createTestUser();
    //         //created user
    //         CloseableHttpResponse response = createProject("projectname", userId);
    //         String projectid = getIdFromResponse(response);//getIdFromResponse hasn't been implimented in this part
    //         response.close();
    //         //created project
    //         response = createSession(userId, projectid, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "0");

    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if (status == 201) {// Covered user cases 7
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         String strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         String id = getIdFromStringResponse(strResponse);

    //         String expectedJson = "{\"id\":\"" + id + "\",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:00Z\",\"count\":\"0\"}";
    //     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //test status 400
    //         response = createSession(userId, projectid, "invalid", "invalid", "0");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         //test status 404
    //         response = createSession(userId + "1", projectid, "2019-02-18T21:00Z", "2019-02-18T21:00Z", "0");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 404){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = createSession(userId, projectid + "1", "2019-02-18T21:00Z", "2019-02-18T21:00Z", "0");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 404){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }


    @Test
    public void updateSessionTest() throws Exception {

        try {
            //create user & project first
            deleteUsers();
            String userId = createTestUser();
            //created user
            CloseableHttpResponse response = createProject("projectname", userId);
            String projectid = getIdFromResponse(response);//getIdFromResponse hasn't been implimented in this part
            response.close();
            //created project
            response = createSession(userId, projectid, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "0");
            String id = getIdFromResponse(response);
            response.close();

            response = updateSession(id, userId, projectid, "2019-02-18T20:00Z", "2019-02-18T20:30Z", "1");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {// Covered user cases 8 & 10.2
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:30Z\",\"counter\":0}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            //test status 400
            response = updateSession(id, userId, projectid, "invalid", "invalid", "0");
            status = response.getStatusLine().getStatusCode();
            if(status != 400){
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());
            response.close();
            //test status 404
            response = updateSession(id + "1", userId, projectid, "2019-02-18T21:00Z", "2019-02-18T21:00Z", "0");
            status = response.getStatusLine().getStatusCode();
            if(status != 404){
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());
            response.close();

            response = updateSession(id, userId + "1", projectid, "2019-02-18T21:00Z", "2019-02-18T21:00Z", "0");
            status = response.getStatusLine().getStatusCode();
            if(status != 404){
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());
            response.close();

            response = updateSession(id, userId, projectid + "1", "2019-02-18T21:00Z", "2019-02-18T21:00Z", "0");
            status = response.getStatusLine().getStatusCode();
            if(status != 404){
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // @Test
    // public void getReportTest() throws Exception {// Covered user cases 12
    //     httpclient = HttpClients.createDefault();
    //     //set up user
    //     deleteUsers();
    //     String userId = createTestUser();
    //     //created user
    //     CloseableHttpResponse response = createProject("projectname", userId);
    //     String projectid = getIdFromResponse(response);//getIdFromResponse hasn't been implimented in this part
    //     response.close();
    //     //created project
    //     response = createSession(userId, projectid, "2019-02-18T20:00Z", "2019-02-18T20:30Z", "1");
    //     response.close();
    //     response = createSession(userId, projectid, "2019-02-18T21:00Z", "2019-02-18T21:30Z", "1");
    //     response.close();

    //     try {
    //         //case 1
    //         response = createReport(userId, projectid, "2019-02-18T20:10Z", "2019-02-18T21:20Z", false, false);
            
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         String expectedJson = "{\"sessions\":\"[{\"startingTime\":\"2019-02-18T20:00Z\",\"endingTime\":\"2019-02-18T20:30Z\",\"hoursWorked\":\"0.50\"},{\"startingTime\":\"2019-02-18T21:00Z\",\"endingTime\":\"2019-02-18T21:30Z\",\"hoursWorked\":\"0.50\"}]\",\"completedPomodoros\":\"0\",\"totalHoursWorkedOnProject\":\"0\"}";
    //     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //case 2
    //         response = createReport(userId, projectid, "2019-02-18T20:10Z", "2019-02-18T21:20Z", true, false);
            
    //         status = response.getStatusLine().getStatusCode();

    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"sessions\":\"[{\"startingTime\":\"2019-02-18T20:00Z\",\"endingTime\":\"2019-02-18T20:30Z\",\"hoursWorked\":\"0.50\"},{\"startingTime\":\"2019-02-18T21:00Z\",\"endingTime\":\"2019-02-18T21:30Z\",\"hoursWorked\":\"0.50\"}]\",\"completedPomodoros\":\"2.00\",\"totalHoursWorkedOnProject\":\"0\"}";
    //     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //case 3
    //         response = createReport(userId, projectid, "2019-02-18T20:10Z", "2019-02-18T21:20Z", false, true);
            
    //         status = response.getStatusLine().getStatusCode();

    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"sessions\":\"[{\"startingTime\":\"2019-02-18T20:00Z\",\"endingTime\":\"2019-02-18T20:30Z\",\"hoursWorked\":\"0.50\"},{\"startingTime\":\"2019-02-18T21:00Z\",\"endingTime\":\"2019-02-18T21:30Z\",\"hoursWorked\":\"0.50\"}]\",\"completedPomodoros\":\"0\",\"totalHoursWorkedOnProject\":\"1.00\"}";
    //     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //case 4
    //         response = createReport(userId, projectid, "2019-02-18T20:10Z", "2019-02-18T21:20Z", true, true);
            
    //         status = response.getStatusLine().getStatusCode();

    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);

    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         expectedJson = "{\"sessions\":\"[{\"startingTime\":\"2019-02-18T20:00Z\",\"endingTime\":\"2019-02-18T20:30Z\",\"hoursWorked\":\"0.50\"},{\"startingTime\":\"2019-02-18T21:00Z\",\"endingTime\":\"2019-02-18T21:30Z\",\"hoursWorked\":\"0.50\"}]\",\"completedPomodoros\":\"2.00\",\"totalHoursWorkedOnProject\":\"1.00\"}";
    //     JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //         //test status 400
    //         response = createReport(userId, projectid, "invalid", "invalid", true, true);
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         //test status 404
    //         response = createReport(userId + "1", projectid, "2019-02-18T20:10Z", "2019-02-18T21:20Z", true, true);
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         response = createReport(userId, projectid + "1", "2019-02-18T20:10Z", "2019-02-18T21:20Z", true, true);
    //         status = response.getStatusLine().getStatusCode();
    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // Set Up -------------------------------------------------------------------------------

    private CloseableHttpResponse createUser(Map<String, Object> inputBody) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        inputBody.put("id", "0");
        JSONObject json = new JSONObject(inputBody);
        StringEntity input = new StringEntity(json.toString());
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private String createTestUser() throws IOException {
        //set up user
        String userId = "-1";
        Map<String, Object> inputBody = new HashMap<String, Object>();
        inputBody.put( "id", "0");// the ID field is irrelevant, will be generate by server
        inputBody.put( "firstName", "John" );
        inputBody.put( "lastName", "Doe" );
        inputBody.put( "email", "john@doe.org" );
        CloseableHttpResponse response  = createUser(inputBody);

        try{

        userId = getIdFromResponse(response);

        }catch(Exception e){
            e.printStackTrace();
        }
        response.close();

        return userId;
    }

    private CloseableHttpResponse updateUser(String id, Map<String, Object> inputBody) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");

        JSONObject json = new JSONObject(inputBody);
        StringEntity input = new StringEntity(json.toString());
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getUser(String id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");

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

    private CloseableHttpResponse deleteUser(String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // We don't have this end point actually....//delete all Users
    // private CloseableHttpResponse deleteUsers() throws IOException {
    //     HttpDelete httpDelete = new HttpDelete(baseUrl + "/users");
    //     httpDelete.addHeader("accept", "application/json");

    //     System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
    //     CloseableHttpResponse response = httpclient.execute(httpDelete);
    //     System.out.println("*** Raw response " + response + "***");
    //     return response;
    // }

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

    private CloseableHttpResponse createProject(String projectName, String userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectName + "\"," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateProject(String projectId, String projectName, String userId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectName + "\"," +
                "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

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

    private CloseableHttpResponse getAllProjects(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/");
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

    private CloseableHttpResponse deleteProjects(String userId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/");
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse createSession(String userid, String projectid, String starttime, String endtime, String count) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userid + "/projects/" + projectid + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + starttime + "\"," +
                "\"endTime\":\"" + endtime + "\"," +
                "\"counter\":\"" + count + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateSession(String id, String userid, String projectid, String starttime, String endtime, String count) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userid + "/projects/" + projectid + "/sessions/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + starttime + "\"," +
                "\"endTime\":\"" + endtime + "\"," +
                "\"count\":\"" + count + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createReport(String userId, String projectId, String fromTime, String toTime, boolean includePomo, boolean includeHours) throws IOException {
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

}
