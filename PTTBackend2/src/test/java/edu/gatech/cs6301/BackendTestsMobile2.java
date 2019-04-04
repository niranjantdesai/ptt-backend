package edu.gatech.cs6301;

import java.io.IOException;
import java.util.Iterator;
import java.util.*;
import java.text.*;

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

public class BackendTestsMobile2 {

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
    private String getDateString(Date d){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm'Z'");
        df.setTimeZone(tz);
        String dat = df.format(d);
        return dat;
    }
    private Date addHoursToCurrentDate(int n){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.getInstance().HOUR_OF_DAY,n);
        Date d = c.getTime();
        return d;
    }
    
     //create session test
     @Test
     public void createSessionTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response =
                     createUser(0, "John", "Doe" , "john@doe.org");
             int userId = getIdFromResponse(response);
             response.close();

             response = addProject(0,"project", userId);
             int projectId = getIdFromResponse(response);
             response.close();

             String fromDate = "2019-02-18T18:00Z";
             String toDate = "2019-02-18T18:00Z";
             response = createSession(userId, projectId,0, fromDate, toDate, 0);
             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if (status == 201) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             String strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             int id = getIdFromStringResponse(strResponse);

             String expectedJson = "{\"id\":" + id + ",\"startTime\": \""+fromDate+"\",\"endTime\": \""+toDate+"\",\"counter\": 0}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }
     //Create session with invalid user
     @Test
     public void createSessionInvalidUserTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response =
                     createUser(0, "John", "Doe" , "john@doe.org");
             int userId = getIdFromResponse(response);
             response.close();

             response = addProject(0,"project", userId);
             int projectId = getIdFromResponse(response);
             response.close();

             String fromDate = "2019-02-18T18:00Z";
             String toDate = "2019-02-18T18:00Z";
             response = createSession(userId + 100, projectId,0, fromDate, toDate, 0);
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }
     //Create session with invalid project
     @Test
     public void createSessionInvalidProjectTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response =
                     createUser(0, "John", "Doe" , "john@doe.org");
             int userId = getIdFromResponse(response);
             response.close();

             response = addProject(0,"project", userId);
             int projectId = getIdFromResponse(response);
             response.close();

             String fromDate = "2019-02-18T18:00Z";
             String toDate = "2019-02-18T18:00Z";
             response = createSession(userId,projectId+100,0, fromDate, toDate, 0);
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     //Create session with invalid date string
     @Test
     public void createSessionInvalidDateTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response =
                     createUser(0, "John", "Doe" , "john@doe.org");
             int userId = getIdFromResponse(response);
             response.close();

             response = addProject(0,"project", userId);
             int projectId = getIdFromResponse(response);

             String fromDate = "2019-02-18T18:00Z";
             String toDate = "2019-02-18T18:00Z";
             response = createSession(userId,projectId,0,fromDate+"fsf", toDate, 0);
             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void updateSessionTest() throws Exception {
        deleteUsers();
        try {
            CloseableHttpResponse response =
                    createUser(0, "John", "Doe" , "john@doe.org");
            int userId = getIdFromResponse(response);
            response = addProject(0,"project", userId);
            int projectId = getIdFromResponse(response);

            //Dates from and to
            String fromDate = "2019-02-18T20:00Z";
            String toDate = "2019-02-18T21:00Z";
            String toDate2 = "2019-02-18T22:00Z";
            
            //create a session
            response = createSession(userId,projectId,0,fromDate, toDate, 0);
            int sessionId = getIdFromResponse(response);

            //update this session
            response = updateSession(userId,projectId,sessionId,fromDate, toDate2, 1);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);
            int id = getIdFromStringResponse(strResponse);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":" + id + ",\"startTime\": \""+fromDate+"\",\"endTime\": \""+toDate2+"\",\"counter\": 1}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    //update invalid session
    @Test
    public void updateSessionInvalidTest() throws Exception {
        deleteUsers();
        try {
            CloseableHttpResponse response =
                    createUser(0, "John", "Doe" , "john@doe.org");
            int userId = getIdFromResponse(response);
            response = addProject(0,"project", userId);
            int projectId = getIdFromResponse(response);

            String fromDate = "2019-02-18T20:00Z";
            String toDate = "2019-02-18T21:00Z";
            String toDate2 = "2019-02-18T22:00Z";

            //create a session
            response = createSession(userId,projectId,0,fromDate, toDate, 0);
            int sessionId = getIdFromResponse(response);

            //update this session
            response = updateSession(userId,projectId,sessionId+1,fromDate, toDate2, 1);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }


    //update session with invalid user
    @Test
    public void updateSessionInvalidUserTest() throws Exception {
        deleteUsers();
        try {
            CloseableHttpResponse response =
                    createUser(0, "John", "Doe" , "john@doe.org");
            int userId = getIdFromResponse(response);
            response = addProject(0,"project", userId);
            int projectId = getIdFromResponse(response);

            String fromDate = "2019-02-18T20:00Z";
            String toDate = "2019-02-18T21:00Z";
            String toDate2 = "2019-02-18T22:00Z";

            //create a session
            response = createSession(userId,projectId,0,fromDate, toDate, 0);
            int sessionId = getIdFromResponse(response);

            //update this session
            response = updateSession(userId+1,projectId,sessionId,fromDate, toDate2, 1);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }


    //update session with invalid project
    @Test
    public void updateSessionInvalidProjectTest() throws Exception {
        deleteUsers();
        try {
            CloseableHttpResponse response =
                    createUser(0, "John", "Doe" , "john@doe.org");
            int userId = getIdFromResponse(response);
            response = addProject(0,"project", userId);
            int projectId = getIdFromResponse(response);

            // //Dates from and to
            // Date d = addHoursToCurrentDate(1);
            // Date d2 = addHoursToCurrentDate(2);
            // String fromDate = getDateString(new Date());
            // String toDate = getDateString(d);
            // String toDate2 = getDateString(d2);

            String fromDate = "2019-02-18T20:00Z";
            String toDate = "2019-02-18T21:00Z";
            String toDate2 = "2019-02-18T22:00Z";

            //create a session
            response = createSession(userId,projectId,0,fromDate, toDate, 0);
            int sessionId = getIdFromResponse(response);

            //update this session
            response = updateSession(userId,projectId + 1,sessionId,fromDate, toDate2, 1);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    //update session with invalid date
    @Test
    public void updateSessionInvalidDateTest() throws Exception {
        deleteUsers();
        try {
            CloseableHttpResponse response =
                    createUser(0, "John", "Doe" , "john@doe.org");
            int userId = getIdFromResponse(response);
            response = addProject(0,"project", userId);
            int projectId = getIdFromResponse(response);

            //Dates from and to
            String fromDate = "2019-02-18T20:00Z";
            String toDate = "2019-02-18T21:00Z";
            String toDate2 = "2019-02-18T22:00Z";

            //create a session
            response = createSession(userId,projectId,0,fromDate, toDate, 0);
            int sessionId = getIdFromResponse(response);

            //update this session
            response = updateSession(userId,projectId ,sessionId,fromDate+"sdf", toDate2, 1);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(400, status);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }


    // @Test
    // public void getReportTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response =
    //                 createUser(0, "John", "Doe" , "john@doe.org");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"project", userId);
    //         int projectId = getIdFromResponse(response);

    //         //Dates from and to
    //         Date d = addHoursToCurrentDate(1);
    //         String fromDate = getDateString(new Date());
    //         String toDate = getDateString(d);

    //         //create a session
    //         response = createSession(userId,projectId,0,fromDate, toDate, 0);
    //         int sessionId = getIdFromResponse(response);

    //         response = getReport(userId,projectId,fromDate,toDate,false,true);

    //         int id = getIdFromResponse(response);

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
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \""+fromDate+"\",\"endingTime\": \""+toDate+"\",\"hoursWorked\": 1 }],\"totalHoursWorkedOnProject\": 1}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }
    // //get report with completed pomodoros
    // @Test
    // public void getReportIncompletePomodoroTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response =
    //                 createUser(0, "John", "Doe" , "john@doe.org");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"project", userId);
    //         int projectId = getIdFromResponse(response);

    //         //Dates from and to
    //         Date d = addHoursToCurrentDate(1);
    //         String fromDate = getDateString(new Date());
    //         String toDate = getDateString(d);

    //         //create a session
    //         response = createSession(userId,projectId,0,fromDate, null, 0);
    //         int sessionId = getIdFromResponse(response);

    //         response = getReport(userId,projectId,fromDate,toDate,true,true);

    //         int id = getIdFromResponse(response);

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
    //         String expectedJson = "{\"sessions\": [{\"startingTime\": \""+fromDate+"\",\"endingTime\": \""+toDate+"\",\"hoursWorked\": 1 }],\"completedPomodoros\": 0,\"totalHoursWorkedOnProject\": 1}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }
    // //get report invalid user
    // @Test
    // public void getReportInvalidUserTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response =
    //                 createUser(0, "John", "Doe" , "john@doe.org");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"project", userId);
    //         int projectId = getIdFromResponse(response);

    //         //Dates from and to
    //         Date d = addHoursToCurrentDate(1);
    //         String fromDate = getDateString(new Date());
    //         String toDate = getDateString(d);

    //         //create a session
    //         response = createSession(userId,projectId,0,fromDate, toDate, 0);
    //         int sessionId = getIdFromResponse(response);

    //         response = getReport(userId+1,projectId,fromDate,toDate,false,false);

    //         int id = getIdFromResponse(response);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }
    // //get report invalid project
    // @Test
    // public void getReportInvalidProjectTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response =
    //                 createUser(0, "John", "Doe" , "john@doe.org");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"project", userId);
    //         int projectId = getIdFromResponse(response);

    //         //Dates from and to
    //         Date d = addHoursToCurrentDate(1);
    //         String fromDate = getDateString(new Date());
    //         String toDate = getDateString(d);

    //         //create a session
    //         response = createSession(userId,projectId,0,fromDate, toDate, 0);
    //         int sessionId = getIdFromResponse(response);

    //         response = getReport(userId,projectId+1,fromDate,toDate,false,false);

    //         int id = getIdFromResponse(response);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }
    // //get report invalid date
    // @Test
    // public void getReportInvalidDateTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response =
    //                 createUser(0, "John", "Doe" , "john@doe.org");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"project", userId);
    //         int projectId = getIdFromResponse(response);

    //         //Dates from and to
    //         Date d = addHoursToCurrentDate(1);
    //         String fromDate = getDateString(new Date());
    //         String toDate = getDateString(d);

    //         //create a session
    //         response = createSession(userId,projectId,0,fromDate, toDate, 0);
    //         int sessionId = getIdFromResponse(response);

    //         response = getReport(userId,projectId,fromDate+"adf",toDate,false,false);

    //         int id = getIdFromResponse(response);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // /////////////////////////////////////////////////////////////////
    // //******Project Test Start Here*********************

    // @Test
    // public void getAllProjectsTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     String expectedJson = "";

    //     try {
    //         CloseableHttpResponse response = createUser(0,"na","nv","nawdd@gmail.com");
    //         int userId = getIdFromResponse(response);
    //         response = addProject(0,"projectnA", userId);
    //         int id1 = getIdFromResponse(response);
    //         response.close();

    //         response = addProject(0,"projectnB", userId);
    //         int id2 = getIdFromResponse(response);
    //         response.close();

    //         expectedJson += "[{\"id\":\"" + id1 + "\",\"projectname\":\"projectA\",\"userId\":\""+userId+"\"}";
    //         expectedJson += ",{\"id\":\"" + id2 + "\",\"projectname\":\"projectA\",\"userId\":\""+userId+"\"}]";

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

    // @Test
    // public void getAllProjectsBadRequestTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response;

    //         response = getAllProjects(-1);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void getAllProjectsUserNotFoundTest() throws Exception {
    //     httpclient = HttpClients.createDefault();
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response;
    //         deleteUser(""+10);
    //         response = getAllProjects(10);
    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }


     @Test
     public void addProjectTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response = createUser(0,"Kevin","Li","abcdefg@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();

             response =addProject(0,"testProject",userId);

             int status = response.getStatusLine().getStatusCode();
             HttpEntity entity;
             if (status == 201) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             String strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             int id = getIdFromStringResponse(strResponse);

             String expectedJson = "{\"id\":" + id + ",\"projectname\":\"testProject\",\"userId\":" + userId + "}";
             JSONAssert.assertEquals(expectedJson,strResponse, false);
             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void addProjectNullProjectNameTest() throws Exception {
         deleteUsers();

         try {
             //create user here called user1
             CloseableHttpResponse response = createUser(0,"Huajun","Guo","hijklmn@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();

             response =addProject(0,userId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void addProjectUserNotFindTest() throws Exception {
         deleteUsers();
         try {

             CloseableHttpResponse response = createUser(0,"Huajun","Guo","hijklmn@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();

             response =addProject(0,"testProject",userId+1);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void addProjectResourceConflictTest() throws Exception {
         deleteUsers();
         try {
             CloseableHttpResponse response = createUser(0,"Kevin","Li","abcdefg@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();

             addProject(0,"testProject",userId);
             response = addProject(0,"testProject",userId);

             int status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(409, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }


    @Test
    public void getProjectTest() throws Exception {
        httpclient = HttpClients.createDefault();

        String expectedJson = "";
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(0,"nf","ng","naawdd@gmail.com");
            int userId = getIdFromResponse(response);
            response.close();
            response = addProject(0,"projectA", userId);
            int id1 = getIdFromResponse(response);
            response.close();

            expectedJson += "{\"id\":" + id1 + ",\"projectname\":\"projectA\",\"userId\":"+userId+"}";

            response = getProject(userId, id1);
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
    public void getProjectBadRequestTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            CloseableHttpResponse response;

            response = getProject(-1, -1);
            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getProjectUserNotFoundTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            CloseableHttpResponse response;
            deleteUser(""+10);
            response = getProject(10, 1);
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
    //     deleteUsers();
    //     try {

    //         CloseableHttpResponse response = createUser(0,"nf","ng","naawdd@gmail.com");
    //         int userId = getIdFromResponse(response);
    //         response.close();
    //         response = addProject(0,"projectnA", userId);
    //         int id = getIdFromResponse(response);
    //         response.close();


    //         response = updateProject(id, "newProjectName",userId,id);

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

    //         String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"newProjectName\",\"userId\":\""+userId+"\"}";
    //         JSONAssert.assertEquals(expectedJson,strResponse, false);
    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void updateProjectBadRequestTest() throws Exception {
    //     deleteUsers();
    //     try {

    //         CloseableHttpResponse response;
    //         response = updateProject(-1, null, -1,-1);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(400, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }



    // @Test
    // public void updateProjectProjectNotFoundTest() throws Exception {
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response = createUser(0,"nf","ng","naawdd@gmail.com");
    //         int userId = getIdFromResponse(response);
    //         response.close();
    //         deleteProject(userId,5);

    //         response = updateProject(5, "theProjectName", userId,5);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }

    // @Test
    // public void updateProjectUserNotFoundTest() throws Exception {
    //     deleteUsers();
    //     try {
    //         CloseableHttpResponse response;
    //         deleteUser(""+3);

    //         response = updateProject(5, "theProjectName", 3,5);

    //         int status = response.getStatusLine().getStatusCode();
    //         Assert.assertEquals(404, status);

    //         EntityUtils.consume(response.getEntity());
    //         response.close();
    //     } finally {
    //         httpclient.close();
    //     }
    // }


     @Test
     public void DeleteProjectTest() throws Exception {
         httpclient = HttpClients.createDefault();

         String expectedJson = null;
         deleteUsers();
         try {
             CloseableHttpResponse response = createUser(0,"gea","gbraz","ngrwsdd@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();
             response = addProject(0,"projectnA", userId);
             int id = getIdFromResponse(response);
             response.close();

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject(userId, id);

             status = response.getStatusLine().getStatusCode();
             if (status == 200) {
                 entity = response.getEntity();
             } else {
                 throw new ClientProtocolException("Unexpected response status: " + status);
             }
             strResponse = EntityUtils.toString(entity);

             System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

             expectedJson = "{\"id\":" + id + ",\"projectname\":\"projectnA\",\"userId\":"+userId+"}";
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

     @Test
     public void DeleteProjectBadRequestTest() throws Exception {
         httpclient = HttpClients.createDefault();

         String expectedJson = null;
         deleteUsers();
         try {
             CloseableHttpResponse response;

             int status;
             HttpEntity entity;
             String strResponse;

             response = deleteProject("abc", "abc");

             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(400, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void DeleteProjectProjectNotFoundTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();

         try {
             CloseableHttpResponse response = createUser(0,"gea","gbraz","ngrwsdd@gmail.com");
             int userId = getIdFromResponse(response);
             response.close();
             deleteProject(userId,5);

             int status;

             response = deleteProject(userId, 5);

             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

     @Test
     public void DeleteProjectUserNotFoundTest() throws Exception {
         httpclient = HttpClients.createDefault();
         deleteUsers();
         try {
             CloseableHttpResponse response;

             deleteUser(""+7);
             int status;

             response = deleteProject(7, 1);

             status = response.getStatusLine().getStatusCode();
             Assert.assertEquals(404, status);

             EntityUtils.consume(response.getEntity());
             response.close();
         } finally {
             httpclient.close();
         }
     }

    @Test
    public void createUserTest() throws Exception {
        //deleteContacts();
        try {
            deleteUsers();

            CloseableHttpResponse response = createUser(1, "John","Doe", "john@doe.org");

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            int id = getIdFromStringResponse(strResponse);

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
        //  deleteContacts();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1,"John", "Doe", "john@doe.org");
            int id = getIdFromResponse(response);

            response.close();

            response = updateUser(id, "Tom", "Doe", "tom@doe.org" );

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
        //  deleteContacts();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1,"John", "Doe", "john@doe.org");
            int id = getIdFromResponse(response);
            response.close();

            response = getUser(""+id);

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
        // deleteContacts();
        int id = 0;
        String expectedJson = "";
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1,"John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id + ",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser(2, "Jane", "Wall", "jane@wall.com");
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
        //  deleteContacts();
        String expectedJson = null;
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1, "John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            int deleteid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(""+deleteid);

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
        //  deleteContacts();
        String expectedJson = "";
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1, "John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            int deleteId = getIdFromResponse(response);
            response.close();

            response = createUser(2, "Jane", "Wall" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            int id = getIdFromResponse(response);
            expectedJson += "[{\"id\":" + id + ",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(""+deleteId);

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
        //  deleteContacts();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1, "John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            int id = getIdFromResponse(response);
            response.close();

            response = createUser(2, "Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            int updatedId = getIdFromResponse(response);
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

            response = getUser(""+updatedId);

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
        // deleteContacts();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1, "John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            int id1 = getIdFromResponse(response);
            response.close();

            response = createUser(2, "Jane", "Wall" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            int id2 = getIdFromResponse(response);
            response.close();

            int missingId = 100009999; // making sure the ID is not present

            response = getUser(""+missingId);

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
        // deleteContacts();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser(1, "John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            int id1 = getIdFromResponse(response);
            response.close();

            response = createUser(2, "Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            int id2 = getIdFromResponse(response);
            response.close();

            int missingId = 1000009999; // making sure the ID is not present

            response = deleteUser(""+missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }



    ////////////////////////////////////////////////////////////////////////////
    //****My Code Start Here******************************************************

    //get all projects of a given userId
    private CloseableHttpResponse getAllProjects(int userId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userId+"/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //add a new project
    private CloseableHttpResponse addProject(int id, String projectname, int userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userId+"/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + id + "," +
                "\"projectname\":\"" + projectname + "\"," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }
    //add a new project without projectname
    private CloseableHttpResponse addProject(int id, int userId) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userId+"/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + id + "," +
                "\"userId\":" + userId + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //get a project by userId and projectId
    private CloseableHttpResponse getProject(int userId, int projectId) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userId+"/projects/"+projectId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //update a project
    private CloseableHttpResponse updateProject(int id, String projectname, int userId, int projectId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userId+"/projects/"+projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":\"" + id + "\"," +
                "\"projectname\":\"" + projectname + "\"," +
                "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //delete a project
    private CloseableHttpResponse deleteProject(int userId, int projectId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/"+userId+"/projects/"+projectId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    //delete a project incorrectly
    private CloseableHttpResponse deleteProject(String userId, String projectId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/"+userId+"/projects/"+projectId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private int getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        int id = getIdFromStringResponse(strResponse);
        return id;
    }

    private int getIdFromStringResponse(String strResponse) throws JSONException {
        JSONObject object = new JSONObject(strResponse);

        int id = -1;
        Iterator<String> keyList = object.keys();
        while (keyList.hasNext()){
            String key = keyList.next();
            if (key.equals("id")) {
                id = Integer.valueOf(object.get(key).toString());
            }
        }
        return id;
    }

    private List<Integer> getAllIds(CloseableHttpResponse response){
        List<Integer> l = new ArrayList<Integer>();
        try{

            HttpEntity entity = response.getEntity();
            String strResponse = EntityUtils.toString(entity);

            JSONArray users = new JSONArray(strResponse);
            for ( int i = 0; i< users.length(); i++){
                JSONObject user = users.getJSONObject(i);
                Iterator<String> keyList = user.keys();
                while (keyList.hasNext()){
                    String key = keyList.next();
                    if (key.equals("id")) {
                        l.add(Integer.valueOf(user.get(key).toString()));
                    }
                }
            }
        } catch (Exception e)
        {
            System.out.println("Error in deleting projects and users");
        }
        return l;
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

    //*****************My Code End Here**************************************
    /////////////////////////////////////////////////////////////////////////

    //create session
    private CloseableHttpResponse createSession(int userId, int projectId, int id, String startTime ,String endTime, int counter) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userId+"/projects/"+projectId+"/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + id + "," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + counter + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //update session
    private CloseableHttpResponse updateSession(int userId, int projectId, int id, String startTime ,String endTime, int counter) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userId+"/projects/"+projectId+"/sessions/"+id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"id\":" + id + "," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + counter + "}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //get report
    private CloseableHttpResponse getReport(int userId, int projectId, String from,String to, boolean includeCompletedPomodoros ,boolean includeTotalHoursWorkedOnProject) throws IOException {
        String reqString = baseUrl + "/users/"+userId+"/projects/"+projectId+"/report";
        reqString += "?from="+from+"&to="+to;
        reqString += "&includeTotalHoursWorked="+includeTotalHoursWorkedOnProject;
        reqString += "&includeCompletedPomodoros="+includeCompletedPomodoros;
        HttpGet httpRequest = new HttpGet(reqString);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    /**ENDS HERE */
    /* MY CODE STARTS HERE */
    private CloseableHttpResponse createUser(int id, String firstName, String lastName, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
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

    private CloseableHttpResponse updateUser(int id, String firstName, String lastName, String email) throws IOException {
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
    /*ENDS HERE*/
}
