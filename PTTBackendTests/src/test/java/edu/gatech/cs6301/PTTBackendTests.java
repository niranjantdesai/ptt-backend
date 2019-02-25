package edu.gatech.cs6301;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;

public class PTTBackendTests {

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

    //done by Weihua
    @Test
    public void getAllUsersTest200() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String id = null;
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":\"" + id
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createUser("Jane", "Wall", "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += ",{\"id\":\"" + id
                    + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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

    // done by Haamid
    @Test
    public void createUserTest201() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        String firstname = "John";
        String lastname = "Doe";
        String email = "john@doe.org";

        try {
            CloseableHttpResponse response = createUser(firstname, lastname, email);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 201 since a valid user object was asked to be created");
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":\"" + id + "\"," + 
                "\"firstname\":\"" + firstname + "\"," + 
                "\"lastname\":\"" + lastname + "\"," + 
                "\"email\":\"" + email + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void createUserTest400() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        String firstname = "John";
        String email = "john@doe.org";

        try {
            CloseableHttpResponse response = createUserIncorrect(firstname, email);

            int status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                System.out.println(
                        "*** Correct! Expected response status: 400 since lastname was not sent to the server.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since lastname was not sent to the server.");
            }

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void createUserTest409() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        String firstname = "John";
        String lastname = "Doe";
        String email = "john@doe.org";

        try {
            CloseableHttpResponse response = createUser(firstname, lastname, email);

            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            if (status == 201) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 201 since a valid user object was asked to be created");
            }
            String strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":\"" + id + "\"," + 
                "\"firstname\":\"" + firstname + "\"," + 
                "\"lastname\":\"" + lastname + "\"," + 
                "\"email\":\"" + email + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());

            // creating another user with the same email but different first and last name
            firstname = "AB";
            lastname = "CD";
            
            response = createUser(firstname, lastname, email);
            status = response.getStatusLine().getStatusCode();
            if (status == 409) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 409 since a user with existing email was asked to be created");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void getUserTest200() throws Exception {
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
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since a user for a valid id: " + id + " was asked to be returned");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + id
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void getUserTest400() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            // Corrupting the ID by making it twice long
            id = id + id;

            response = getUser(id);

            int status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                System.out.println(
                    "*** Correct! Expected response status: 400 since a corrupted user ID: " + id + " was sent to the server.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since a get request was made with a corrupted(twice as long) id: " + id);
            }

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void getUserTest404() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            // Corrupting the ID to an ID that is hopefully not in the database
            char[] idChars = id.toCharArray();
            idChars[id.length()-1] = '1';
            idChars[id.length()-2] = '2';
            id = String.valueOf(idChars);

            response = getUser(id);
            int status = response.getStatusLine().getStatusCode();

            if (status == 404) {
                System.out.println(
                    "*** Correct! Expected response status: 404 since a valid but unused user ID was sent to the server.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since a get request was made with an unsed (hopefully) user id: " + id);
            }

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void updateUserTest200() throws Exception {
        httpclient = HttpClients.createDefault();
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
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since firstname and email were asked to be updated");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + id
                    + "\",\"firstname\":\"Tom\",\"lastname\":\"Doe\",\"email\":\"tom@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void updateUserTest400() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            response = updateUserIncorrect(id, "Tom", "Doe", "tom@doe.org");

            int status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                System.out.println(
                    "*** Correct! Expected response status: 400 since an invalid attribute (noname) was asked to be updated.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since a \"noname\" atribute was asked to be updated which does not exist in the user model");
            }

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void updateUserTest404() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            char[] idChars = id.toCharArray();
            idChars[id.length()-1] = '1';
            idChars[id.length()-2] = '2';
            id = String.valueOf(idChars);

            response = updateUser(id, "Tom", "Doe", "tom@doe.org");

            int status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                System.out.println(
                    "*** Correct! Expected response status: 404 since a valid but hopefully unused user id was asked to be updated.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since a valid but hopefully unused user id was asked to be updated.");
            }

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void deleteUserTest200_1() throws Exception {
        // CreateOneDeleteOneUserTest
        httpclient = HttpClients.createDefault();
        deleteUsers();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(deleteId);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since an existing user with id: " + deleteId + " was asked to be deleted");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":\"" + deleteId
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllUsers();
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since getAllUsers should return all users with code 200 even if no user exists");
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

    // done by Haamid
    @Test
    public void deleteUserTest200_2() throws Exception {
        // CreateMultipleDeleteOneUserTest
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
            expectedJson += "[{\"id\":\"" + id
                    + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}]";
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteUser(deleteId);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since an existing user with id: " + deleteId + " was asked to be deleted");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson2 = "{\"id\":\"" + deleteId
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson2, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllUsers();
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since getAllUsers should return all users with code 200 even if no user exists");
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

    // done by Haamid
    @Test
    public void deleteUserTest400() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            response = deleteUser(id + id);

            int status = response.getStatusLine().getStatusCode();
            if (status == 400) {
                System.out.println(
                    "*** Correct! Expected response status: 400 since an invalid user id was asked to be deleted.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since a user with an invalid id: " + id + id + " was asked to be deleted");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Haamid
    @Test
    public void deleteUserTest404() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();

        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            // Corrupting the ID to an ID that is hopefully not in the database
            char[] idChars = id.toCharArray();
            idChars[id.length()-1] = '1';
            idChars[id.length()-2] = '2';
            id = String.valueOf(idChars);

            response = deleteUser(id);

            int status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                System.out.println(
                    "*** Correct! Expected response status: 404 since a valid but hopefully unused user id was asked to be deleted.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since a user with a valid but hopefully unused id: " + id + " was asked to be deleted");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void createProjectTest201() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 201){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 201");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6301\",\"userId\":\"" + userId + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void createProjectTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProjectIncorrect(userId);
            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since projectname was not sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getAllProjectsTest200() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId1 = getIdFromResponse(response);
            response.close();

            response = createProject("CSE6242",userId);
            String projectId2 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6560",userId);
            String projectId3 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6610",userId);
            String projectId4 = getIdFromResponse(response);
            response.close();

            response = getAllProjects(userId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "[" + "{\"id\":\"" + projectId1
                    + "\",\"projectname\":\"CS6301\",\"userId\":\"" + userId + "\"}" + "," +
                    "{\"id\":\"" + projectId2
                    + "\",\"projectname\":\"CSE6242\",\"userId\":\"" + userId + "\"}" + "," +
                    "{\"id\":\"" + projectId3
                    + "\",\"projectname\":\"ECE6560\",\"userId\":\"" + userId + "\"}" + "," +
                    "{\"id\":\"" + projectId4
                    + "\",\"projectname\":\"ECE6610\",\"userId\":\"" + userId + "\"}" + "]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getAllProjectTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId1 = getIdFromResponse(response);
            response.close();

            response = createProject("CSE6242",userId);
            String projectId2 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6560",userId);
            String projectId3 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6610",userId);
            String projectId4 = getIdFromResponse(response);
            response.close();

            //constructing an invalid Id
            long badId = 2*Integer.MAX_VALUE;
            userId = String.valueOf(badId);

            response = getAllProjects(userId);
            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since a corrupted user ID: " + userId + " was sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getAllProjectTest404() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId1 = getIdFromResponse(response);
            response.close();

            response = createProject("CSE6242",userId);
            String projectId2 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6560",userId);
            String projectId3 = getIdFromResponse(response);
            response.close();

            response = createProject("ECE6610",userId);
            String projectId4 = getIdFromResponse(response);
            response.close();

            int temp = Integer.parseInt(userId);
            userId = String.valueOf(userId+1);

            response = getAllProjects(userId);
            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since a not existed user ID: " + userId + " was sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getProjectTest200() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = getProject(userId,projectId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6301\",\"userId\":\"" + userId + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getProjectTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            long badId = 2*Integer.MAX_VALUE;
            String projectId = String.valueOf(badId);
            response.close();

            response = getProject(userId,projectId);
            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since a corrupted project ID: " + projectId + " was sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getProjectTest404_1() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            //corrupt the projectId
            int temp = Integer.parseInt(projectId);
            projectId = String.valueOf(temp - 1);

            response = getProject(userId,projectId);
            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since a not existed project ID: " + projectId + " was sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Billy
    @Test
    public void getProjectTest404_2() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try {
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            //corrupt the userId
            int temp = Integer.parseInt(userId);
            userId = String.valueOf(temp - 1);

            response = getProject(userId, projectId);
            int status = response.getStatusLine().getStatusCode();
            if (status == 404) {
                System.out.println(
                        "*** Correct! Expected response status: 404 since a not existed user ID: " + userId + " was sent to the server.***");
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void updateProjectTest200() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = updateProject(projectId, "CS6302", userId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6302\",\"userId\":\"" + userId + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void updateProjectTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = updateProjectIncorrect(projectId, "CS6302", userId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if (status == 400) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void updateProjectTest404() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            char[] idChars = projectId.toCharArray();
            idChars[projectId.length()-1] = '1';
            idChars[projectId.length()-2] = '2';
            projectId = String.valueOf(idChars);

            response = updateProjectIncorrect(projectId, "CS6302", userId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 404){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void deleteProjectTest200_1() throws Exception{
        // CreateOneDeleteOneProjectTest
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = deleteProject(userId,projectId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6301\",\"userId\":\"" + userId + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllProjects(userId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since getAllProjects should return all projects with code 200 even if no project exists");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void deleteProjectTest200_2() throws Exception{
        // CreateMultipleDeleteOneProjectTest
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6302", userId);
            String deleteId = getIdFromResponse(response);
            response.close();


            response = deleteProject(userId, deleteId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6302\",\"userId\":\"" + deleteId + "\"}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllProjects(userId);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200 since getAllProjects should return all projects with code 200 even if no project exists");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "[{\"id\":\"" + projectId
                    + "\",\"projectname\":\"CS6301\",\"userId\":\"" + projectId + "\"}]";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void deleteProjectTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            response = deleteProject(userId, projectId + projectId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 400){
                System.out.println(
                    "*** Correct! Expected response status: 400 since an invalid project id was asked to be deleted.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since a project with an invalid projectId: " + projectId + projectId + " was asked to be deleted");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    //done by Weihua
    @Test
    public void deleteProjectTest404() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            char[] idChars = projectId.toCharArray();
            idChars[projectId.length()-1] = '1';
            idChars[projectId.length()-2] = '2';
            projectId = String.valueOf(idChars);

            response = deleteProject(userId, projectId);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 404){
                System.out.println(
                    "*** Correct! Expected response status: 404 since a valid but hopefully unused project id was asked to be deleted.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since a project with a valid but hopefully unused projectId: " + projectId + " was asked to be deleted");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }




    }

    // done by Niranjan
    @Test
    public void createSessionTest201() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 201){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 201");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":\"" + id + "\"," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":0}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void createSessionTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            String endTime = "2019-02-18T20:00Z";
            response = createSessionIncorrect(userId, projectId, endTime);
            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since start time was not sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since start time was not sent to the server");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void createSessionTest404_1() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Corrupt the userId
            int temp = Integer.parseInt(userId);
            userId = String.valueOf(temp - 1);
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);

            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the user ID: " + userId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the user ID " + userId + "does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void createSessionTest404_2() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Corrupt the project ID
            int temp = Integer.parseInt(projectId);
            projectId = String.valueOf(temp - 1);
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);

            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the project ID: " + projectId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the project ID " + projectId + "does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void updateSessionTest201() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();

            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            String sessionId = getIdFromResponse(response);
            response.close();
            
            String newStartTime = "2019-03-18T20:00Z";
            String newEndTime = newStartTime;
            String counter = "1";
            response = updateSession(userId, projectId, sessionId, newStartTime, newEndTime, counter);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 201){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 201");
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String id = getIdFromStringResponse(strResponse);

            String expectedJson = "{\"id\":\"" + id + "\"," +
                "\"startTime\":\"" + newStartTime + "\"," +
                "\"endTime\":\"" + newEndTime + "\"," +
                "\"counter\":" + counter + "}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void updateSessionTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            String sessionId = getIdFromResponse(response);
            response.close();

            // Update the session
            String newStartTime = "2019-03-18T20:00Z";
            String newEndTime = newStartTime;
            response = updateSessionIncorrect(userId, projectId, sessionId, newStartTime, newEndTime);

            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since updated counter was not sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since updated counter was not sent to the server");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        }finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void updateSessionTest404_1() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            String sessionId = getIdFromResponse(response);
            response.close();

            // Corrupt the userId
            int temp = Integer.parseInt(userId);
            userId = String.valueOf(temp - 1);

            // Update the session
            String newStartTime = "2019-03-18T20:00Z";
            String newEndTime = newStartTime;
            String counter = "1";
            response = updateSession(userId, projectId, sessionId, newStartTime, newEndTime, counter);

            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the user ID: " + userId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the user ID " + userId + "does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void updateSessionTest404_2() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            String sessionId = getIdFromResponse(response);
            response.close();

            // Corrupt the projectId
            int temp = Integer.parseInt(projectId);
            projectId = String.valueOf(temp - 1);

            // Update the session
            String newStartTime = "2019-03-18T20:00Z";
            String newEndTime = newStartTime;
            String counter = "1";
            response = updateSession(userId, projectId, sessionId, newStartTime, newEndTime, counter);

            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the project ID: " + projectId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the project ID " + projectId + "does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void updateSessionTest404_3() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301", userId);
            String projectId = getIdFromResponse(response);
            response.close();
            
            // Create a session
            String startTime = "2019-02-18T20:00Z";
            String endTime = startTime;
            response = createSession(userId, projectId, startTime, endTime);
            String sessionId = getIdFromResponse(response);
            response.close();

            // Corrupt the sessionId
            int temp = Integer.parseInt(sessionId);
            sessionId = String.valueOf(temp - 1);

            // Update the session
            String newStartTime = "2019-03-18T20:00Z";
            String newEndTime = newStartTime;
            String counter = "1";
            response = updateSession(userId, projectId, sessionId, newStartTime, newEndTime, counter);

            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the session ID: " + sessionId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the session ID " + sessionId + "does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void getReportTest200() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Create session 1 (completely within the timeframe)
            String startTime1 = "2019-02-18T20:00Z";
            String endTime1 = startTime1;
            response = createSession(userId, projectId, startTime1, endTime1);
            String sessionId1 = getIdFromResponse(response);
            response.close();

            // Update session 1
            String newStartTime1 = startTime1;
            String newEndTime1 = "2019-02-18T20:30Z";
            String counter1 = "1";
            response = updateSession(userId, projectId, sessionId1, newStartTime1, newEndTime1, counter1);

            // Create session 2 (partially within the timeframe)
            String startTime2 = "2019-02-18T21:00Z";
            String endTime2 = startTime2;
            response = createSession(userId, projectId, startTime2, endTime2);
            String sessionId2 = getIdFromResponse(response);
            response.close();

            // Update session 2
            String newStartTime2 = startTime2;
            String newEndTime2 = "2019-02-18T22:00Z";
            String counter2 = "2";
            response = updateSession(userId, projectId, sessionId2, newStartTime2, newEndTime2, counter2);

            // Create session 3 (outside the timeframe)
            String startTime3 = "2019-02-18T22:30Z";
            String endTime3 = startTime3;
            response = createSession(userId, projectId, startTime3, endTime3);
            String sessionId3 = getIdFromResponse(response);
            response.close();

            // Update session 3
            String newStartTime3 = startTime3;
            String newEndTime3 = "2019-02-18T23:00Z";
            String counter3 = "1";
            response = updateSession(userId, projectId, sessionId3, newStartTime3, newEndTime3, counter3);

            String reportStartTime = "2019-02-18T19:30Z";
            String reportEndTime = "2019-02-18T21:30Z";
            response = getReport(userId, projectId, reportStartTime, reportEndTime, true, true);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity;
            String strResponse;
            if(status == 200){
                entity = response.getEntity();
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 200");
            }
            strResponse = EntityUtils.toString(entity);
            System.out.println(
                    "*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson = "{\"sessions\":[{\"startingTime\":\"" + newStartTime1 + "\",\"endingTime\":\"" + newEndTime1 + "\",\"hoursWorked\":0.5}," +
                    "{\"startingTime\":\"" + newStartTime2 + "\",\"endingTime\":\"" + newEndTime2 + "\",\"hoursWorked\":1}]," +
                    "\"completedPomodoros\":3," +
                    "\"totalHoursWorkedOnProject\":1.5}";
            JSONAssert.assertEquals(expectedJson, strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void getReportTest400() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Create session 1 (completely within the timeframe)
            String startTime1 = "2019-02-18T20:00Z";
            String endTime1 = startTime1;
            response = createSession(userId, projectId, startTime1, endTime1);
            String sessionId1 = getIdFromResponse(response);
            response.close();

            // Update session 1
            String newStartTime1 = startTime1;
            String newEndTime1 = "2019-02-18T20:30Z";
            String counter1 = "1";
            response = updateSession(userId, projectId, sessionId1, newStartTime1, newEndTime1, counter1);

            // Create session 2 (partially within the timeframe)
            String startTime2 = "2019-02-18T21:00Z";
            String endTime2 = startTime2;
            response = createSession(userId, projectId, startTime2, endTime2);
            String sessionId2 = getIdFromResponse(response);
            response.close();

            // Update session 2
            String newStartTime2 = startTime2;
            String newEndTime2 = "2019-02-18T22:00Z";
            String counter2 = "2";
            response = updateSession(userId, projectId, sessionId2, newStartTime2, newEndTime2, counter2);

            // Create session 3 (outside the timeframe)
            String startTime3 = "2019-02-18T22:30Z";
            String endTime3 = startTime3;
            response = createSession(userId, projectId, startTime3, endTime3);
            String sessionId3 = getIdFromResponse(response);
            response.close();

            // Update session 3
            String newStartTime3 = startTime3;
            String newEndTime3 = "2019-02-18T23:00Z";
            String counter3 = "1";
            response = updateSession(userId, projectId, sessionId3, newStartTime3, newEndTime3, counter3);

            // Send bad request
            response = getReportIncorrect(userId, projectId, true, false);
            int status = response.getStatusLine().getStatusCode();
            if(status == 400){
                System.out.println(
                        "*** Correct! Expected response status: 400 since the starting and ending times for the report were not sent to the server.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 400 since the starting and ending times for the report were not sent to the server.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void getReportTest404_1() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Create session 1 (completely within the timeframe)
            String startTime1 = "2019-02-18T20:00Z";
            String endTime1 = startTime1;
            response = createSession(userId, projectId, startTime1, endTime1);
            String sessionId1 = getIdFromResponse(response);
            response.close();

            // Update session 1
            String newStartTime1 = startTime1;
            String newEndTime1 = "2019-02-18T20:30Z";
            String counter1 = "1";
            response = updateSession(userId, projectId, sessionId1, newStartTime1, newEndTime1, counter1);

            // Create session 2 (partially within the timeframe)
            String startTime2 = "2019-02-18T21:00Z";
            String endTime2 = startTime2;
            response = createSession(userId, projectId, startTime2, endTime2);
            String sessionId2 = getIdFromResponse(response);
            response.close();

            // Update session 2
            String newStartTime2 = startTime2;
            String newEndTime2 = "2019-02-18T22:00Z";
            String counter2 = "2";
            response = updateSession(userId, projectId, sessionId2, newStartTime2, newEndTime2, counter2);

            // Create session 3 (outside the timeframe)
            String startTime3 = "2019-02-18T22:30Z";
            String endTime3 = startTime3;
            response = createSession(userId, projectId, startTime3, endTime3);
            String sessionId3 = getIdFromResponse(response);
            response.close();

            // Update session 3
            String newStartTime3 = startTime3;
            String newEndTime3 = "2019-02-18T23:00Z";
            String counter3 = "1";
            response = updateSession(userId, projectId, sessionId3, newStartTime3, newEndTime3, counter3);

            // Corrupt the userID
            int temp = Integer.parseInt(userId);
            userId = String.valueOf(temp - 1);

            // Send request
            String reportStartTime = "2019-02-18T19:30Z";
            String reportEndTime = "2019-02-18T21:30Z";
            response = getReport(userId, projectId, reportStartTime, reportEndTime, true, true);
            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the userId " + userId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the userId " + userId + " does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // done by Niranjan
    @Test
    public void getReportTest404_2() throws Exception{
        httpclient = HttpClients.createDefault();
        deleteUsers();
        try{
            // Create a user
            CloseableHttpResponse response = createUser("John", "Doe", "john@doe.org");
            String userId = getIdFromResponse(response);
            response.close();
            
            // Create a project
            response = createProject("CS6301",userId);
            String projectId = getIdFromResponse(response);
            response.close();

            // Create session 1 (completely within the timeframe)
            String startTime1 = "2019-02-18T20:00Z";
            String endTime1 = startTime1;
            response = createSession(userId, projectId, startTime1, endTime1);
            String sessionId1 = getIdFromResponse(response);
            response.close();

            // Update session 1
            String newStartTime1 = startTime1;
            String newEndTime1 = "2019-02-18T20:30Z";
            String counter1 = "1";
            response = updateSession(userId, projectId, sessionId1, newStartTime1, newEndTime1, counter1);

            // Create session 2 (partially within the timeframe)
            String startTime2 = "2019-02-18T21:00Z";
            String endTime2 = startTime2;
            response = createSession(userId, projectId, startTime2, endTime2);
            String sessionId2 = getIdFromResponse(response);
            response.close();

            // Update session 2
            String newStartTime2 = startTime2;
            String newEndTime2 = "2019-02-18T22:00Z";
            String counter2 = "2";
            response = updateSession(userId, projectId, sessionId2, newStartTime2, newEndTime2, counter2);

            // Create session 3 (outside the timeframe)
            String startTime3 = "2019-02-18T22:30Z";
            String endTime3 = startTime3;
            response = createSession(userId, projectId, startTime3, endTime3);
            String sessionId3 = getIdFromResponse(response);
            response.close();

            // Update session 3
            String newStartTime3 = startTime3;
            String newEndTime3 = "2019-02-18T23:00Z";
            String counter3 = "1";
            response = updateSession(userId, projectId, sessionId3, newStartTime3, newEndTime3, counter3);

            // Corrupt the projectId
            int temp = Integer.parseInt(projectId);
            projectId = String.valueOf(temp - 1);

            // Send request
            String reportStartTime = "2019-02-18T19:30Z";
            String reportEndTime = "2019-02-18T21:30Z";
            response = getReport(userId, projectId, reportStartTime, reportEndTime, true, true);
            int status = response.getStatusLine().getStatusCode();
            if(status == 404){
                System.out.println(
                        "*** Correct! Expected response status: 404 since the projectId " + projectId + " does not exist.***");
            }else{
                throw new ClientProtocolException("Unexpected response status: " + status + ", expecting 404 since the projectId " + projectId + " does not exist.***");
            }
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally{
            httpclient.close();
        }
    }

    // sample, TODO: comment out before submitting
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

            expectedJson = "{\"id\":\"" + deleteid
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
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

    // sample, TODO: comment out before submitting
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
            expectedJson += "[{\"id\":\"" + id
                    + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}]";
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

            String expectedJson2 = "{\"id\":\"" + deleteId
                    + "\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"john@doe.org\"}";
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

    // sample, TODO: comment out before submitting
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

    // sample, TODO: comment out before submitting
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
            String expectedJson = "{\"id\":\"" + updatedId
                    + "\",\"firstname\":\"Jane\",\"lastname\":\"Wall\",\"email\":\"jane@wall.com\"}";

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

    // sample, TODO: comment out before submitting
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

    
    private CloseableHttpResponse createUser(String firstname, String lastname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
            "{\"firstname\":\"" + firstname + "\"," + 
            "\"lastname\":\"" + lastname + "\"," + 
            "\"email\":\"" + email + "\"}"
            );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse createUserIncorrect(String firstname, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
            "{\"firstname\":\"" + firstname + "\"," + 
            "\"email\":\"" + email + "\"}"
            );
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
        StringEntity input = new StringEntity(
            "{\"firstname\":\"" + firstname + "\"," + 
            "\"lastname\":\"" + lastname + "\"," + 
            "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateUserIncorrect(String id, String firstname, String lastname, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
            "{\"firstname\":\"" + firstname + "\"," + 
            "\"noname\":\"" + lastname + "\"," + 
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
    
    private CloseableHttpResponse getAllUsers() throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/users");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //Done by Weihua: first make a get call to get all users and then invoke the delete for each of those users
    private void deleteUsers() throws Exception {

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
        response.close();

        List<String> userIds = new ArrayList<>();
        Pattern p = Pattern.compile("\"id\":.*?,");
        Matcher m = p.matcher(strResponse);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            userIds.add(strResponse.substring(start + 6, end - 2));
        }
        for (String id : userIds) {
            response = deleteUser(id);
            response.close();
        }
        // HttpDelete httpDelete = new HttpDelete(baseUrl + "/users");
        // httpDelete.addHeader("accept", "application/json");

        // System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        // CloseableHttpResponse response = httpclient.execute(httpDelete);
        // System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        //return response;
    }

    // done by Billy
    private CloseableHttpResponse createProject(String projectName, String userId) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"projectname\":\"" + projectName + "\"," +
                "\"userId\":\"" + userId + "\"}"
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Billy
    private CloseableHttpResponse createProjectIncorrect(String userId) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"userId\":\"" + userId + "\"}"
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Billy
    private CloseableHttpResponse getProject(String userId, String projectId) throws IOException{
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Billy
    private CloseableHttpResponse getAllProjects(String userId) throws IOException{
        HttpGet httpRequest = new HttpGet(baseUrl + "/users/" + userId + "/projects");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;

    }

    //done by Weihua
    private CloseableHttpResponse updateProject(String projectId, String projectName, String userId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
            "{\"projectId\":\"" + projectId + "\"," + 
            "\"projectName\":\"" + projectName + "\"," + 
            "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //done by Weihua
    private CloseableHttpResponse updateProjectIncorrect(String projectId, String projectName, String userId) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
            "{\"projectId\":\"" + projectId + "\"," + 
            "\"noName\":\"" + projectName + "\"," + 
            "\"userId\":\"" + userId + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    //done by Weihua
    private CloseableHttpResponse deleteProject(String projectId, String userId) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/users/" + userId + "/projects/" + projectId);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse createSession(String userId, String projectId, String startTime, String endTime) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":0}" 
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse createSessionIncorrect(String userId, String projectId, String endTime) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"endTime\":\"" + endTime + "\"," +
                "\"counter\":0}" 
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse updateSession(String userId, String projectId, String sessionId, String startTime, String endTime, String counter) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"counter\":" + counter + "}" 
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse updateSessionIncorrect(String userId, String projectId, String sessionId, String startTime, String endTime) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/sessions/" + sessionId);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity(
                "{\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"}"
        );
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse getReport(String userId, String projectId, String startTime, String endTime, Boolean inclCompPomodoros, Boolean inclTotalHrs) throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report?from=" + startTime + "&to=" + endTime + 
        "&includeCompletedPomodoros=" + Boolean.toString(inclCompPomodoros) + "&includeTotalHoursWorkedOnProject=" + Boolean.toString(inclTotalHrs));
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    // done by Niranjan
    private CloseableHttpResponse getReportIncorrect(String userId, String projectId, Boolean inclCompPomodoros, Boolean inclTotalHrs)  throws IOException{
        HttpPost httpRequest = new HttpPost(baseUrl + "/users/" + userId + "/projects/" + projectId + "/report?" + 
        "includeCompletedPomodoros=" + Boolean.toString(inclCompPomodoros) + "&includeTotalHoursWorkedOnProject=" + Boolean.toString(inclTotalHrs));
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
        while (keyList.hasNext()) {
            String key = keyList.next();
            if (key.equals("id")) {
                id = object.get(key).toString();
            }
        }
        return id;
    }

}
