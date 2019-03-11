package edu.gatech.cs6301;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.Iterator;

public class BackendTestsBackend4 {

    private String baseUrl = "http://localhost:8080";
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
	deleteUsers();
        try {
            APIResponse createResponse =
                createUser("John", "Doe","john@doe.org");
            createResponse.checkStatusAndPrintResponse(201);

            String expectedJson = "{\"id\":\"" + createResponse.getId() + "\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
	        createResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateUserTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse updateResponse = updateUser(createResponse.getId(), "Tom", "Doe", "john@doe.org");
            updateResponse.checkStatusAndPrintResponse(200);

            String expectedJson = "{\"id\":\"" + updateResponse.getId() + "\",\"firstName\":\"Tom\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            updateResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse getResponse = getUser(createResponse.getId());
            getResponse.checkStatusAndPrintResponse(200);

            String expectedJson = "{\"id\":\"" + getResponse.getId() + "\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            getResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllUsersTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");
            String expectedJson = "[{\"id\":\"" + createResponse.getId() + "\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";

            createResponse = createUser("Jane", "Wall", "jane@wall.com");
            expectedJson += ",{\"id\":\"" + createResponse.getId() + "\",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";

            APIResponse getResponse = getAllUsers();
            getResponse.checkStatusAndPrintResponse(200);
            getResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void DeleteUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse deleteResponse = deleteUser(createResponse.getId());
            deleteResponse.checkStatusAndPrintResponse(200);

            String expectedJson = "{\"id\":\"" + deleteResponse.getId() + "\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            deleteResponse.checkEqual(expectedJson);

            APIResponse getResponse = getAllUsers();
            getResponse.checkStatusAndPrintResponse(200);
            expectedJson = "[]";
            getResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleDeleteOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponseJohn = createUser("John", "Doe", "john@doe.org");

            APIResponse createResponseJane = createUser("Jane", "Wall", "jane@wall.com");
            String expectedJsonJane = "[{\"id\":\"" + createResponseJane.getId() + "\",\"firstName\":\"Jane\",\"lastName\":\"Wall\",\"email\":\"jane@wall.com\"}]";

            APIResponse deleteResponseJane = deleteUser(createResponseJane.getId());
            deleteResponseJane.checkStatusAndPrintResponse(200);

            String expectedJsonJohn = "{\"id\":\"" + createResponseJohn.getId() + "\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@doe.org\"}";
            deleteResponseJane.checkEqual(expectedJsonJane);

            APIResponse getResponse = getAllUsers();
            getResponse.checkStatusAndPrintResponse(200);
            getResponse.checkEqual(expectedJsonJohn);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void CreateMultipleUpdateOneUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            createUser("John", "Doe", "john@doe.org");

            APIResponse createResponseJane = createUser("Jane", "Wall", "jane@wall.com");

            APIResponse updateResponse = updateUser(createResponseJane.getId(), "Jane", "Ball", "jane@wall.com");
            updateResponse.checkStatusAndPrintResponse(200);
            String expectedJson = "{\"id\":\"" + updateResponse.getId() + "\",\"firstName\":\"Jane\",\"lastName\":\"Ball\",\"email\":\"jane@wall.com\"}";
            updateResponse.checkEqual(expectedJson);

            APIResponse getResponse = getUser(updateResponse.getId());
            getResponse.checkStatusAndPrintResponse(200);
            getResponse.checkEqual(expectedJson);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponse1 = createUser("John", "Doe", "john@doe.org");
            APIResponse createResponse2 = createUser("Jane", "Wall", "jane@wall.com");

            Integer missingId = createResponse1.getId() + createResponse2.getId(); // making sure the ID is not present
            APIResponse getResponse = getUser(missingId);
            getResponse.checkStatus(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteMissingUserTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            APIResponse createResponse1 = createUser("John", "Doe", "john@doe.org");
            APIResponse createResponse2 = createUser("Jane", "Wall", "jane@wall.com");

            Integer missingId = createResponse1.getId() + createResponse2.getId(); // making sure the ID is not present
            APIResponse deleteResponse = deleteUser(missingId);
            deleteResponse.checkStatus(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void createMultipleUserSameEmailTest() throws Exception {
    deleteUsers();
        try {
            APIResponse createResponse =
                createUser("John", "Doe","john@doe.org");
            createResponse.checkStatusAndPrintResponse(201);

            APIResponse createResponse2 = 
                createUser("John", "Duh","john@doe.org");
            createResponse2.checkStatusAndPrintResponse(409);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }


    @Test
    public void createUserIncorrectInputTest() throws Exception {
    deleteUsers();
        try {
            APIResponse createResponse =
                createUser("John", "Doe","johnny");
            createResponse.checkStatusAndPrintResponse(400);

        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateMissingUserTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse updateResponse = updateUser(-1, "Tom", "Doe", "tom@doe.org");
            updateResponse.checkStatusAndPrintResponse(400);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createSessionTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            String input = new String("{\"id\":\"" +  createSession.getId()+ "\"," +
                "\"startTime\":\"" + "2019-02-18T20:00Z" + "\"," +
                "\"endTime\":\"" + "2019-02-19T20:00Z" + "\"" +
                "counter:"+1);
            createSession.checkEqual(input);
            createSession.checkStatusAndPrintResponse(201);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createSessionNoUserTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), -1, "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            createSession.checkStatusAndPrintResponse(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createSessionNoProjectTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(-1, createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            createSession.checkStatusAndPrintResponse(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createSessionInvalidInputTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "", "", 1);

            createSession.checkStatusAndPrintResponse(400);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionNoUserTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);
            //int userID, int projectID, int sessionID, String startTime, String endTime, int counter
            APIResponse updateSession = updateSession(-1, createProject.getId(), createSession.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            updateSession.checkStatusAndPrintResponse(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionNoProjectTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);
            //int userID, int projectID, int sessionID, String startTime, String endTime, int counter
            APIResponse updateSession = updateSession(createResponse.getId(), -1, createSession.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            updateSession.checkStatusAndPrintResponse(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionNoSessionTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);
            //int userID, int projectID, int sessionID, String startTime, String endTime, int counter
            APIResponse updateSession = updateSession(createResponse.getId(), createProject.getId(), -1, "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);

            updateSession.checkStatusAndPrintResponse(404);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateSessionInvalidInputTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");

            APIResponse createProject = createProject("project1", createResponse.getId());

            //(int projectID,  int userID, String startTime, String endTime, int counter)
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-19T20:00Z", 1);
            APIResponse updateSession = updateSession(createResponse.getId(), createProject.getId(), createSession.getId(), "", "", 1);


            updateSession.checkStatusAndPrintResponse(400);
        } catch (Exception e){
        } finally {
            httpclient.close();
        }
    }

    //getReport(int userID, int projectID)
    @Test
    public void createReportFalseParameterTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");
            APIResponse createProject = createProject("project1", createResponse.getId());
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);
            APIResponse getReport = getReport(createResponse.getId(), createProject.getId());

            String input = new String("{\"sessions\":\"[{\"startingTime\":\"" + 
                "2019-02-18T20:00Z" + "\"," +
                "\"endingTime\":\"" + "2019-02-18T21:00Z" + "\"" +
                "\"hoursWorked\":\"" + 1 + "}]" +"}");
            getReport.checkEqual(input);
            getReport.checkStatusAndPrintResponse(200);
        } catch (Exception e){

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createReportNoUserTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");
            APIResponse createProject = createProject("project1", createResponse.getId());
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);
            APIResponse getReport = getReport(-1, createProject.getId());
            getReport.checkStatusAndPrintResponse(404);
        } catch (Exception e){

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createReportNoProjectTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");
            APIResponse createProject = createProject("project1", createResponse.getId());
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);
            APIResponse getReport = getReport(createResponse.getId(), -1);
            getReport.checkStatusAndPrintResponse(404);
        } catch (Exception e){

        } finally {
            httpclient.close();
        }
    }

    @Test
    public void createReportTrueParameterTest() throws Exception {
        deleteUsers();
        try {
            APIResponse createResponse = createUser("John", "Doe", "john@doe.org");
            APIResponse createProject = createProject("project1", createResponse.getId());
            APIResponse createSession = createSession(createProject.getId(), createResponse.getId(), "2019-02-18T20:00Z", "2019-02-18T21:00Z", 1);
            APIResponse getReport = getReport(createResponse.getId(), createProject.getId());

            String input = new String("{\"sessions\":\"[{\"startingTime\":\"" + 
                "2019-02-18T20:00Z" + "\"," +
                "\"endingTime\":\"" + "2019-02-18T21:00Z" + "\"" +
                "\"hoursWorked\":\"" + 1 + "}]," +
                "\"completedPomodoros\":" + 1 +
                "\"totalHoursWorkedOnProject\":" + 1 + "}");
            getReport.checkEqual(input);
            getReport.checkStatusAndPrintResponse(200);
        } catch (Exception e){

        } finally {
            httpclient.close();
        }
    }

    /*
    @Authors: James Gangavarapu, Issac Wong
     */


    //Testing Post
    @Test
    public void createProjectTest()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int id = projResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            projResponse.checkStatusAndPrintResponse(201);
            projResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    @Test
    public void createProjectTest400()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID+1);
            int id = projResponse.getId();
            projResponse.checkStatusAndPrintResponse(400);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    @Test
    public void createProjectTest409()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            createProject("testProject1", userID);
            APIResponse projResponse = createProject("testProject1", userID);
            int id = projResponse.getId();

            //String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            projResponse.checkStatusAndPrintResponse(409);
            //projResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }


    //Testig GET all Projects for User
    //The online API YAML specification for this the return body is not reutrn array of string for projects
    //Testing GET
    @Test
    public void getProjectTest()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse getProjResponse = getAllProjectsOfUser(userID);

            int id = getProjResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            getProjResponse.checkStatusAndPrintResponse(200);
            getProjResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    @Test
    public void getProjectTest404()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse getProjResponse = getAllProjectsOfUser(userID+1);

            int id = getProjResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            getProjResponse.checkStatusAndPrintResponse(404);
            getProjResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }




    //Testing GET Project By User
    @Test
    public void getOneProjectTest()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse getProjResponse = getProjectByUser(userID, projectID);

            int id = getProjResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            getProjResponse.checkStatusAndPrintResponse(200);
            getProjResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    public void getOneProjectTest404()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse getProjResponse = getProjectByUser(userID+1, projectID);
            getProjResponse.checkStatusAndPrintResponse(404);

            getProjResponse = getProjectByUser(userID, projectID+1);
            getProjResponse.checkStatusAndPrintResponse(404);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }




    //Testing PUT
    @Test
    public void updateProjectTest()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse putProjResponse = updateProject(userID, projectID, "testProjectChange");

            int id = putProjResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProjectChange\",\"userId\":"+userID;

            putProjResponse.checkStatusAndPrintResponse(200);
            putProjResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    //Duplicate Project Name
    @Test
    public void updateProjectTest400()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            createProject("testProject2", userID);
            int projectID = projResponse.getId();

            APIResponse putProjResponse = updateProject(userID, projectID, "testProject2");
            putProjResponse.checkStatusAndPrintResponse(400);



        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }


    //Invalid User ID or Project ID
    @Test
    public void updateProjectTest404()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse putProjResponse = updateProject(userID+1, projectID, "testProject2");
            putProjResponse.checkStatusAndPrintResponse(404);

            putProjResponse = updateProject(userID, projectID+1, "testProject2");
            putProjResponse.checkStatusAndPrintResponse(404);
            

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }



    //Testing DELETE
    @Test
    public void deleteProjectTest()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse delProjResponse = deleteUsersProject(userID, projectID);

            int id = delProjResponse.getId();

            String expectedJson = "{\"id\":\"" + id + "\",\"projectname\":\"testProject1\",\"userId\":"+userID;

            delProjResponse.checkStatusAndPrintResponse(200);
            delProjResponse.checkEqual(expectedJson);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }

    @Test
    public void deleteProjectTest404()throws Exception {
        deleteUsers();

        try{
            APIResponse userResponse = createUser("John","Doe","john.doe@gatech.edu");
            int userID = userResponse.getId();
            APIResponse projResponse = createProject("testProject1", userID);
            int projectID = projResponse.getId();

            APIResponse delProjResponse = deleteUsersProject(userID+1, projectID);
            delProjResponse.checkStatusAndPrintResponse(404);

            delProjResponse = deleteUsersProject(userID, projectID+1);
            delProjResponse.checkStatusAndPrintResponse(404);

        }catch (Exception e){

        }finally {
            httpclient.close();
        }

    }


    private APIResponse createUser(String firstName, String lastName, String email) throws Exception {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstName + "\"," +
            "\"lastName\":\"" + lastName + "\"," +
            "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }

    private APIResponse updateUser(Integer id, String firstName, String lastName, String email) throws Exception {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        StringEntity input = new StringEntity("{\"firstName\":\"" + firstName + "\"," +
            "\"lastName\":\"" + lastName + "\"," +
            "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }

    private APIResponse getUser(Integer id) throws Exception {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + id);
        return executeRequest(httpRequest);
    }

    private APIResponse getAllUsers() throws Exception {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        return executeRequest(httpRequest);
    }

    private APIResponse deleteUser(Integer id) throws Exception {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + id);
        return executeRequest(httpDelete);
    }

    private APIResponse deleteUsers() throws Exception {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users");
        return executeRequest(httpDelete);
    }

    private APIResponse executeRequest(HttpUriRequest httpRequest) throws Exception {
        httpRequest.addHeader("accept", "application/json");
        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return new APIResponse(response);
    }

        /*
    @Author: James Gangavarapu
    * */

    //Get Projects /users/{userId}/projects
    private APIResponse getAllProjectsOfUser(int id) throws Exception {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + id+"/projects");
        return executeRequest(httpRequest);
    }

    ///Post users/{userId}/projects
    //create a new project
     /*{
                "id": 0,
                "projectname": "string",
                "userId": 0
    }*/
    private APIResponse createProject( String projectName, int userID) throws Exception {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userID+"/projects");

        StringEntity input = new StringEntity("{\"id\":\"" + userID + "\"," +
            "\"projectname\":\"" + projectName + "\"," +
            "\"userId\":\"" + userID + "\"");

        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }



    //GET /users/{userId}/projects/{projectId}
    //Get project by ID for a given user
    /*{
        "id": 0,
            "projectname": "string",
            "userId": 0
    }*///Cannot have Body in GET
    private APIResponse getProjectByUser(int userID, int projectID) throws Exception {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userID+"/projects/"+projectID);
        return executeRequest(httpRequest);
    }

    //PUT /users/{userId}/projects/{projectId}
    //Update a project and return the updated object
    /*Request and Response Body
    {
      "id": 0,
      "projectname": "string",
      "userId": 0
    }
    * */
    private APIResponse updateProject(int userID, int projectID, String projectName) throws Exception {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userID+"/projects/"+projectID);
        StringEntity input = new StringEntity("{\"id\":\"" + projectID + "\"," +
            "\"projectname\":\"" + projectName + "\"," +
            "\"userId\":\"" + userID + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }


    //DELETE /users/{userId}/projects/{projectId}
    //Delete a project and return the deleted object
    //Response Body:
    // {
    //  "id": 0,
    //  "projectname": "string",
    //  "userId": 0
    //  }
    private APIResponse deleteUsersProject(int userID, int projectID) throws Exception {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userID+"/projects/"+projectID);
        return executeRequest(httpDelete);
    }



    //POST /users/{userId}/projects/{projectId}/sessions
    //Add a new session and return the newly created object
    //Request and Response
    //{
    //  "id": 0,
    //  "startTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //  "endTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //  "counter": 0
    //}
    private APIResponse createSession(int projectID,  int userID, String startTime, String endTime, int counter) throws Exception {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/"+userID+"/projects/"+projectID+"/sessions");

        StringEntity input = new StringEntity("{\"id\":\"" +  0+ "\"," +
            "\"startTime\":\"" + startTime + "\"," +
            "\"endTime\":\"" + endTime + "\"" +
            "counter:"+counter);

        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }


    //PUT /users/{userId}/projects/{projectId}/sessions/{sessionId}
    //Update session and return the updated object
    //Request and Response(200) Body:
    //{
    //  "id": 0,
    //  "startTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //  "endTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //  "counter": 0
    //}
    private APIResponse updateSession(int userID, int projectID, int sessionID, String startTime, String endTime, int counter) throws Exception {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/"+userID+"/projects/"+projectID+"/sessions/"+sessionID);

        StringEntity input = new StringEntity("{\"id\":\"" + 0+ "\"," +
            "\"startTime\":\"" + startTime + "\"," +
            "\"endTime\":\"" + endTime + "\"" +
            "counter:"+counter);


        input.setContentType("application/json");
        httpRequest.setEntity(input);
        return executeRequest(httpRequest);
    }


    //GET /users/{userId}/projects/{projectId}/report
    //Get a (newly generated) report for a given user and project
    // Response Object
    // {
    //  "sessions": [
    //    {
    //      "startingTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //      "endingTime": "2019-02-18T20:00Z (see http://apiux.com/2013/03/20/5-laws-api-dates-and-times/)",
    //      "hoursWorked": 0
    //    }
    //  ],
    //  "completedPomodoros": 0,
    //  "totalHoursWorkedOnProject": 0
    //}
    private APIResponse getReport(int userID, int projectID, boolean includeCompletedPomodoros, boolean includeTotalHoursWorkedOnProject) throws Exception {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/"+userID+"/projects/"+projectID+"/report?"+
            "includeCompletedPomodoros="+includeCompletedPomodoros+"&includeTotalHoursWorkedOnProject="+includeTotalHoursWorkedOnProject);
        return executeRequest(httpRequest);
    }

    private APIResponse getReport(int userID, int projectID) throws Exception {
        return getReport(userID, projectID, false, false);
    }

}

class APIResponse {

    private Integer id;
    private int status;
    private HttpEntity entity;
    private CloseableHttpResponse response;

    public int getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    @SuppressWarnings("unchecked")
    public APIResponse(CloseableHttpResponse response) throws Exception{
        this.response = response;
        id = getIdFromResponse(response);
        status = response.getStatusLine().getStatusCode();
        if (status == 200) {
            entity = response.getEntity();
        }
        response.close();
    }

    public void checkStatus(int status) throws Exception {
        if (this.status != status) {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
        EntityUtils.consume(response.getEntity());
        response.close();
    }

    public void printResponse() throws Exception {
        String strResponse = EntityUtils.toString(entity);
        System.out.println("*** String response " + strResponse + " (" + this.status + ") ***");
    }

    public void checkStatusAndPrintResponse(int status) throws Exception {
        checkStatus(status);
        printResponse();
    }

    public void checkEqual(String expectedJson) throws Exception {
        String strResponse = EntityUtils.toString(entity);
        JSONAssert.assertEquals(expectedJson,strResponse, false);
        EntityUtils.consume(response.getEntity());
        response.close();
    }

    private Integer getIdFromResponse(CloseableHttpResponse response) throws IOException, JSONException {
        HttpEntity entity = response.getEntity();
        String strResponse = EntityUtils.toString(entity);
        Integer id = getIdFromStringResponse(strResponse);
        return id;
    }

    private Integer getIdFromStringResponse(String strResponse) throws JSONException {
        JSONObject object = new JSONObject(strResponse);
        Integer id = null;
        Iterator<String> keyList = object.keys();
        while (keyList.hasNext()){
            String key = keyList.next();
            if (key.equals("id")) {
                id = Integer.parseInt(object.get(key).toString());
            }
        }
        return id;
    }




}
