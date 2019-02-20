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
    
    @Test
    public void createContactTest() throws Exception {
	    deleteContacts();

        try {
            CloseableHttpResponse response =
		    createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");

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

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void updateContactTest() throws Exception {
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            String id = getIdFromResponse(response);
            response.close();

            response = updateContact(id, "Tom", "Doe", "(123)-456-7890" , "tom@doe.org");

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

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"Tom\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"tom@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            String id = getIdFromResponse(response);
            // EntityUtils.consume(response.getEntity());
            response.close();

            response = getContact(id);

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

            String expectedJson = "{\"id\":\"" + id + "\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
	    JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void getAllContactsTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();
        String id = null;
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += "[{\"id\":\"" + id + "\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
            response.close();

            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            id = getIdFromResponse(response);
            expectedJson += ",{\"id\":\"" + id + "\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(9876)-543-210\",\"email\":\"jane@wall.com\"}]";
            response.close();

            response = getAllContacts();

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
    public void DeleteContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();
        String expectedJson = null;

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteid = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteContact(deleteid);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            expectedJson = "{\"id\":\"" + deleteid + "\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllContacts();
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
    public void CreateMultipleDeleteOneContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();
        String expectedJson = "";

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String deleteId = getIdFromResponse(response);
            response.close();

            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            expectedJson += "[{\"id\":\"" + id + "\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(9876)-543-210\",\"email\":\"jane@wall.com\"}]";
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = deleteContact(deleteId);

            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
            strResponse = EntityUtils.toString(entity);

            System.out.println("*** String response " + strResponse + " (" + response.getStatusLine().getStatusCode() + ") ***");

            String expectedJson2 = "{\"id\":\"" + deleteId + "\",\"firstname\":\"John\",\"familyname\":\"Doe\",\"phonenumber\":\"(123)-456-7890\",\"email\":\"john@doe.org\"}";
            JSONAssert.assertEquals(expectedJson2,strResponse, false);
            EntityUtils.consume(response.getEntity());
            response.close();

            response = getAllContacts();
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
    public void CreateMultipleUpdateOneContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id = getIdFromResponse(response);
            response.close();

            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String updatedId = getIdFromResponse(response);
            response.close();

            int status;
            HttpEntity entity;
            String strResponse;

            response = updateContact(updatedId, "Jane", "Wall", "(6789)-210-534" , "jane@wall.com");
            String expectedJson = "{\"id\":\"" + updatedId + "\",\"firstname\":\"Jane\",\"familyname\":\"Wall\",\"phonenumber\":\"(6789)-210-534\",\"email\":\"jane@wall.com\"}";

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

            response = getContact(updatedId);

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
    public void getMissingContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "xyz" + id1 + id2; // making sure the ID is not present

            response = getContact(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    @Test
    public void deleteMissingContactTest() throws Exception {
        httpclient = HttpClients.createDefault();
        deleteContacts();

        try {
            CloseableHttpResponse response = createContact("John", "Doe", "(123)-456-7890" , "john@doe.org");
            // EntityUtils.consume(response.getEntity());
            String id1 = getIdFromResponse(response);
            response.close();

            response = createContact("Jane", "Wall", "(9876)-543-210" , "jane@wall.com");
            // EntityUtils.consume(response.getEntity());
            String id2 = getIdFromResponse(response);
            response.close();

            String missingId = "xyz" + id1 + id2; // making sure the ID is not present

            response = deleteContact(missingId);

            int status = response.getStatusLine().getStatusCode();
            Assert.assertEquals(404, status);

            EntityUtils.consume(response.getEntity());
            response.close();
        } finally {
            httpclient.close();
        }
    }

    private CloseableHttpResponse createContact(String firstname, String familyname, String phonenumber, String email) throws IOException {
        HttpPost httpRequest = new HttpPost(baseUrl + "/api/contacts");
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"familyname\":\"" + familyname + "\"," +
                "\"phonenumber\":\"" + phonenumber + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse updateContact(String id, String firstname, String familyname, String phonenumber, String email) throws IOException {
        HttpPut httpRequest = new HttpPut(baseUrl + "/api/contacts/" + id);
        httpRequest.addHeader("accept", "application/json");
        StringEntity input = new StringEntity("{\"firstname\":\"" + firstname + "\"," +
                "\"familyname\":\"" + familyname + "\"," +
                "\"phonenumber\":\"" + phonenumber + "\"," +
                "\"email\":\"" + email + "\"}");
        input.setContentType("application/json");
        httpRequest.setEntity(input);

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getContact(String id) throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/api/contacts/" + id);
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse getAllContacts() throws IOException {
        HttpGet httpRequest = new HttpGet(baseUrl + "/api/contacts");
        httpRequest.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpRequest.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpRequest);
        System.out.println("*** Raw response " + response + "***");
        return response;
    }

    private CloseableHttpResponse deleteContact(String id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(baseUrl + "/api/contacts/" + id);
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
        return response;
    }

    private CloseableHttpResponse deleteContacts() throws IOException {
	    HttpDelete httpDelete = new HttpDelete(baseUrl + "/api/contacts");
        httpDelete.addHeader("accept", "application/json");

        System.out.println("*** Executing request " + httpDelete.getRequestLine() + "***");
        CloseableHttpResponse response = httpclient.execute(httpDelete);
        System.out.println("*** Raw response " + response + "***");
        // EntityUtils.consume(response.getEntity());
        // response.close();
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
