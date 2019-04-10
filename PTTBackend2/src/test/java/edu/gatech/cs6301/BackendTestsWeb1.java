package edu.gatech.cs6301;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

public class BackendTestsWeb1 {

    private String baseUrl = "";
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
        while (keyList.hasNext()) {
            String key = keyList.next();
            if (key.equals("id")) {
                id = object.get(key).toString();
            }
        }
        return id;
    }

    // // *** YOU SHOULD NOT NEED TO CHANGE ANYTHING ABOVE THIS LINE ***

    // Users
    @Test
    public void createUserTest() throws Exception {
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":" + id
                    + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserTest() throws Exception {
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            response = updateUser(id, "Tom", "Doe", "tom@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id
                    + ",\"firstName\":\"Tom\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = getUser(id);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id
                    + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String id = null;
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id
                    + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += ",{\"id\":" + id
                    + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";
            response.close();

            response = getAllUsers();

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void DeleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(deleteid);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":" + deleteid
                    + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllUsers();
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleDeleteOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteId = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id
                    + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(deleteId);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson2 = "{\"id\":" + deleteId
                    + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson2, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllUsers();
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleUpdateOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateUser(updatedId, "Jane", "Wall", "jane@wall.com");
            String expectedJson = "{\"id\":" + updatedId
                    + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}";

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // Check that the record is correct in the response
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getUser(updatedId);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // Check that the record was correctly updated in the addressbook
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "999" + id1 + id2; // making sure the ID is not present

            response = getUser(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = Long.toString(Long.parseLong(id1) + Long.parseLong(id2)); // making sure the ID is not present

            response = deleteUser(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse createUser(String firstName, String lastName, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + 0 + "\"," + "\"firstName\":\"" + firstName + "\","
                + "\"lastName\":\"" + lastName + "\"," + "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateUser(String id, String firstName, String lastName, String email)
            throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstName + "\"," + "\"lastName\":\"" + lastName
                + "\"," + "\"email\":\"" + email + "\"}");
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
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse deleteUsers() throws IOException, JSONException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);

        int status;
        HttpEntity entity;
        status = response.getStatusLine().getStatusCode();
        if (status == 200) {
            entity = response.getEntity();
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }

        String strResponse = EntityUtils.toString(entity);
        CloseableHttpResponse deleteResponse;
        JSONArray jsonResponse = new JSONArray(strResponse);

        for (int i = 0; i < jsonResponse.length(); i++) {
            JSONObject a = jsonResponse.getJSONObject(i);
            String id = Integer.toString(a.getInt("id"));
            deleteResponse = deleteUser(id);
            deleteResponse.getStatusLine().getStatusCode();
            if (status == 200) {
                // HttpEntity deleteEntity = deleteResponse.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            deleteResponse.close();
        }
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    // Projects
    // Tests for projects
     @Test
     public void createProjectTest() throws Exception {
        deleteUsers();
         try {
             CloseableHttpResponse response = createUser("TestProject", "User1", "TPU1@gmail.com");
             String user_id = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = createProject(user_id, "test_project");
             status = response.getStatusLine().getStatusCode();

             if (status == 201) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("unexpected response status: " + status);
             }

             strResponse = EntityUtils.toString(entity);

             System.out.println(
                     "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             String project_id = getIdFromStringResponse(strResponse);
             String expectedJSON = "{\"id\":" + project_id + "," + "\"projectname\":\"test_project\","
                     + "\"userId\":" + user_id + "}";
             JSONAssert.assertEquals(expectedJSON, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

         } finally {
             httpclient.close();
         }
     }

    @Test
    public void updateProjectTest() throws Exception{
        try {
            CloseableHttpResponse response1 = createUser("TestProject", "User2", "TPU2@gmail.com");
            String user_id = getIdFromResponse(response1);
            response1.close();

            CloseableHttpResponse response2 = createProject(user_id, "test_project");
            String project_id = getIdFromResponse(response2);
            response2.close();

            CloseableHttpResponse response3 = updateProject(user_id, project_id, "updated_test_project");

            int status = response3.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;

            if (status == 200) {
                entity = response3.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response3.getStatusLine().getStatusCode() + ") ***");

            String expectedJSON = "{\"id\":" + project_id + "," + "\"projectname\":\"updated_test_project\","
                    + "\"userId\":" + user_id + "}";

            JSONAssert.assertEquals(expectedJSON, strResponse, false);
            EntityUtils.consume(response3.getEntity());
            response3.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectTest() throws Exception {

        try {
            deleteUsers();
            CloseableHttpResponse response1 = createUser("TestProject", "User3", "TPU3@gmail.com");
            String user_id = getIdFromResponse(response1);
            response1.close();

            CloseableHttpResponse response2 = createProject(user_id, "test_project");
            String project_id = getIdFromResponse(response2);
            response2.close();

            CloseableHttpResponse response3 = getProject(user_id, project_id);

            int status = response3.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;

            if (status == 200) {
                entity = response3.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response3.getStatusLine().getStatusCode() + ") ***");

            String expectedJSON = "{\"id\":" + project_id + "," + "\"projectname\":\"test_project\","
                    + "\"userId\":" + user_id + "}";

            JSONAssert.assertEquals(expectedJSON, strResponse, false);
            EntityUtils.consume(response3.getEntity());
            response3.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllProjectsTest() throws Exception {
        String expectedJson = "";
        try {
            CloseableHttpResponse response1 = createUser("TestProject", "User4", "TPU4@gmail.com");
            String user_id = getIdFromResponse(response1);
            response1.close();

            CloseableHttpResponse response2 = createProject(user_id, "test_project1");
            String project_id1 = getIdFromResponse(response2);
            expectedJson += "[{\"id\":" + project_id1 + "," + "\"projectname\":\"test_project1\","
            + "\"userId\":" + user_id + "}";
            response2.close();

            CloseableHttpResponse response3 = createProject(user_id, "test_project2");
            String project_id2 = getIdFromResponse(response3);
            expectedJson += ",{\"id\":" + project_id2 + "," + "\"projectname\":\"test_project2\","
            + "\"userId\":" + user_id + "}]";
            response3.close();

            CloseableHttpResponse response4 = getAllProjects(user_id);

            int status = response4.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;

            if (status == 200) {
                entity = response4.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response4.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response4.getEntity());
            response4.close();
        } finally {
            httpclient.close();
        }
    }

     @Test
     public void DeleteProjectTest() throws Exception {
        deleteUsers();
         try {

             CloseableHttpResponse response1 = createUser("TestProject", "User5", "TPU5@gmail.com");
             String user_id = getIdFromResponse(response1);
             response1.close();

             CloseableHttpResponse response2 = createProject(user_id, "test_project");
             String project_id = getIdFromResponse(response2);
             response2.close();

             int status;
             HttpEntity entity;
             String strResponse;

             CloseableHttpResponse response3 = deleteProject(user_id, project_id);

             status = response3.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response3.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println(
                     "*** String response " + strResponse + " (" + response3.getStatusLine().getStatusCode() + ") ***");

             String expectedJSON = "{\"id\":" + project_id + "," + "\"projectname\":\"test_project\","
                     + "\"userId\":" + user_id + "}";
                    
             JSONAssert.assertEquals(expectedJSON, strResponse, false);
             EntityUtils.consume(response3.getEntity());
             response3.close();

             CloseableHttpResponse response4 = getAllProjects(user_id);
             status = response4.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response4.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println(
                     "*** String response " + strResponse + " (" + response4.getStatusLine().getStatusCode() + ") ***");

             expectedJSON = "[]";
             JSONAssert.assertEquals(expectedJSON, strResponse, false);
             EntityUtils.consume(response4.getEntity());
             response4.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void CreateMultipleDeleteOneProjectTest() throws Exception {
         String expectedJson = "";
         deleteUsers();
         try {

             CloseableHttpResponse response = createUser("TestProject", "User6", "TPU6@gmail.com");
             String user_id = getIdFromResponse(response);
             response.close();

             response = createProject(user_id, "test_project_delete");
             String deleteId = getIdFromResponse(response);
             response.close();

             response = createProject(user_id, "test_project_remain");
             String id = getIdFromResponse(response);
             expectedJson += "[{\"id\":" + id + "," + "\"projectname\":\"test_project_remain\","
                                 + "\"userId\":" + user_id + "}]";
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject(user_id, deleteId);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println(
                     "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             String expectedJson2 = "{\"id\":" + deleteId + "," + "\"projectname\":\"test_project_delete\","
                                     + "\"userId\":" + user_id + "}";
             JSONAssert.assertEquals(expectedJson2, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();

             response = getAllProjects(user_id);
             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);
             System.out.print("\n\n\n\n\n\n\n\n\n\n\n");
             System.out.println(
                     "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
             System.out.print("\n\n\n\n\n\n\n\n\n\n\n");
             JSONAssert.assertEquals(expectedJson, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void CreateMultipleUpdateOneProjectTest() throws Exception {
        String expectedJson = "";

        try {

            CloseableHttpResponse response = createUser("TestProject", "User7", "TPU7@gmail.com");
            String user_id = getIdFromResponse(response);
            response.close();

            response = createProject(user_id, "test_project");
            String updateId = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + updateId + "," + "\"projectname\":\"test_project_update\","
                                + "\"userId\":" + user_id + "}";
            response.close();

            response = createProject(user_id, "test_project_remain");
            String id = getIdFromResponse(response);
            expectedJson += ",{\"id\":" + id + "," + "\"projectname\":\"test_project_remain\","
                                + "\"userId\":" + user_id + "}]";
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateProject(user_id, updateId, "test_project_update");

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson2 = "{\"id\":" + updateId + "," + "\"projectname\":\"test_project_update\","
                                    + "\"userId\":" + user_id + "}";
            JSONAssert.assertEquals(expectedJson2, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllProjects(user_id);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingProjectTest() throws Exception {
        try {
            deleteUsers();
            CloseableHttpResponse response = createUser("TestProject", "User8", "TPU8@gmail.com");
            String user_id = getIdFromResponse(response);
            response.close();

            response = createProject(user_id, "test_project1");
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject(user_id, "test_project2");
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "12341234" + id1 + id2; // making sure the ID is not present
            
            response = getProject(user_id, missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

     @Test
     public void deleteMissingProjectTest() throws Exception {
        deleteUsers();
         try {
             CloseableHttpResponse response = createUser("TestProject", "User8", "TPU8@gmail.com");
             String user_id = getIdFromResponse(response);
             response.close();

             response = createProject(user_id, "test_project1");
             String id1 = getIdFromResponse(response);
             response.close();

             response = createProject(user_id, "test_project2");
             String id2 = getIdFromResponse(response);
             response.close();

             String missingId = id1 + id2; // making sure the ID is not present
            
             response = deleteProject(user_id, missingId);
            
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

    // API calls for projects
    // GET ​/users​/{userId}​/projects Return all projects for a user
    private CloseableHttpResponse getAllProjects(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // POST ​/users​/{userId}​/projects Add a new project
    private CloseableHttpResponse createProject(String userId, String projectname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects" );
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," + "\"userId\":" + userId + ",\"id\":" + "0" + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //GET ​/users​/{userId}​/projects​/{projectId} Get project by ID
    private CloseableHttpResponse getProject(String userId, String projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // PUT ​/users​/{userId}​/projects​/{projectId} Update project
    private CloseableHttpResponse updateProject(String userId, String projectId, String projectname)
            throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + projectId + "\"," + "\"projectname\":\"" + projectname
                + "\"," + "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //DELETE ​/users​/{userId}​/projects​/{projectId} Delete a project
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
    
    // Sessions

     @Test
     public void createSessionTest() throws Exception {
         // deleteContacts();
         httpclient = HttpClients.createDefault();

         try {
             CloseableHttpResponse response = createUser("John", "Lisa", "JL@gmail.com");
             String user_id = getIdFromResponse(response);
             response.close();

             response = createProject(user_id, "test_project");
             String proj_id = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = createSession(user_id, proj_id, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "2");

             status = response.getStatusLine().getStatusCode();
             if (status == 201) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println(
                     "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             String session_id = getIdFromStringResponse(strResponse);

             String expectedJSON = "{\"id\":" + session_id + "," + "\"startTime\":\"2019-02-18T20:00Z\","
                     + "\"endTime\":\"2019-02-18T20:00Z\"," + "\"counter\":2" + "}";
             JSONAssert.assertEquals(expectedJSON, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void updateSessionTest() throws Exception{
        try {
            deleteUsers();
            CloseableHttpResponse response1 = createUser("Tom", "John", "tom@gatech.edu");
            String user_id = getIdFromResponse(response1);
            response1.close();
            CloseableHttpResponse response2 = createProject(user_id, "pomodoro");
            String project_id = getIdFromResponse(response2);
            response2.close();
            CloseableHttpResponse response3 = createSession(user_id, project_id, "2019-02-18T20:00Z", "2019-02-18T20:00Z", "2");
            String session_id = getIdFromResponse(response3);
            response3.close();

            response3 = updateSession(user_id, project_id, session_id, "2019-02-18T21:00Z", "2019-02-18T23:00Z", "3");

            int status = response3.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response3.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response3.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + session_id
                    + ",\"startTime\": \"2019-02-18T21:00Z\",\"endTime\": \"2019-02-18T23:00Z\",\"counter\":3}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response3.getEntity());
            response3.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse createSession(String user_id, String proj_id, String startTime, String endTime,
            String counter) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + user_id + "/projects/" + proj_id + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," + "\"endTime\":\"" + endTime
                + "\"," + "\"counter\":\"" + counter + "\"}");
        
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("\n\n\n\n\n*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateSession(String user_id, String project_id, String session_id, String startTime,
            String endTime, String counter) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + user_id + "/projects/" + project_id + "/sessions/" + session_id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," + "\"endTime\":\"" + endTime
                + "\"," + "\"counter\":\"" + counter + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // Reports

    @Test
    public void GetReportTest() throws Exception {
        try {
            CloseableHttpResponse response = createUser("John", "Lisa", "JL@gmail.com");
            String user_id = getIdFromResponse(response);
            response.close();

            response = createProject(user_id, "test_project");
            String proj_id = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            String StartTime = "2019-02-18T20:00Z";
            String EndTime = "2019-02-18T21:00Z";
            String counter = "2";
            response = createSession(user_id, proj_id, StartTime, EndTime, counter);
            response.close();

            response = getReport(user_id, proj_id, StartTime, EndTime, true, true);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            String completedPomodoros = "2";
            String totalHoursWorkedOnProject = "1";
            String hoursWorked = "1";

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJSON = "{\"sessions\":[" + "{" + "\"startingTime\":\"" + StartTime + "\","
                    + "\"endingTime\":\"" + EndTime + "\"," + "\"hoursWorked\":" + hoursWorked + "}" + "],"
                    + "\"completedPomodoros\":" + completedPomodoros + "," + "\"totalHoursWorkedOnProject\":"
                    + totalHoursWorkedOnProject + "}";

            JSONAssert.assertEquals(expectedJSON, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse getReport(String user_id, String proj_id, String startTime, String endTime,
            boolean optCompletedPomodoros, boolean optTotalHoursWorkedOnProject) throws Exception{
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + user_id + "/projects/" + proj_id + "/report" 
        + "?from=" + startTime + "&to=" + endTime + "&includeCompletedPomodoros=" + String.valueOf(optCompletedPomodoros) + "&includeTotalHoursWorkedOnProject=" + String.valueOf(optTotalHoursWorkedOnProject));
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

}
