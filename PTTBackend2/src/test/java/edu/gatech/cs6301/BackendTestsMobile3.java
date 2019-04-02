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

public class BackendTestsMobile3 {

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

    // *** User Tests ***


    @Test
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String id = null;
        String expectedJson = "";
        deleteAllUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += ",{\"id\":" + id + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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
    public void createUserTest() throws Exception {

        try {
            deleteAllUsers();
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");

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
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();

        try {
            deleteAllUsers();
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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
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
        deleteAllUsers();

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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"Tom\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void DeleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = null;

        try {
            deleteAllUsers();
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

            expectedJson = "{\"id\":" + deleteid + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
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
    public void CreateMultipleDeleteOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String expectedJson = "";

        try {
            deleteAllUsers();
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteId = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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

            String expectedJson2 = "{\"id\":" + deleteId + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
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
    public void CreateMultipleUpdateOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateUser(updatedId, "Jane", "Wall","jane@wall.com");
            String expectedJson = "{\"id\":" + updatedId + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}";

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

        try {
            deleteAllUsers();
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = id1 + id2; // making sure the ID is not present

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

        try {
            deleteAllUsers();
            CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall",  "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = id1 + id2; // making sure the ID is not present

            response = deleteUser(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // //project tests



    // @Test
    // public void getAllProjectsTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
    //     String userId = getIdFromResponse(response);

    //     response.close();
    //     String id = "";
    //     String expectedJson = "";
    //     try {
    //         response = createProject("test1", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson = "[{\"id\":\"" + id + "\"," + "\"projectname\":\"" + "test1" + "\"," +
    //             "\"userId\":\"" + userId + "\"}";
    //         response.close();

    //         response = createProject("test2", userId);
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson += "{\"id\":\"" + id + "\"," + "\"projectname\":\"" + "test2" + "\"," +
    //             "\"userId\":\"" + userId + "\"}]";
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
    //     } finally {
    //         httpclient.close();
    //     }
    // }

     @Test
     public void createProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
         String userId = getIdFromResponse(response);
         response.close();

         //new project test1
         try {
             response =
	 	createProject("test1", userId);

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

             String expectedJson = "{\"id\":" + id + "," + "\"projectname\":\"" + "test1" + "\"," +
                 "\"userId\":" + userId + "}";
	     JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
         httpclient = HttpClients.createDefault();
         //duplicate condition
         try {
             response =
	 	createProject("test1", userId);

             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if (status == 409) {
                 System.out.println("Correctly response resource conflict");
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             response.close();
         } finally {
             httpclient.close();
         }

     }


    @Test
    public void getProjectTest() throws Exception {
        deleteAllUsers();
        httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
        String userId = getIdFromResponse(response);
        response.close();
        try {
            response = createProject("test1", userId);
            String projectId = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = getProject(projectId, userId);

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
            String id = getIdFromStringResponse(strResponse);
            String expectedJson = "{\"id\":" + id + "," + "\"projectname\":\"" + "test1" + "\"," +
                "\"userId\":" + userId + "}";
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void getMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
        String userId = getIdFromResponse(response);
        response.close();

        try {
            response = createProject("test1", userId);
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject("test2", userId);
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "12341234" + id1 + id2; // making sure the ID is not present

            response = getProject(missingId, userId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // @Test
    // public void updateProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
    //         String userID = getIdFromResponse(response);
    //         response.close();

    //         response = createProject("test1", userID);
    //         String projectID = getIdFromResponse(response);
    //         response.close();

    //         response = updateProject(userID, projectID, "SecondProject");

    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);
    //         String id = getIdFromStringResponse(strResponse);
    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         String expectedJson = "{\"id\":\"" + id + "\"," + "\"projectname\":\"" + "SecondProject" + "\"," +
    //             "\"userId\":\"" + userID + "\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // // xyzhere
    // @Test
    // public void CreateMultipleUpdateOneProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
    //         // EntityUtils.consume(response.getEntity());
    //         String userId = getIdFromResponse(response);
    //         response.close();

    //         response = createProject("test1", userId);
    //         String projectID1 = getIdFromResponse(response);
    //         response.close();

    //         response = createProject("test2", userId);
    //         String projectID2 = getIdFromResponse(response);
    //         response.close();

    //         response = updateProject(userId, projectID1, "SecondProject");

    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         if (status == 200) {
    //             entity = response.getEntity();
    //         } else {
    //             throw new ClientProtocolException("Unexpected response status: " + status);
    //         }
    //         strResponse = EntityUtils.toString(entity);
    //         String id = getIdFromStringResponse(strResponse);
    //         System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

    //         String expectedJson = "{\"id\":\"" + id + "\"," + "\"projectname\":\"" + "SecondProject" + "\"," +
    //             "\"userId\":\"" + userId + "\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();


    //     } finally {
    //         httpclient.close();
    //     }
    // }

     @Test
     public void DeleteProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         String expectedJson = null;

         try {
             CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
             // EntityUtils.consume(response.getEntity());
             String userId = getIdFromResponse(response);
             response.close();


             response = createProject("testProject", userId);
             String projectID = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject(userId, projectID);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);
             String id = getIdFromStringResponse(strResponse);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             expectedJson = "{\"id\":" + id + "," + "\"projectname\":\"" + "testProject" + "\"," +
                 "\"userId\":" + userId + "}";
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
             response.close();
         } finally {
             httpclient.close();
         }
     }



     // xyzhere
     @Test
     public void CreateMultipleDeleteOneProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();

         try {
             CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
             // EntityUtils.consume(response.getEntity());
             String userId = getIdFromResponse(response);
             response.close();

             response = createProject("project1", userId);
             String projectID1 = getIdFromResponse(response);
             response.close();

             response = createProject("project2", userId);
             String projectID2 = getIdFromResponse(response);
             response.close();

             response = deleteProject(userId, projectID2);

             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             String strResponse;
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Error in CreateMultipleDeleteOneProjectTest Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);
             String id = getIdFromStringResponse(strResponse);
             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             String expectedJson = "{\"id\":" + id + "," + "\"projectname\":\"" + "project2" + "\"," +
                 "\"userId\":" + userId + "}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();


         } finally {
             httpclient.close();
         }
     }


     @Test
     public void deleteMissingProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         String expectedJson = null;

         try {
             CloseableHttpResponse response = createUser("John", "Doe",  "john@doe.org");
             // EntityUtils.consume(response.getEntity());
             String userId = getIdFromResponse(response);
             response.close();


             response = createProject("testProject", userId);
             String projectID = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             String missingProjectID = projectID + "100"; // making sure the ID is not present
             // Ensure the project ID does NOT exist

             response = deleteProject(userId, missingProjectID);

             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();

         } finally {
             httpclient.close();
         }
     }


    // // *** Session POST Test ***

     @Test
     public void createSessionTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();

         try {
             CloseableHttpResponse response = createTestSession("2019-02-18T20:00Z", "0");

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

             String expectedJson = "{\"id\":" + id + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:00Z\",\"counter\":0}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createSessionInvalidTimeTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         try {
             CloseableHttpResponse response = createTestSession("02-18-2019T20:00Z", "0");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             Assert.assertEquals(400, status);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createSessionInvalidCounterTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         try {
             CloseableHttpResponse response = createTestSession("2019-02-18T20:00Z", "zero");
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             Assert.assertEquals(400, status);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void createSessionMissingUserTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteAllUsers();
         try {
             String testTime = "2019-02-18T20:00Z";
             String counter = "0";
             CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
             String userID = getIdFromResponse(response);
             response.close();

             response = createProject("test1", userID);
             String projectID = getIdFromResponse(response);
             response.close();

             StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter +"}");
             response = createSession(userID + "100", projectID, input);

             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             String strResponse;
             Assert.assertEquals(404, status);
             EntityUtils.consume(response.getEntity());
             response.close();

         } finally {
             httpclient.close();
         }


     }

    // // *** Session PUT Test ***

    @Test
    public void updateSessionTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

        try {
            String testTime = "2019-02-18T20:00Z";
            String counter = "0";

            CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
            String userID = getIdFromResponse(response);
            response.close();

            response = createProject("test1", userID);
            String projectID = getIdFromResponse(response);
            response.close();

            StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter+"}");
            response = createSession(userID, projectID, input);
            String sessionID = getIdFromResponse(response);
            response.close();

            response = updateSession(userID, projectID, sessionID, "2019-02-18T20:00Z", "2019-02-18T20:15Z", "1");

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

            String expectedJson = "{\"id\":" + sessionID + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:15Z\",\"counter\":1}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionInvalidTimeTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

        try {
            String testTime = "2019-02-18T20:00Z";
            String counter = "0";

            CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
            String userID = getIdFromResponse(response);
            response.close();

            response = createProject("test1", userID);
            String projectID = getIdFromResponse(response);
            response.close();

            StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter+"}");
            response = createSession(userID, projectID, input);
            String sessionID = getIdFromResponse(response);
            response.close();

            response = updateSession(userID, projectID, sessionID, "02-18-2019T20:00Z", "2019-02-18T20:15Z", "1");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            Assert.assertEquals(400, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionInvalidCounterTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

        try {
            String testTime = "2019-02-18T20:00Z";
            String counter = "0";

            CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
            String userID = getIdFromResponse(response); //non-existent userID
            response.close();

            response = createProject("test1", userID);
            String projectID = getIdFromResponse(response);
            response.close();

            StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter+"}");
            response = createSession(userID, projectID, input);
            String sessionID = getIdFromResponse(response);
            response.close();

            response = updateSession(userID, projectID, sessionID, "2019-02-18T20:00Z", "2019-02-18T20:15Z", "one");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            Assert.assertEquals(400, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateMissingSessionTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();
        try {
            String testTime = "2019-02-18T20:00Z";
            String counter = "0";

            CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
            String userID = getIdFromResponse(response);
            response.close();


            response = createProject("test1", userID); 
            String projectID = getIdFromResponse(response);
            response.close();

            StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter+"}");
            response = createSession(userID, projectID, input);
            String sessionID = (getIdFromResponse(response)+1000);
            response.close();

            response = updateSession(userID, projectID, sessionID, "2019-02-18T20:00Z", "2019-02-18T20:15Z", "1");
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            Assert.assertEquals(404, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // // *** Report GET Test ***

    // @Test
    // public void getReportTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {

    //         CloseableHttpResponse response = createTestReport("2019-02-18T20:00Z", "0", "true", "true");
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

    //         String expectedJson = "{\"sessions\":[{\"startingTime\": \"2019-02-18T20:00Z\", \"endingTime\":\"2019-02-18T20:00Z\", \"hoursWorked\":0}], \"completedPomodoros\": 0, \"totalHoursWorkedOnProject\":0}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportInvalidTimeTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         CloseableHttpResponse response = createTestReport("02-18-2019T20:00Z", "0", "true", "true");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportInvalidBooleanTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         CloseableHttpResponse response = createTestReport("2019-02-18T20:00Z", "0", "true", "\"true\"");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportInvalidCounterTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         CloseableHttpResponse response = createTestReport("2019-02-18T20:00Z", "zero", "true", "true");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getReportMissingUserTest() throws Exception {
    //     httpclient = HttpClients.createDefault();

    //     try {
    //         String testTime = "2019-02-18T20:00Z";
    //         String counter = "0";
    //         CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
    //         String userID = (getIdFromResponse(response) + 1); //non-existent userID
    //         response.close();

    //         response = createProject("test1", userID);

    //         String projectID = getIdFromResponse(response);
    //         response.close();

    //         StringEntity input = new StringEntity("\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter);
    //         response = createSession(userID, projectID, input);
    //         response.close();

    //         response = getReport(userID, projectID, testTime, testTime, "true", "true");
    //         int status = response.getStatusLine().getStatusCode();
    //         HttpEntity entity;
    //         String strResponse;
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();

    //     } finally {
    //         httpclient.close();
    //     }
    // }

    //Generic helper methods
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


    //User helper methods
    private CloseableHttpResponse createUser(String firstName, String lastName, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstName + "\"," +
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
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstName + "\"," +
                "\"lastName\":\"" + lastName + "\"," +
                "\"email\":\"" + email + "\"}");
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

    //Project helper methods

    private CloseableHttpResponse createProject(String projectname, String userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
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

    private CloseableHttpResponse getProject(String projectId, String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getAllProjects(String userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId +"/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateProject(String userID, String projectID, String projectname) throws IOException  {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userID+"/projects/" + projectID);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," +
                "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }


    private CloseableHttpResponse deleteProject(String userID, String projectID) throws IOException  {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userID + "/projects/" + projectID);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    //Session helper methods

    private CloseableHttpResponse createSession(String userID, String projectID, StringEntity input) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userID + "/projects/" + projectID + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateSession(String userID, String projectID, String sessionID, String startTime, String endTime, String counter) throws IOException  {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userID+"/projects/" + projectID + "/sessions/" + sessionID);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"ud\":" + sessionID + "," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":\"" + counter + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createTestSession(String testTime, String counter) throws IOException, JSONException {
        CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
        String userID = getIdFromResponse(response);
        response.close();

        response = createProject("test1", userID);
        String projectID = getIdFromResponse(response);
        response.close();

        StringEntity input = new StringEntity("{\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter + "}");
        response = createSession(userID, projectID, input);
        return response;
    }
    
    // Report helper methods
    private CloseableHttpResponse getReport(String userID, String projectID, String from, String to, String includeCompletedPomodoros, String includeTotalHoursWorkedOnProject) throws IOException  {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userID + "/projects/" + projectID + "/report" + "?from=" + from + "&to=" + to + "&includeCompletedPomodoros=" +includeCompletedPomodoros+ "&includeTotalHoursWorkedOnProject="+includeTotalHoursWorkedOnProject);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;

    }

    private CloseableHttpResponse createTestReport(String testTime, String counter, String includeCompletedPomodoros, String includeTotalHoursWorkedOnProject) throws IOException, JSONException {
        CloseableHttpResponse response = createUser("Jane", "Doe", "123@abc.com");
        String userID = getIdFromResponse(response);
        response.close();

        response = createProject("test1", userID);
        String projectID = getIdFromResponse(response);
        response.close();

        StringEntity input = new StringEntity("\"startTime\": \"" + testTime + "\", \"endTime\": \""+ testTime +"\", \"counter\": " + counter);
        response = createSession(userID, projectID, input);
        response.close();


        response = getReport(userID, projectID, testTime, testTime, includeCompletedPomodoros, includeTotalHoursWorkedOnProject);
        return response;
    }
}
