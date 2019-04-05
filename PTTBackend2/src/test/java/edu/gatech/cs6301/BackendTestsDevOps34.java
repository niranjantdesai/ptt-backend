package edu.gatech.cs6301;

import java.io.IOException;
import java.util.Iterator;

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
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.skyscreamer.jsonassert.JSONAssert;

public class BackendTestsDevOps34 {

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
    public void createUserTest() throws Exception {
	    deleteAllUsers();

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

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
	        JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserTest() throws Exception {
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
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

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
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();
        String id;
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
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
    public void DeleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();
        String expectedJson;

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
        deleteAllUsers();
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
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
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateUser(updatedId, "Jane", "Wall", "jane@wall.com");
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
        deleteAllUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
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
        deleteAllUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
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

    @Test
    public void createDuplicatedUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteAllUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            response.close();
            // try to create the same user the second time
            response = createUser("John", "Doe", "john@doe.org");
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(409, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createProjectTest() throws Exception {
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");

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

            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateProjectTest() throws Exception {
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            String id = getIdFromResponse(response);
            response.close();

            response = updateProject(userId, id, "Programming");

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

            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Programming\",\"userId\":"+ userId +"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectTest() throws Exception {
        deleteAllUsers();
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
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

            String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // @Test
    // public void getAllProjectsTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     String userId = singleUserBeforeProjectTest();
    //     String id;
    //     String expectedJson = "";

    //     try {
    //         CloseableHttpResponse response = createProject(userId, "Homework");
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson += "[{\"id\":" + id + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}";
    //         response.close();

    //         response = createProject(userId, "Programming");
    //         // EntityUtils.consume(response.getEntity());
    //         id = getIdFromResponse(response);
    //         expectedJson += ",{\"id\":" + id + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}]";
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

    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

     // TODO: uncomment after implementing getAllProjects which is for the next deliverable
     @Test
     public void DeleteProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         String userId = singleUserBeforeProjectTest();
         String expectedJson;

         try {
             CloseableHttpResponse response = createProject(userId, "Homework");
             // EntityUtils.consume(response.getEntity());
             String deleteid = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             // deleteProject(userId, deleteid);

             response = deleteProject(userId, deleteid);
             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             expectedJson = "{\"id\":" + deleteid + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}";
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


     // TODO: uncomment after implementing getAllProjects which is for the next deliverable
     @Test
     public void CreateMultipleDeleteOneProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();
         String userId = singleUserBeforeProjectTest();
         String expectedJson = "";

         try {
             CloseableHttpResponse response = createProject(userId, "Homework");
             // EntityUtils.consume(response.getEntity());
             String deleteId = getIdFromResponse(response);
             response.close();

             response = createProject(userId, "Programming");
             // EntityUtils.consume(response.getEntity());
             String id = getIdFromResponse(response);
             expectedJson += "[{\"id\":" + id + ",\"projectname\":\"Programming\",\"userId\":"+ userId +"}]";
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

             String expectedJson2 = "{\"id\":" + deleteId + ",\"projectname\":\"Homework\",\"userId\":"+ userId +"}";
             JSONAssert.assertEquals(expectedJson2,strResponse, false);
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

             // expectedJson = "[]";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }



    // @Test
    // public void CreateMultipleUpdateOneProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     String userId = singleUserBeforeProjectTest();

    //     try {
    //         CloseableHttpResponse response = createProject(userId, "Homework");
    //         // EntityUtils.consume(response.getEntity());
    //         String id = getIdFromResponse(response);
    //         response.close();

    //         response = createProject(userId, "Programming");
    //         // EntityUtils.consume(response.getEntity());
    //         String updatedId = getIdFromResponse(response);
    //         response.close();

    //         int status;
    //         HttpEntity entity;
    //         String strResponse;

    //         response = updateProject(userId, updatedId, "Watching Udacity");
    //         String expectedJson = "{\"id\":" + id + ",\"projectname\":\"Watching Udacity\",\"userId\":"+ userId +"}";

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

    @Test
    public void getMissingProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject(userId, "Programming");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = id1 + id2; // making sure the ID is not present

            response = getProject(userId, missingId);

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
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createProject(userId, "Programming");
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

    // TODO: uncomment after implementing getAProject which is for the next deliverable
    @Test
    public void getProjectWithMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            String missingUserId = userId+userId;
            response = getProject(missingUserId, id);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createDuplicatedProjectsTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            response.close();
            // try to create the same user the second time
            response = createProject(userId, "Homework");
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(409, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateToDuplicatedProjectsTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();

        try {
            CloseableHttpResponse response = createProject(userId, "Homework");
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = createProject(userId, "Programming");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            response = updateProject(userId, updatedId, "Homework");

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private String singleUserBeforeProjectTest() throws IOException, JSONException{
        deleteAllUsers();
        CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
        HttpEntity entity=response.getEntity();
        String userId=getIdFromStringResponse(EntityUtils.toString(entity));
        response.close();
        return userId;
    }

    private String singleProjectBeforeSessionTest(String userId) throws IOException, JSONException
    {
        CloseableHttpResponse response = createProject(userId, "Homework");
        HttpEntity entity=response.getEntity();
        String projectId=getIdFromStringResponse(EntityUtils.toString(entity));
        response.close();
        return projectId;
    }

     @Test
     public void createSessionTest() throws Exception {
        deleteAllUsers();
         String userId = singleUserBeforeProjectTest();
         String projectId = singleProjectBeforeSessionTest(userId);
         try {
             CloseableHttpResponse response = createSession(userId, projectId,"2019-02-18T20:00Z","2019-02-18T20:00Z",0);

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
             JSONAssert.assertEquals(expectedJson, strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

        @Test
            public void updateSessionTest() throws Exception {
                deleteAllUsers();
                String userId = singleUserBeforeProjectTest();
                String projectId = singleProjectBeforeSessionTest(userId);
                try {
                    CloseableHttpResponse response = createSession(userId, projectId,"2019-02-18T20:00Z","2019-02-18T20:00Z",0);
                    String id = getIdFromResponse(response);
                    response.close();

                    response = updateSession(userId, projectId, id, "2019-02-18T20:00Z","2019-02-18T20:30Z",1);

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

                    String expectedJson = "{\"id\":" + id + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:30Z\",\"counter\":1 }";
                    JSONAssert.assertEquals(expectedJson,strResponse, false);
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } finally {
                    httpclient.close();
                }
            }


    @Test
    public void CreateMultipleUpdateOneSessionTest() throws Exception {
        httpclient = HttpClients.createDefault();
        String userId = singleUserBeforeProjectTest();
        String projectId = singleProjectBeforeSessionTest(userId);
        try {
            CloseableHttpResponse response = createSession(userId, projectId,"2019-02-18T20:00Z","2019-02-18T20:00Z",0);
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            response.close();

            response = createSession(userId, projectId,"2019-02-18T20:00Z","2019-02-18T20:30Z",1);
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateSession(userId, projectId,updatedId, "2019-02-18T20:00Z","2019-02-18T20:45Z",4);
            String expectedJson = "{\"id\":" + updatedId + ",\"startTime\":\"2019-02-18T20:00Z\",\"endTime\":\"2019-02-18T20:45Z\",\"counter\":4 }";

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
    public void updateSessionWithMissingProjectTest() throws Exception {
        deleteAllUsers();
        String userId = singleUserBeforeProjectTest();
        String projectId = singleProjectBeforeSessionTest(userId);
        try {
            CloseableHttpResponse response = createSession(userId, projectId,"2019-02-18T20:00Z","2019-02-18T20:00Z",0);
            String id = getIdFromResponse(response);
            response.close();

            deleteProject(userId, projectId);
            response=updateSession(userId, projectId,id, "2019-02-18T20:00Z","2019-02-18T20:45Z",4);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
            
        } finally {
            httpclient.close();
        }
    }

//     //Bharath
//     @Test
//     public void getReportOfAllSessionsTest() throws Exception {
//         httpclient = HttpClients.createDefault();
//         deleteAllUsers();
//         try {

//             //create a user
//             CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
//             response.close();
//             String userid = getIdFromResponse(response);

//             //create a project for the user
//             response = createProject(userid, "Homework");
//             response.close();
//             String projectid = getIdFromResponse(response);

//             //create a session for the project
//             response = createSession(userid, projectid, "2019-02-02T10:09Z", "2019-02-02T11:09Z", 2);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T12:09Z", "2019-02-02T12:39Z", 1);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T13:09Z", "2019-02-02T13:39Z", 1);
//             response.close();

//             String from = "1970-01-01T00:00Z";
//             String to = "2099-12-31T23:59Z";

//             response = getReports(userid, projectid, from, to, true, true);
//             int status = response.getStatusLine().getStatusCode();
//             HttpEntity entity;
//             String strResponse;
//             if (status == 200) {
//                 entity = response.getEntity();
//             }
//             else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);

//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//             String report_session = "[{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":1.00}," +
//                     "{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T11:09Z\"," +
//                     "\"hoursWorked\":0.50}," +
//                     "{\"startTime\":\"2019-02-02T13:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":\"0.50\"}]";
//             String expectedJson = "{\"sessions\":\"" + report_session + "\",\"completedPomodoros\":2,\"totalHoursWorkedOnProject\":1.00}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//         } finally {
//             httpclient.close();
//         }
//     }

//     @Test
//     public void getReportOfSessionsWithGivenTimeFrameTest() throws Exception {
//         httpclient = HttpClients.createDefault();
//         deleteAllUsers();

//         try {

//             //create a user
//             CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
//             response.close();
//             String userid = getIdFromResponse(response);

//             //create a project for the user
//             response = createProject(userid, "Homework");
//             response.close();
//             String projectid = getIdFromResponse(response);

//             //create a session for the project
//             response = createSession(userid, projectid, "2019-02-02T10:09Z", "2019-02-02T11:09Z", 2);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T12:09Z", "2019-02-02T12:39Z", 1);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T13:09Z", "2019-02-02T13:39Z", 1);
//             response.close();

//             String from = "2019-02-02T11:09Z";
//             String to = "2019-02-02T13:00Z";

//             response = getReports(userid, projectid, from, to, true, true);
//             int status = response.getStatusLine().getStatusCode();
//             HttpEntity entity;
//             String strResponse;
//             if (status == 200) {
//                 entity = response.getEntity();
//             }
//             else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);

//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//             String report_session = "[{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":1.00}," +
//                     "{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T11:09Z\"," +
//                     "\"hoursWorked\":0.50}]";
//             String expectedJson = "{\"sessions\":\"" + report_session + "\",\"completedPomodoros\":3,\"totalHoursWorkedOnProject\":1.50}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//         } finally {
//             httpclient.close();
//         }
//     }

//     @Test
//     public void getReportWithoutTotalHoursDetailsTest() throws Exception {
//         httpclient = HttpClients.createDefault();
//         deleteAllUsers();

//         try {

//             //create a user
//             CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
//             response.close();
//             String userid = getIdFromResponse(response);

//             //create a project for the user
//             response = createProject(userid, "Homework");
//             response.close();
//             String projectid = getIdFromResponse(response);

//             //create a session for the project
//             response = createSession(userid, projectid, "2019-02-02T10:09Z", "2019-02-02T11:09Z", 2);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T12:09Z", "2019-02-02T12:39Z", 1);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T13:09Z", "2019-02-02T13:39Z", 1);
//             response.close();

//             String from = "1970-01-01T00:00Z";
//             String to = "2099-12-31T23:59Z";

//             response = getReports(userid, projectid, from, to, true, false);
//             int status = response.getStatusLine().getStatusCode();
//             HttpEntity entity;
//             String strResponse;
//             if (status == 200) {
//                 entity = response.getEntity();
//             }
//             else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);

//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//             String report_session = "[{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":1.00}," +
//                     "{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T11:09Z\"," +
//                     "\"hoursWorked\":0.50}," +
//                     "{\"startTime\":\"2019-02-02T13:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":\"0.50\"}]";
//             String expectedJson = "{\"sessions\":\"" + report_session + "\",\"completedPomodoros\":2}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//         } finally {
//             httpclient.close();
//         }
//     }

//     @Test
//     public void getReportWithMissingUserTest() throws Exception {
//         httpclient = HttpClients.createDefault();
//         deleteAllUsers();

//         try {

//             //create a user
//             CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
//             response.close();
//             String userid = getIdFromResponse(response);

//             //create a project for the user
//             response = createProject(userid, "Homework");
//             response.close();
//             String projectid = getIdFromResponse(response);

//             //create a session for the project
//             response = createSession(userid, projectid, "2019-02-02T10:09Z", "2019-02-02T11:09Z", 2);
//             response.close();

//             //create another session for the project
//              response = createSession(userid, projectid, "2019-02-02T12:09Z", "2019-02-02T12:39Z", 1);
//             response.close();

//             //create another session for the project
//              response = createSession(userid, projectid, "2019-02-02T13:09Z", "2019-02-02T13:39Z", 1);
//             response.close();

//             String from = "1970-01-01T00:00Z";
//             String to = "2099-12-31T23:59Z";

//             response = getReports(userid+"xyz", projectid, from, to, false, false);
//             int status = response.getStatusLine().getStatusCode();
//             HttpEntity entity;
//             String strResponse;
//             if (status == 200) {
//                 entity = response.getEntity();
//             }
//             else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);

//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//             String report_session = "[{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":1.00}," +
//                     "{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T11:09Z\"," +
//                     "\"hoursWorked\":0.50}," +
//                     "{\"startTime\":\"2019-02-02T13:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":\"0.50\"}]";
//             String expectedJson = "{\"sessions\":\"" + report_session + "}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//         } finally {
//             httpclient.close();
//         }
//     }

//     @Test
//     public void getReportWithMissingProjectTest() throws Exception {
//         httpclient = HttpClients.createDefault();
//         deleteAllUsers();

//         try {

//             //create a user
//             CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
//             response.close();
//             String userid = getIdFromResponse(response);

//             //create a project for the user
//             response = createProject(userid, "Homework");
//             response.close();
//             String projectid = getIdFromResponse(response);

//             //create a session for the project
//             response = createSession(userid, projectid, "2019-02-02T10:09Z", "2019-02-02T11:09Z", 2);
//             response.close();

//             //create another session for the project
//              response = createSession(userid, projectid, "2019-02-02T12:09Z", "2019-02-02T12:39Z", 1);
//             response.close();

//             //create another session for the project
//             response = createSession(userid, projectid, "2019-02-02T13:09Z", "2019-02-02T13:39Z", 1);
//             response.close();

//             String from = "1970-01-01T00:00Z";
//             String to = "2099-12-31T23:59Z";

//             response = getReports(userid, projectid+"xyz", from, to, false, false);
//             int status = response.getStatusLine().getStatusCode();
//             HttpEntity entity;
//             String strResponse;
//             if (status == 200) {
//                 entity = response.getEntity();
//             }
//             else {
//                 throw new ClientProtocolException("Unexpected response status: " + status);
//             }
//             strResponse = EntityUtils.toString(entity);

//             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");
//             String report_session = "[{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":1.00}," +
//                     "{\"startingTime\":\"2019-02-02T10:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T11:09Z\"," +
//                     "\"hoursWorked\":0.50}," +
//                     "{\"startTime\":\"2019-02-02T13:09Z\"," +
//                     "\"endingTime\":\"2019-02-02T13:39Z\"," +
//                     "\"hoursWorked\":\"0.50\"}]";
//             String expectedJson = "{\"sessions\":\"" + report_session + "}";
//             JSONAssert.assertEquals(expectedJson,strResponse, false);
//             EntityUtils.consume(response.getEntity());
//             response.close();
//         } finally {
//             httpclient.close();
//         }
//     }

    
    private CloseableHttpResponse createProject(String userId, String projectname) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId +"/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateProject(String userId, String id, String projectname) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId +"/projects/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"projectname\":\"" + projectname + "\"," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getProject(String userId, String id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId +"/projects/" + id);
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

    private CloseableHttpResponse deleteProject(String userId, String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId +"/projects/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse createUser(String firstname, String familyname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + familyname + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateUser(String id, String firstname, String familyname, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstname + "\"," +
                "\"lastName\":\"" + familyname + "\"," +
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

    private String getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        return getIdFromStringResponse(strResponse);
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

    private CloseableHttpResponse createSession(String userId, String projectId,String startTime, String endTime, int counter) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId +"/projects/"+projectId+"/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + counter +"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateSession(String userId, String projectId,String sessionId, String startTime, String endTime, int counter) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId +"/projects/" + projectId+"/sessions/"+sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + counter +"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getReports(String userid, String projectid, String from, String to, Boolean completedPomodoros, Boolean totalHours) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userid + "/projects/" + projectid + "/report");
        httpRequest.addHeader("accept", "application/json");
        HttpParams params = new BasicHttpParams();
        params.setParameter("from", from);
        params.setParameter("to", to);
        params.setParameter("includeCompletedPomodoros", completedPomodoros);
        params.setParameter("includeTotalHoursWorkedOnProject", totalHours);
        httpRequest.setParams(params);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

}
