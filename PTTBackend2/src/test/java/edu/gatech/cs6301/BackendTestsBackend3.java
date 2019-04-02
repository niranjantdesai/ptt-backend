package edu.gatech.cs6301;

import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class BackendTestsBackend3 {

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

    /* User related tests -- START*/ 
    
    @Test
    public void createUserTest() throws Exception{
        System.out.println("----- Start testing POST user -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Post user request
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");

            // Response
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;

            // Check response status, if correct get response body
            if(status == 201){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected POST response status: " + status);

            // Convert response body to string (in purpose of comparing)
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + status + ") ***");
            // Get server-side generated id
            String id = getIdFromStringResponse(strResponse);
            // Expected response body
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"Logan\",\"lastName\":\"Superman\",\"email\":\"logansuperman@xxx.com\"}";
            // Compare
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete created user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void createUserBadRequestTest() throws Exception{
        System.out.println("----- Start testing POST user with bad request body -----");
        httpclient = HttpClients.createDefault();

        try{
            // Post user with bad request
            CloseableHttpResponse response = createUserBadRequest();
            int status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void creatUserConflictTest() throws Exception{
        System.out.println("----- Start testing user POST conflict  -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user once
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status1 = response.getStatusLine().getStatusCode();
            String id = getIdFromResponse(response);
            if(status1 != 201) throw new ClientProtocolException("Unexpected POST response status: " + status1);
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Create user twice
            response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status2 = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status2 + " ***");
            if(status2 == 409) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status2 + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete created user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }

    }

    @Test
    public void getUserTest() throws Exception{
        System.out.println("----- Start testing successful user GET -----");
        httpclient = HttpClients.createDefault();

        try {
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            String id = getIdFromResponse(response);
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Get user
            response = getUser(id);
            status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            System.out.println("*** Response code: " + status + " ***");
            if(status == 200) {
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected GET response status: " + status);

            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            String expectedJSON = "{\"id\":" + id + ",\"firstName\":\"Logan\",\"lastName\":\"Superman\",\"email\":\"logansuperman@xxx.com\"}";
            JSONAssert.assertEquals(strResponse, expectedJSON, false);
            //EntityUtils.consume(response.getEntity());
            response.close();

            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingUser() throws Exception{
        System.out.println("----- Start testing GET missing user -----");
        httpclient = HttpClients.createDefault();

        try {
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            String id = getIdFromResponse(response);
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Get user
            response = getUser("521" + id);
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllUserTest() throws Exception{
        System.out.println("----- Start testing GET all users -----");
        httpclient = HttpClients.createDefault();

        String expectedJson = "";
        int status = 0;

        try{
            deleteUsers();
            // Create a user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id1 = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id1 + ",\"firstName\":\"Logan\",\"lastName\":\"Superman\",\"email\":\"logansuperman@xxx.com\"},";
            response.close();

            // Create another user
            response = createUser("No", "More", "nomore@xxx.com");
            status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id2 = getIdFromResponse(response);
            expectedJson += "{\"id\":" + id2 + ",\"firstName\":\"No\",\"lastName\":\"More\",\"email\":\"nomore@xxx.com\"}]";
            response.close();

            // Get all users
            response = getAllUsers();
            status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            System.out.println("*** Response code: " + status + " ***");
            if(status == 200){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected GET response status: " + status);
            String strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            response.close();

            // Delete users
            response = deleteUser(id1);
            response.close();
            response = deleteUser(id2);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserIllegalIdTest() throws Exception{
        System.out.println("----- Start testing GET user with illegal id -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create a user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Get user with illegal id
            response = getUser("ABCDEFGHIJKLMN");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete users
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserTest() throws Exception{
        System.out.println("----- Start testing PUT user -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Update user
            response = updateUser(id, "No", "Superman", "logansuperman@xxx.com");
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"No\",\"lastName\":\"Superman\",\"email\":\"logansuperman@xxx.com\"}";
            status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            System.out.println("*** Response code: " + status + " ***");
            if(status == 200){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected PUT response status: " + status);
            String strResponse = EntityUtils.toString(entity);
            JSONAssert.assertEquals(strResponse, expectedJson, false);
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void updateMissingUserTest() throws Exception{
        System.out.println("----- Start testing PUT user -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Update user
            response = updateUser("541" + id, "No", "Superman", "logansuperman@xxx.com");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserBadRequestTest() throws Exception{
        System.out.println("----- Start testing PUT user with bad request -----");
        httpclient = HttpClients.createDefault();

        try{
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Update user with bad request body
            response = updateUserBadRequest(id);
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if (status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserEmptyIdTest() throws Exception{
        System.out.println("----- Start testing PUT user with empty id -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Update user with empty id
            response = updateUser("", "No", "More", "logansuperman@xxx.com");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserIllegalIdTest() throws Exception{
        System.out.println("----- Start testing PUT user wit illegal id -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Update user with illegal id
            response = updateUser("ABCDEFGHIJKLMN", "No", "More", "logansuperman@xxx.com");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserTest() throws Exception{
        System.out.println("----- Start testing DELETE user -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Delte user
            response = deleteUser(id);
            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"Logan\",\"lastName\":\"Superman\",\"email\":\"logansuperman@xxx.com\"}";
            status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            System.out.println("*** Response code: " + status + " ***");
            if(status == 200){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected DELETE response status: " + status);
            String strResponse = EntityUtils.toString(entity);
            JSONAssert.assertEquals(strResponse, expectedJson, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteMissingUser() throws Exception{
        System.out.println("----- Start testing DELETE missing user -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Delete missing user
            response = deleteUser("541" + id);
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserEmptyIdTest() throws Exception{
        System.out.println("----- Start testing DELETE user with empty id -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Delete user with empty id
            response = deleteUser("");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserIllegalIdTest() throws Exception{
        System.out.println("----- Start testing DELETE user with illegal id -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            if(status != 201) throw new ClientProtocolException("Unexpected POST response status: " + status);
            String id = getIdFromResponse(response);
            response.close();

            // Delete user with illegal id
            response = deleteUser("ABCDEFGHIJKLMN");
            status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            //EntityUtils.consume(response.getEntity());
            response.close();

            // Delete user
            response = deleteUser(id);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    // /* User related test -- END*/
    
     /* Session related test -- START */
     @Test
     public void createSessionTest() throws Exception{
         System.out.println("----- Start testing POST session -----");
         httpclient = HttpClients.createDefault();

         try{
             // 1. Create a user
             CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
             int status = response.getStatusLine().getStatusCode();
            
             if(status != 201)
             	throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);
            
             String userId = getIdFromResponse(response);
             response.close();
            
             // 2. Create a project
             response = createProject(userId, "Project 1");
             status = response.getStatusLine().getStatusCode();
            
             if(status != 201)
             	throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);
            
             String projectId = getIdFromResponse(response);
             response.close();

             // 3. Create a session and associate it to a user and project
             response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
             // 4. Check the response status, if correct get the response body
             status = response.getStatusLine().getStatusCode();
             HttpEntity entity;

             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
            
             // Convert response body to string (in purpose of comparing)
             String strResponse = EntityUtils.toString(entity);
             System.out.println("*** String response " + strResponse + " (" + status + ") ***");
             // Get server-side generated id
             String id = getIdFromStringResponse(strResponse);
             // Expected response body
             String expectedJson = "{\"id\":\"" + id + "\",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:00Z\",\"counter\":1}";
             // Compare
             JSONAssert.assertEquals(expectedJson, strResponse, false);
             //EntityUtils.consume(response.getEntity());
             response.close();

             // Delete project
             response = deleteProject(userId, projectId);
             EntityUtils.consume(response.getEntity());
             response.close();
            
             // Delete created user
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }
    
     @Test
     public void createSessionBadRequestTest() throws Exception{
         System.out.println("----- Start testing POST session for ill-formed request body -----");
         httpclient = HttpClients.createDefault();

         try{
             // Post user with bad request
             CloseableHttpResponse response = createSessionBadRequest();
             int status = response.getStatusLine().getStatusCode();
             System.out.println("*** Response code: " + status + " ***");
             if(status == 400) System.out.println("*** Response code correct ***");
             else System.out.println("*** Response code wrong: " + status + "***");
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }
    
     @Test
     public void createSessionNonExistentUserTest() throws Exception{
         System.out.println("----- Start testing POST session for non-existent user -----");
         httpclient = HttpClients.createDefault();

         try{
             // 1. Post user with non-existent userId = 101
             CloseableHttpResponse response = createSession("101", "projectId", "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
             // 2. Check the response status, if correct get the response body
             int status = response.getStatusLine().getStatusCode();
             System.out.println("*** Response code: " + status + " ***");
             if(status == 404) System.out.println("*** Response code correct ***");
             else System.out.println("*** Response code wrong: " + status + "***");
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }
    
     @Test
     public void createSessionNonExistentProjectTest() throws Exception{
         System.out.println("----- Start testing POST session for non-existent user -----");
         httpclient = HttpClients.createDefault();

         try{
             // 1. Post user with non-existent projectId = 101
             CloseableHttpResponse response = createSession("userId", "101", "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
             // 2. Check the response status, if correct get the response body
             int status = response.getStatusLine().getStatusCode();
             System.out.println("*** Response code: " + status + " ***");
             if(status == 404) System.out.println("*** Response code correct ***");
             else System.out.println("*** Response code wrong: " + status + "***");
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }
    
    @Test
    public void updateSessionTest() throws Exception{
        System.out.println("----- Start testing PUT session -----");
        httpclient = HttpClients.createDefault();

        try{
            deleteUsers();
            // 1. Create a user
            CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
            int status = response.getStatusLine().getStatusCode();
            
            if(status != 201)
            	throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);
            
            String userId = getIdFromResponse(response);
            response.close();
            
            // 2. Create a project
            response = createProject(userId, "Project 1");
            status = response.getStatusLine().getStatusCode();
            
            if(status != 201)
            	throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);
            
            String projectId = getIdFromResponse(response);
            response.close();

            // 3. Create a session and associate it to a user and project
            response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
            // 4. Check the response status, if correct get the response body
            status = response.getStatusLine().getStatusCode();
            HttpEntity entity;

            if(status == 201){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
            
            // Convert response body to string (in purpose of comparing)
            String strResponse = EntityUtils.toString(entity);
            String sessionId = getIdFromStringResponse(strResponse);
            System.out.println("*** String response " + strResponse + " (" + status + ") ***");
            // Get server-side generated id
            String id = getIdFromStringResponse(strResponse);
            // Expected response body
            String expectedJson = "{\"id\":" + id + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:00Z\",\"counter\":1}";
            // Compare
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            //EntityUtils.consume(response.getEntity());
            response.close();

            response = updateSession(userId, projectId, sessionId, "2019-02-18T20:00Z", "2019-02-18T23:00Z", "2");
            status = response.getStatusLine().getStatusCode();

            if(status == 200){
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected POST response status while updating a session: " + status);

            strResponse = EntityUtils.toString(entity);
            System.out.println("*** String response " + strResponse + " (" + status + ") ***");
            // Get server-side generated id
            String updatedId = getIdFromStringResponse(strResponse);
            // Expected response body
            expectedJson = "{\"id\":" + updatedId + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T23:00Z\",\"counter\":2}";
            // Compare
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            //EntityUtils.consume(response.getEntity());
            response.close();
            
            
            // Delete project
            response = deleteProject(userId, projectId);
            EntityUtils.consume(response.getEntity());
            response.close();
            
            // Delete created user
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }
    
    @Test
    public void updateSessionBadRequestTest() throws Exception{
        System.out.println("----- Start testing PUT session for ill-formed request body -----");
        httpclient = HttpClients.createDefault();

        try{
            // Post user with bad request
            CloseableHttpResponse response = updateSessionBadRequest();
            int status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }
    
    @Test
    public void updateSessionNonExistentUserTest() throws Exception{
        System.out.println("----- Start testing PUT session for non-existent user -----");
        httpclient = HttpClients.createDefault();

        try{
            // 1. Post user with non-existent userId = 101
            CloseableHttpResponse response = updateSession("10101010", "projectId", "sessionId", "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
            // 2. Check the response status, if correct get the response body
            int status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404 || status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }
    
    @Test
    public void updateSessionNonExistentProjectTest() throws Exception{
        System.out.println("----- Start testing POST session for non-existent project -----");
        httpclient = HttpClients.createDefault();

        try{
            // 1. Post user with non-existent userId = 101
            CloseableHttpResponse response = updateSession("userId", "101010101", "sessionId", "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
            // 2. Check the response status, if correct get the response body
            int status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404 || status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }
    
    @Test
    public void updateSessionNonExistentSessionTest() throws Exception{
        System.out.println("----- Start testing POST session for non-existent session -----");
        httpclient = HttpClients.createDefault();

        try{
            // 1. Post user with non-existent userId = 101
            CloseableHttpResponse response = updateSession("userId", "projectId", "101010101", "2019-02-18T20:00Z", "2019-02-18T20:00Z", "1");
            
            // 2. Check the response status, if correct get the response body
            int status = response.getStatusLine().getStatusCode();
            System.out.println("*** Response code: " + status + " ***");
            if(status == 404 || status == 400) System.out.println("*** Response code correct ***");
            else throw new ClientProtocolException("*** Response code wrong: " + status + "***");
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }
    /* Session related test -- END */
    

    // // ------------------------------  Projects test ----------------------------------

     @Test
     public void createProjectSuccessTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
            deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);

             // Create project
             response = createProject(userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String strResponse = EntityUtils.toString(entity);
             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
             // Get server-side generated id
             String id = getIdFromStringResponse(strResponse);
             // Expected response body
             String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Project-1\",\"userId\":" + userId + "}";
             // Compare
             JSONAssert.assertEquals(expectedJson, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

             // Delete created project and user
             response = deleteProject(userId, id);
             EntityUtils.consume(response.getEntity());
             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void createProjectBadUserIDTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
            deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){ entity = response.getEntity(); }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject("badUserID", "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 400){
                 System.out.println("----- Pass createProjectBadUserID test -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             response.close();
             // Delete created project and user
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void createProjectBadBodyTest() throws Exception{
         System.out.println("----- Start testing 404 project POST -----");
         try{
            deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){ entity = response.getEntity(); }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createBadProject("Project-1", userId);
             status = response.getStatusLine().getStatusCode();
             //baseUrl + "/users/"+userId +"", this end point doesn't have a POST method, it should return 400 Bad Request
             if(status == 400){
                 System.out.println("----- Pass createProjectBadBody test -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             response.close();
//             String id = getIdFromResponse(response);
//             // Delete created project and user
//             response = deleteProject(userId ,id);
//             EntityUtils.consume(response.getEntity());
//             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void createProjectMissUserIdTest() throws Exception{
         System.out.println("----- Start testing 404 project POST -----");
         try{
            deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){ entity = response.getEntity(); }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject( userId+"1", "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 404){
                 System.out.println("----- Pass createProjectMissUserID test -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             response.close();
             // Delete created project and user
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void createProjectExistedTest() throws Exception{
         System.out.println("----- Start testing 409 project POST -----");
         try{
            deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){ entity = response.getEntity(); }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject(userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In createProjectExist test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             response.close();

             response = createProject(userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 409){
                 System.out.println("----- Pass createProjectExist test -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
//             String id = getIdFromResponse(response);
//             // Delete created project and user
//             response = deleteProject(userId ,id);
//             EntityUtils.consume(response.getEntity());
//             response.close();
             // Delete created project and user
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

    @Test
    public void getProjectTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            String id = getIdFromResponse(response);
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            response.close();

            response = getProject(userId);
            status = response.getStatusLine().getStatusCode();
            if(status == 200){
                System.out.println("----- get test receive 200 -----");
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            String strResponse = EntityUtils.toString(entity);
            String expectedJson = "[{\"id\":" + id + ",\"projectname\":\"Project-1\",\"userId\":" + userId + "}]";
            // Compare
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Delete created project and user
            response = deleteProject(userId ,id);
            EntityUtils.consume(response.getEntity());
            response.close();
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    // @Test
    // public void getProjectBadIDTest() throws Exception{
    //     System.out.println("----- Start testing 200 project GET -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){ entity = response.getEntity(); }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In getProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         response.close();

    //         response = getProject("badID");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 400){
    //             System.out.println("----- pass get project via badUserId -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);

    //         String id = getIdFromResponse(response);
    //         // Delete created project and user
    //         response = deleteProject(userId ,id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getProjectMissIDTest() throws Exception{
    //     System.out.println("----- Start testing 200 project GET -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){ entity = response.getEntity(); }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In getProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         response.close();

    //         response = getProject(userId+"1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 404){
    //             System.out.println("----- pass get project via MissingUserId -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);

    //         String id = getIdFromResponse(response);
    //         // Delete created project and user
    //         response = deleteProject(userId ,id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    @Test
    public void getProjectViaIDTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            String id = getIdFromResponse(response);
            response.close();

            response = getProjectViaID(userId, id);
            status = response.getStatusLine().getStatusCode();
            if(status == 200){
                System.out.println("----- get test receive 200 -----");
                entity = response.getEntity();
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            String strResponse = EntityUtils.toString(entity);
            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Project-1\",\"userId\":" + userId + "}";
            // Compare
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            // Delete created project and user
            response = deleteProject(userId ,id);
            EntityUtils.consume(response.getEntity());
            response.close();
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectViaBadUserIDTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            String id = getIdFromResponse(response);
            response.close();

            response = getProjectViaID("badUserId", id);
            status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println("----- pass get project via bad userId and projectId -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectViaBadProjectIDTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            String id = getIdFromResponse(response);
            response.close();

            response = getProjectViaID(userId, "badId");
            status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println("----- pass get project via bad userId and projectId -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            // Delete created project and user
            response = deleteProject(userId ,id);
            EntityUtils.consume(response.getEntity());
            response.close();
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectViaMissProjectIDTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            String id = getIdFromResponse(response);
            response.close();

            response = getProjectViaID(userId, id+"10000");
            status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println("----- pass get project via missing projectId -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            // Delete created project and user
            response = deleteProject(userId ,id);
            EntityUtils.consume(response.getEntity());
            response.close();
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectViaMissUserIDTest() throws Exception{
        System.out.println("----- Start testing 200 project GET -----");
        try{
            deleteUsers();
            // Create user
            CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if(status == 201){ entity = response.getEntity(); }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            String userId = getIdFromResponse(response);

            // Create project
            response = createProject(userId, "Project-1");
            status = response.getStatusLine().getStatusCode();
            if(status == 201){
                System.out.println("----- In getProject test  -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);
            // Convert response body to string (in purpose of comparing)
            String id = getIdFromResponse(response);
            response.close();

            response = getProjectViaID(userId+"1", id);
            status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println("----- pass get project via missing UserId -----");
            }
            else throw new ClientProtocolException("Unexpected response status: " + status);

            // Delete created project and user
            response = deleteProject(userId ,id);
            EntityUtils.consume(response.getEntity());
            response.close();
            response = deleteUser(userId);
            EntityUtils.consume(response.getEntity());
            response.close();
        }
        finally {
            httpclient.close();
        }
    }

    // @Test
    // public void putProjectSuccessTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateProject(id, userId, "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 200){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"newProjectName\",\"userId\":\"" + userId + "\"}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void putProjectBadUserIdTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateProject(id, "badUserId", "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 400){
    //             System.out.println("----- pass putProjectBadUserIdTest  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void putProjectBadProjectIdTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateProject("badProjectId", userId, "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 400){
    //             System.out.println("----- pass putProjectBadProjectIdTest  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void putProjectBadProjectBodyTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateBadProject(id, userId, "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 400){
    //             System.out.println("----- pass putProjectBadProjectBodyTest  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void putProjectMissUserIdTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateProject(id, userId+"1", "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 404){
    //             System.out.println("----- pass putProjectMissUserIdTest  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void putProjectMissProjectIdTest() throws Exception{
    //     System.out.println("----- Start testing successful project POST -----");
    //     try{
    //         // Create user
    //         CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         if(status == 201){
    //             entity = response.getEntity();
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         String userId = getIdFromResponse(response);

    //         // Create project
    //         response = createProject(userId, "Project-1");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 201){
    //             System.out.println("----- In putProject test  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         // update project
    //         response = updateProject(id+"1", userId, "newProjectName");
    //         status = response.getStatusLine().getStatusCode();
    //         if(status == 404){
    //             System.out.println("----- pass putProjectMissProjectIdTest  -----");
    //         }
    //         else throw new ClientProtocolException("Unexpected response status: " + status);
    //         // Convert response body to string (in purpose of comparing)
    //         // Delete created project and user
    //         response = deleteProject(userId, id);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }


     @Test
     public void deleteProjectSuccessTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
             deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject( userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In putProject test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String id = getIdFromResponse(response);
             response.close();

             // delete project
             response = deleteProject(userId, id);
             status = response.getStatusLine().getStatusCode();
             if(status == 200){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String strResponse = EntityUtils.toString(entity);
             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
             // Expected response body
             String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Project-1\",\"userId\":" + userId + "}";
             // Compare
             JSONAssert.assertEquals(expectedJson, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
             // Delete created project and user
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteProjectBadUserIdTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject(userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In putProject test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String id = getIdFromResponse(response);
             response.close();

             // delete project
             response = deleteProject("badUserId", id);
             status = response.getStatusLine().getStatusCode();
             if(status == 400){
                 System.out.println("----- pass deleteProjectBadUserIdTest  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             // Delete created project and user
             response = deleteProject(userId, id);
             EntityUtils.consume(response.getEntity());
             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteProjectBadProjectIdTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
             deleteUsers();
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject( userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In putProject test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String id = getIdFromResponse(response);
             response.close();

             // delete project
             response = deleteProject(userId, "badProjectId");
             status = response.getStatusLine().getStatusCode();
             if(status == 400){
                 System.out.println("----- pass deleteProjectBadProjectIdTest  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             // Delete created project and user
             response = deleteProject(userId, id);
             EntityUtils.consume(response.getEntity());
             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteProjectMissProjectIdTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject( userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In putProject test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String id = getIdFromResponse(response);
             response.close();

             // delete project
             response = deleteProject(userId, id+"1");
             status = response.getStatusLine().getStatusCode();
             if(status == 404){
                 System.out.println("----- pass deleteProjectMissProjectIdTest  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             // Delete created project and user
             response = deleteProject(userId, id);
             EntityUtils.consume(response.getEntity());
             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }

     @Test
     public void deleteProjectMissUserIdTest() throws Exception{
         System.out.println("----- Start testing successful project POST -----");
         try{
             // Create user
             CloseableHttpResponse response = createUser("Logan", "Superman", "logansuperman@xxx.com");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if(status == 201){
                 entity = response.getEntity();
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             String userId = getIdFromResponse(response);
             response.close();

             // Create project
             response = createProject( userId, "Project-1");
             status = response.getStatusLine().getStatusCode();
             if(status == 201){
                 System.out.println("----- In putProject test  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             String id = getIdFromResponse(response);
             response.close();

             // delete project
             response = deleteProject(userId+"1", id);
             status = response.getStatusLine().getStatusCode();
             if(status == 404){
                 System.out.println("----- pass deleteProjectMissUserIdTest  -----");
             }
             else throw new ClientProtocolException("Unexpected response status: " + status);
             // Convert response body to string (in purpose of comparing)
             // Delete created project and user
             response = deleteProject(userId, id);
             EntityUtils.consume(response.getEntity());
             response.close();
             response = deleteUser(userId);
             EntityUtils.consume(response.getEntity());
             response.close();
         }
         finally {
             httpclient.close();
         }
     }



    // /* Report related test -- START */
    // @Test
    // public void getReportTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReport(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z");
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 				"\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1" + "}]}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithInvalidProjectTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid project ID
    //         response = getReport(userId, "1859301234985293", "2019-02-18T19:00Z", "2019-02-18T21:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 404){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithInvalidUserTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport("1859301234985293", projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 404){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithInvalidStartTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport(userId, projectId, "gobbledygook", "2019-02-18T21:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithInvalidEndTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport(userId, projectId, "2019-02-18T21:00Z", "gobbledygook");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithStartTimeAfterEndTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport(userId, projectId, "2019-02-18T21:00Z", "2019-02-18T19:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithEmptyStartTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport(userId, projectId, "", "2019-02-18T21:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithEmptyEndTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with invalid user ID
    //         response = getReport(userId, projectId, "2019-02-18T21:00Z", "");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithProjectFromOtherUserTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create two users
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         response = createUser("John", "Doe", "john@doe.com");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String otherUserId = getIdFromResponse(response);
    //         response.close();
    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Call a report with other user ID
    //         response = getReport(otherUserId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 400){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithNoSessionTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Try to get a report
    //         HttpEntity entity;
    //         response = getReport(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z");
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": []}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithStartTimeAfterSessionStartTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReport(userId, projectId, "2019-02-18T20:01Z", "2019-02-18T21:00Z");
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 "\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1}]}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithEndTimeBeforeSessionEndTimeTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReport(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T20:59Z");
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 "\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1}]}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithHoursAndPomodorosTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReportWithPomodorosAndHours(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z", true, true);
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 "\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1}]," +
    //                 "\"completedPomodoros\": 1," + "\"totalHoursWorkedOnProject\": 1}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithPomodorosTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReportWithPomodoros(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z", true);
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 "\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1}]," +
    //                 "\"completedPomodoros\": 1}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportWithHoursTest() throws Exception{
    //     System.out.println("----- Start testing GET report-----");
    //     httpclient = HttpClients.createDefault();

    //     try{
    //         // 1. Create a user
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "jane@doe.com");
    //         int status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a user: " + status);

    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         // 2. Create a project
    //         response = createProject(userId, "Project 1");
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201)
    //             throw new ClientProtocolException("Unexpected POST response status while creating a project: " + status);

    //         String projectId = getIdFromResponse(response);
    //         response.close();

    //         // 3. Create a session and associate it to a user and project
    //         response = createSession(userId, projectId, "2019-02-18T20:00Z", "2019-02-18T21:00Z", "1");

    //         // 4. Check the response status, if correct get the response body
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 201){
    //             throw new ClientProtocolException("Unexpected POST response status while creating a session: " + status);
    //         }

    //         // 5. Finally get a report
    //         HttpEntity entity;
    //         response = getReportWithHours(userId, projectId, "2019-02-18T19:00Z", "2019-02-18T21:00Z", true);
    //         entity = response.getEntity();
    //         status = response.getStatusLine().getStatusCode();

    //         if(status != 200){
    //             throw new ClientProtocolException("Unexpected GET response status while getting a report: " + status);
    //         }

    //         // Convert response body to string (in purpose of comparing)
    //         String strResponse = EntityUtils.toString(entity);
    //         System.out.println("*** String response " + strResponse + " (" + status + ") ***");
    //         // Expected response body
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \"2019-02-18T20:00Z\"," +
    //                 "\"endingTime\": \"2019-02-18T21:00Z\"," + "\"hoursWorked\": 1}]," +
    //                 "\"totalHoursWorkedOnProject\": 1}";
    //         // Compare
    //         JSONAssert.assertEquals(expectedJson, strResponse, false);
    //         //EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete project
    //         response = deleteProject(userId, projectId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //         // Delete created user
    //         response = deleteUser(userId);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     }
    //     finally {
    //         httpclient.close();
    //     }
    // }

    //-------------------------------------------- Requests ----------------------------------------------
    // Users Requests
    private CloseableHttpResponse createUser(String firstname, String lastname, String email) throws IOException{
        HttpPost httpPost = new HttpPost(baseUrl + "/users");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                                                "\"lastName\":\"" + lastname + "\"," +
                                                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse createUserBadRequest() throws IOException{
        HttpPost httpPost = new HttpPost(baseUrl + "/users");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("Blah Blah Blah...");
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse updateUser(String id, String firstname, String lastname, String email) throws IOException{
        HttpPut httpPut = new HttpPut(baseUrl + "/users/" + id);
        httpPut.addHeader("accpet", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse updateUserBadRequest(String id) throws IOException{
        HttpPut httpPut = new HttpPut(baseUrl + "/users/" + id);
        httpPut.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("Blah Blah Blah...");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse deleteUser(String id) throws IOException{
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getUser(String id) throws IOException{
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + id);
        httpGet.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getAllUsers() throws IOException{
        HttpGet httpGet = new HttpGet(baseUrl + "/users");
        httpGet.addHeader("accpet", "application/json");

        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    // Projects Requests
    private CloseableHttpResponse createProject(String userId, String projectname) throws IOException{
        HttpPost httpPost = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," +
                                                "\"userid\":\"" + userId + "\"}" );
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }
    
    private CloseableHttpResponse deleteProject(String userId, String projectId) throws IOException{
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse createBadProject (String projectName, String userId) throws IOException{
        HttpPost httpPost = new HttpPost(baseUrl + "/users/"+userId +"");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("Bad Body");
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getProject(String userId) throws IOException{
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + userId +"/projects");
        httpGet.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getProjectViaID(String userId, String Id) throws IOException{
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + userId +"/projects/"+Id);
        httpGet.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse updateProject(String id, String userId, String projectName) throws IOException {
        HttpPut httpPut = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + id);
        httpPut.addHeader("accpet", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"projectname\":\"" + projectName + "\"," +
                "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse updateBadProject(String id, String userId, String projectName) throws IOException {
        HttpPut httpPut = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + id);
        httpPut.addHeader("accpet", "application/json");
        StringEntity input = new StringEntity("bad body");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    // Sessions Requests
    private CloseableHttpResponse createSession(String userId, String projectId, String startTime, String endTime, String counter) throws IOException{
        
    	HttpPost httpPost = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                                                "\"endTime\":\"" + endTime + "\"," +
        										"\"counter\":\"" + counter + "\"}");
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }
    
    private CloseableHttpResponse createSessionBadRequest() throws IOException{
        HttpPost httpPost = new HttpPost(baseUrl + "/users/" + "userId" + "/projects/" + "projectId" + "/sessions");
        httpPost.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("Ill-formed request body. This should be rejected.");
        input.setContentType("application/json");
        httpPost.setEntity(input);

        System.out.println("*** Executing request " + httpPost.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPost);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }
    
    private CloseableHttpResponse updateSession(String userId, String projectId, String sessionId, String startTime, String endTime, String counter) throws IOException{
        HttpPut httpPut = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpPut.addHeader("accpet", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                								"\"endTime\":\"" + endTime + "\"," +
                								"\"counter\":\"" + counter + "\"}");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }
    
    private CloseableHttpResponse updateSessionBadRequest() throws IOException{
    	HttpPut httpPut = new HttpPut(baseUrl + "/users/" + "userId" + "/projects/" + "projectId" + "/sessions/" + "sessionId");
    	httpPut.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("Ill-formed request body. This should be rejected.");
        input.setContentType("application/json");
        httpPut.setEntity(input);

        System.out.println("*** Executing request " + httpPut.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpPut);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }
    
    // Report Requests
    private CloseableHttpResponse getReport(String id, String projectId, String startTime, String endTime) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + id + "/projects/" + projectId + "/report/?from=" + startTime + "&?to=" + endTime);
        httpGet.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getReportWithPomodoros(String id, String projectId, String startTime, String endTime, boolean pomodoros) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + id + "/projects/" + projectId + "/report/?from=" + startTime + "&?to=" + endTime + "&?includeCompletedPomodoros=" + pomodoros);
        httpGet.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getReportWithHours(String id, String projectId, String startTime, String endTime, boolean hours) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + id + "/projects/" + projectId + "/report/?from=" + startTime + "&?to=" + endTime + "&?includeTotalHoursWorkedOnProject=" + hours);
        httpGet.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    private CloseableHttpResponse getReportWithPomodorosAndHours(String id, String projectId, String startTime, String endTime, boolean pomodoros, boolean hours) throws IOException {
        HttpGet httpGet = new HttpGet(baseUrl + "/users/" + id + "/projects/" + projectId + "/report/?from=" + startTime + "&?to=" + endTime + "&?includeCompletedPomodoros=" + pomodoros + "&?includeTotalHoursWorkedOnProject=" + hours);
        httpGet.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpGet.getRequestLine() + " ***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println("*** Raw response " + response + " ***");
        return response;
    }

    // ------------------------------------------- Id parsing function --------------------------------
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
    
}
