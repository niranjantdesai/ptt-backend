package edu.gatech.cs6301;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackendTestsMobile1 {

    private String baseUrl = "http://localhost:8080";
    private PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpclient;
    private boolean setupdone = false;
    
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
    private List<String> createdIDs = new ArrayList<String>();
    
    @Test
    public void createUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
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
            //Push Our ID at the end of the list
            createdIDs.add(id);

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            //Push the ID at the end of the list
            createdIDs.add(id);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"Tom\",\"lastname\":\"Doe\",\"email\":\"tom@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            //Push our created ID to the end of the list.
            createdIDs.add(id);
            response.close();

            response = updateUser(id+"xyz", "Tom", "Doe", "tom@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            createdIDs.add(id);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",,\"email\":\"john@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        String id = null;
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            createdIDs.add(id);
            expectedJson += "[{\"id\":\"" + id + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            createdIDs.add(id);
            expectedJson += ",{\"id\":\"" + id + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":\"" + deleteid + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createMultipleDeleteOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteId = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            createdIDs.add(id);
            expectedJson += "[{\"id\":\"" + id + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson2 = "{\"id\":\"" + deleteId + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson2,strResponse, false);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createMultipleUpdateOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            createdIDs.add(id);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            createdIDs.add(updatedId);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateUser(updatedId, "Jane", "Wall",  "jane@wall.com");
            String expectedJson = "{\"id\":\"" + updatedId + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}";

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // Check that the record is correct in the response
            JSONAssert.assertEquals(expectedJson,strResponse, false);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            // Check that the record was correctly updated in the addressbook
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            createdIDs.add(id1);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            createdIDs.add(id2);
            response.close();

            String missingId = "xyz" + id1 + id2; // making sure the ID is not present

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
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            createdIDs.add(id1);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            createdIDs.add(id2);
            response.close();

            String missingId = "xyz" + id1 + id2; // making sure the ID is not present

            response = deleteUser(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");

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

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"Project 1\",\"userId\":" + userId +"\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllProjectsForUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            String expectedJson;

            response = createProject(userId,"Project 1");
            String projectId1 = getIdFromResponse(response);
            expectedJson = "[{\"id\":\"" + projectId1 + "\",\"projectname\":\"Project 1\",\"userId\":" + userId +"\"},";
            response = createProject(userId,"Project 1");
            String projectId2 = getIdFromResponse(response);
            expectedJson += "{\"id\":\"" + projectId2 + "\",\"projectname\":\"Project 1\",\"userId\":" + userId +"\"},";
            response = createProject(userId,"Project 1");
            String projectId3 = getIdFromResponse(response);
            expectedJson = "{\"id\":\"" + projectId3 + "\",\"projectname\":\"Project 1\",\"userId\":" + userId +"\"}]";

            response = getAllProjects(userId);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }
    

    @Test
    public void getAllProjectsForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");

            String missingId ="xyz"+ userId; //User ID should not exist
            response = getAllProjects(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectForUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");
            String projectId= getIdFromResponse(response);

            response = getProject(userId, projectId);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"Project 3\",\"userId\":" + userId +"\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectForMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");
            String projectId= getIdFromResponse(response);
            String missingprojectId= "xyz"+projectId;
            response = getProject(userId, missingprojectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");

            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");
            String projectId= getIdFromResponse(response);
            String missinguserId= "xyz"+userId;
            response = getProject(missinguserId, projectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }
    
    @Test
    public void postOneSessionTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            
            String projectId = getIdFromResponse(response);
            
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;
            
            response = createSession(userId, projectId, startTime, endTime, counter);
            JSONObject responseJSON = new JSONObject(EntityUtils.toString(response.getEntity()));
            
            startTime = responseJSON.getString("startTime");
            
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

            String expectedJson = "{\"id\":\"" + id + "\",\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\",\"counter\":" + counter +"\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }
    
    @Test
    public void postSessionUpdateMissingSessionTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;
            
            response = createSession(userId, projectId, startTime, endTime, counter);
            String sessionId = getIdFromResponse(response) + "xyz";
            endTime = "2019-02-18T20:00Z";
            counter = 2;
            
            response = updateSession(userId, projectId, sessionId, startTime, endTime, counter);
            
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }          
    }
    
    @Test
    public void updateSessionForMissingUserTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            userId = userId + "xyz";
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);
            String startTime = "2019-02-18T18:00Z";
            String endTime = "2019-02-18T19:00Z";
            int counter = 2;            
            response = createSession(userId, projectId, startTime, endTime, counter);
            String sessionId = getIdFromResponse(response);
            endTime = "2019-02-18T20:00Z";
            counter = 4;
            
            response = updateSession(userId, projectId, sessionId, startTime, endTime, counter);
            
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }          
    }
    
    @Test
    public void updateSessionForMissingProjectTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response) + "xyz";
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;            
            response = createSession(userId, projectId, startTime, endTime, counter);
            String sessionId = getIdFromResponse(response);
            
            endTime = "2019-02-18T20:30Z";
            counter = 5;
            
            response = updateSession(userId, projectId, sessionId, startTime, endTime, counter);
            
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }          
    }
    
    @Test
    public void postSessionForMissingProjectTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response) + "xyz";
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;
            
            response = createSession(userId, projectId, startTime, endTime, counter);
            
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }          
    }
    
    @Test
    public void postSessionForMissingUserTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            userId = userId + "xyz";
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);
            
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;            
            response = createSession(userId, projectId, startTime, endTime, counter);
            
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }          
    }
    
    @Test
    public void postMultipleSessionsUpdateOneSessionTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);
            
            String startTime = "2019-02-18T18:00Z";
            String endTime = "2019-02-18T18:30Z";
            int counter = 1;            
            response = createSession(userId, projectId, startTime, endTime, counter);
            response = createSession(userId, projectId, startTime, endTime, counter);
            response = createSession(userId, projectId, startTime, endTime, counter);
            String sessionId = getIdFromResponse(response);
            response = createSession(userId, projectId, startTime, endTime, counter);
            
            endTime = "2019-02-18T20:30Z";
            counter = 5;
            
            response = updateSession(userId, projectId, sessionId, startTime, endTime, counter);
            
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

            String expectedJson = "{\"id\":\"" + id + "\",\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\",\"counter\":" + counter +"\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }              
    }

    @Test
    public void postOneSessionUpdateOneSessionTest() throws Exception{
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);
            
            String startTime = "2019-02-18T18:00Z";
            String endTime = "";
            int counter = 0;            
            response = createSession(userId, projectId, startTime, endTime, counter);
            
            String sessionId = getIdFromResponse(response);
            endTime = "2019-02-18T20:30Z";
            counter = 5;
            
            response = updateSession(userId, projectId, sessionId, startTime, endTime, counter);
            
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

            String expectedJson = "{\"id\":\"" + id + "\",\"startTime\":\"" + startTime + "\",\"endTime\":\"" + endTime + "\",\"counter\":" + counter +"\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }              
    }

    @Test
    public void updateProjectForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");

            String projectId= getIdFromResponse(response);
            String missinguserId= "xyz"+userId;
            response = updateProject(missinguserId, "New name", projectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateProjectForMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");

            String projectId= getIdFromResponse(response);
            String missingprojectId= "xyz"+projectId;
            response = updateProject(userId, "New Name", missingprojectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);
            
            response = createProject(userId,"Project 3");

            String pId = getIdFromResponse(response);
            response = updateProject(userId, "New name", pId);

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

            String expectedJson = "{\"id\":\"" + userId + "{\"projectname\":\"" + "New name" + "{\"projectid\":\""+pId;
        JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }    

    @Test
    public void deleteProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteUserid = getIdFromResponse(response);
            
            response = createProject(deleteUserid,"Project 1");
            String deleteProjectid = getIdFromResponse(response);
            

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteProject(deleteUserid, deleteProjectid);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"Userid\":\"" + deleteUserid + "\"Projectid\":\"" + deleteProjectid + "}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
        JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteProjectForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");

            String projectId= getIdFromResponse(response);
            String missinguserId= "xyz"+userId;
            response = deleteProject(missinguserId, projectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteProjectForMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            response = createProject(userId,"Project 2");
            response = createProject(userId,"Project 3");

            String projectId= getIdFromResponse(response);
            String missingprojectId= "xyz"+projectId;
            response = deleteProject(userId, missingprojectId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }



    @Test
    public void getReportForProjectsForUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            String expectedJson = "\"sessions\": [";
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);

            response = createSession(userId,projectId,"2019-02-18T20:00Z","2019-02-18T21:00Z",2);
            expectedJson += "{\"startingTime\":\"\"2019-02-18T20:00Z\"\",\"endTime\":\"\"2019-02-18T21:00Z\"\",\"counter\": 2\", \"hoursWorked\":\"1\"},";

            response = createSession(userId,projectId,"2019-02-20T20:00Z","2019-02-21T01:00Z",6);
            String sessionId2 = getIdFromResponse(response);
            expectedJson += "{\"startTime\":\"\"2019-02-20T20:00Z\"\",\"endTime\":\"\"2019-02-21T01:00Z\"\",\"counter\": 6\", \"hoursWorked\":\"5\"}";

            response = createSession(userId,projectId,"2019-02-23T20:00Z","",0);
            String sessionId3 = getIdFromResponse(response);

            response = getReport(userId,projectId,"2019-02-18T20:00Z","2019-02-21T01:00Z",true,true);

            expectedJson += "], \"completedPomodoros\": 8,\n" +
                    "  \"totalHoursWorkedOnProject\": \"6\"}";

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void getReportForProjectForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);

            response = createSession(userId,projectId,"2019-02-18T20:00Z","2019-02-18T21:00Z",2);

            String missingId ="xyz"+ userId; //User ID should not exist

            response = getReport(missingId,projectId,"","",true,true);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getReportForMissingProjectForMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);

            response = createSession(userId,projectId,"2019-02-18T20:00Z","2019-02-18T21:00Z",2);

            String missingId ="xyz"+ userId; //User ID should not exist

            response = getReport(missingId,projectId,"","",true,true);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getReportForMissingProjectForUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        //deleteAllUsers();
        deleteOurUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            createdIDs.add(userId);

            response = createProject(userId,"Project 1");
            String projectId = getIdFromResponse(response);

            response = createSession(userId,projectId,"2019-02-18T20:00Z","2019-02-18T21:00Z",2);

            String missingId ="xyz"+ projectId; //Project ID should not exist

            response = getReport(userId,missingId,"","",true,true);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse createUser(String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"lastname\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createProject(String userId, String projectname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userId +"/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," +
                "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }
    
    private CloseableHttpResponse createSession(String userId, String projectId, String startTime, String endTime, int counter) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," + "\"endTime\":\"" + endTime + "\"," + "\"counter\":" + Integer.toString(counter) + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;        
    }  

    private CloseableHttpResponse updateUser(String id, String firstname, String lastname, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"lastname\":\"" + lastname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }
    
    private CloseableHttpResponse updateSession(String userId, String projectId, String sessionId, String startTime, String endTime, int counter) throws IOException{
        
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," + "\"endTime\":\"" + endTime + "\"," + "\"counter\":" + Integer.toString(counter) + "}");
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

    private CloseableHttpResponse getAllProjects(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userId + "/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getProject(String userId, String projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userId + "/projects/"+projectId);
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

    private CloseableHttpResponse deleteAllUsers() throws IOException, JSONException {
        CloseableHttpResponse response = getAllUsers();
        List<String> allIds = getIdsFromResponse(response);

        for (String id : allIds) {
            HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/"+id);
            httpDelete.addHeader("accept", "application/json");

            System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
            response = httpclient.execute(httpDelete);
            System.out.println("*** Raw response " + response + "***");
            // EntityUtils.consume(response.getEntity());
            // response.close();
        }
        return response;
    }
    
    private CloseableHttpResponse deleteOurUsers() throws IOException, JSONException {
        CloseableHttpResponse response = null;
        for (String id : createdIDs) {
            HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/"+id);
            httpDelete.addHeader("accept", "application/json");

            System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
            response = httpclient.execute(httpDelete);
            System.out.println("*** Raw response " + response + "***");
            // EntityUtils.consume(response.getEntity());
            // response.close();
        }
        createdIDs.clear();
        return response;
    }

    private CloseableHttpResponse updateProject(String userId, String projectname, String projectId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "\"projectname\":\"" + projectname + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

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

    private CloseableHttpResponse getReport(String userId, String projectId, String startTime, String endTime, boolean completed, boolean totalHrs) throws IOException {
        String url = baseUrl + "/users/" + userId + "/projects/" + projectId + "/report?";
        url += "&startTime="+startTime+"&endTime="+endTime+"&optCompletedPomodoros="+completed;
        url += "&optTotalHoursWorkedOnProject="+totalHrs;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpGet.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpGet);
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

    private List<String> getIdsFromResponse(CloseableHttpResponse response) throws JSONException, IOException {
        JSONArray array = new JSONArray(response);

        List<String> ids = null;

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            Iterator<String> keyList = object.keys();
            while (keyList.hasNext()){
                String key = keyList.next();
                if (key.equals("id")) {
                    String id = object.get(key).toString();
                    ids.add(id);
                }
            }
        }

        return ids;
    }

}
