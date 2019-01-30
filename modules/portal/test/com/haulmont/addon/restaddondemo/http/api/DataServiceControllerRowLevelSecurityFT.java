/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.meterware.httpunit.*;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

public class DataServiceControllerRowLevelSecurityFT {
    private static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";
    private static final String URI_BASE = "http://localhost:8080/";

    private static final String userLogin = "admin";
    private static final String userPassword = "admin";

    private String sessionId;
    private Connection conn;
    private String redCarUuidString;
    private String blackCarUuidString;
    private String repair1UuidString;
    private String repair2UuidString;

    private String redColourUuidString;
    private String blackColourUuidString;

    private DataSet dirtyData = new DataSet();
    private WebConversation conv;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);

        conv = new WebConversation();

        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(DB_URL, "cuba", "cuba");

        prepareDb();

        sessionId = login(userLogin, userPassword);
    }

    private void prepareDb() throws SQLException {
        UUID redColourId = dirtyData.createColourUuid();
        redColourUuidString = redColourId.toString();
        executePrepared("insert into ref_colour(id, version, name) values (?, ?, ?)",
                new PostgresUUID(redColourId),
                1L,
                "Red");

        UUID blackColourId = dirtyData.createColourUuid();
        blackColourUuidString = blackColourId.toString();
        executePrepared("insert into ref_colour(id, version, name) values (?, ?, ?)",
                new PostgresUUID(blackColourId),
                1L,
                "Black");

        UUID redCarUuid = dirtyData.createCarUuid();
        redCarUuidString = redCarUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                new PostgresUUID(redCarUuid),
                1L,
                "VWV000",
                new PostgresUUID(redColourId)
        );

        UUID blackCarUuid = dirtyData.createCarUuid();
        blackCarUuidString = blackCarUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                new PostgresUUID(blackCarUuid),
                1L,
                "VWV002",
                new PostgresUUID(blackColourId)
        );

        UUID repair1Uuid = dirtyData.createRepairUuid();
        repair1UuidString = repair1Uuid.toString();
        executePrepared("insert into ref_repair(id, car_id, version, repair_date) values (?, ?, ?, ?)",
                new PostgresUUID(repair1Uuid),
                new PostgresUUID(redCarUuid),
                1L,
                java.sql.Date.valueOf("2012-01-13"));

        UUID repair2Uuid = dirtyData.createRepairUuid();
        repair2UuidString = repair2Uuid.toString();
        executePrepared("insert into ref_repair(id, car_id, version, repair_date) values (?, ?, ?, ?)",
                new PostgresUUID(repair2Uuid),
                new PostgresUUID(redCarUuid),
                1L,
                java.sql.Date.valueOf("2012-02-13"));

        UUID constraintId = dirtyData.createConstraintId();
        String constraintInsertSql = "insert into SEC_CONSTRAINT \n" +
                "(ID, CREATE_TS, CREATED_BY, VERSION, UPDATE_TS, UPDATED_BY, DELETE_TS, DELETED_BY, CHECK_TYPE, " +
                "OPERATION_TYPE, CODE, ENTITY_NAME, JOIN_CLAUSE, WHERE_CLAUSE, GROOVY_SCRIPT, FILTER_XML, GROUP_ID) \n" +
                "values (?, '2015-11-26 10:57:15', 'test', 1, '2015-11-26 10:57:15', null, null, null, 'db_and_memory', " +
                "'read', null, 'ref$Colour', null, '{E}.id <> ''" + redColourUuidString + "''', " +
                "'{E}.id != parse(java.util.UUID.class, ''" + redColourUuidString + "'')', null, '0fa2b1a5-1d68-4d69-9fbd-dff348347f93')";
        executePrepared(constraintInsertSql, new PostgresUUID(constraintId));

        UUID constraint2Id = dirtyData.createConstraintId();
        String constraint2InsertSql = "insert into SEC_CONSTRAINT \n" +
                "(ID, CREATE_TS, CREATED_BY, VERSION, UPDATE_TS, UPDATED_BY, DELETE_TS, DELETED_BY, CHECK_TYPE, " +
                "OPERATION_TYPE, CODE, ENTITY_NAME, JOIN_CLAUSE, WHERE_CLAUSE, GROOVY_SCRIPT, FILTER_XML, GROUP_ID) \n" +
                "values (?, '2015-11-26 10:57:15', 'test', 1, '2015-11-26 10:57:15', null, null, null, 'db_and_memory', " +
                "'read', null, 'ref$Repair', null, '{E}.id <> ''" + repair1UuidString + "''', " +
                "'{E}.id != parse(java.util.UUID.class, ''" + repair1UuidString + "'')', null, '0fa2b1a5-1d68-4d69-9fbd-dff348347f93')";
        executePrepared(constraint2InsertSql, new PostgresUUID(constraint2Id));
    }

    @After
    public void tearDown() throws Exception {
        logout();
        dirtyData.cleanup(conn);

        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void queryJSON() throws Exception {
        String url = "app-portal/api/query.json?e=ref$Colour&q=select c from ref$Colour c&s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray colours = new JSONArray(response.getText());
        assertEquals(1, colours.length());
        JSONObject colourObject = colours.getJSONObject(0);
        assertEquals("Black", colourObject.getString("name"));
        assertNotNull(colourObject.getString("__securityToken"));

        url = "app-portal/api/query.json?e=ref_Car&q=select c from ref_Car c where c.id = '" + redCarUuidString + "'&s=" + sessionId + "&view=carEdit";
        response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject carObject = cars.getJSONObject(0);
        assertNotNull(carObject.getString("__securityToken"));

        assertEquals(JSONObject.NULL, carObject.get("colour"));

        JSONArray filteredAttributes = carObject.getJSONArray("__filteredAttributes");
        assertNotNull(filteredAttributes);
        HashSet<String> attributes = new HashSet<>(Arrays.asList(filteredAttributes.getString(0), filteredAttributes.getString(1)));
        HashSet<String> expectedAttributes = new HashSet<>(Arrays.asList("colour", "repairs"));
        assertEquals(expectedAttributes, attributes);

        JSONArray repairs = carObject.getJSONArray("repairs");
        JSONObject repair = repairs.getJSONObject(0);
        assertEquals(1, repairs.length());
        assertEquals("ref$Repair-" + repair2UuidString, repair.getString("id"));

    }

    @Test
    public void findJSON() throws Exception {
        try {
            WebResponse response = GET("app-portal/api/find.json?e=ref$Colour-" + redColourUuidString + "&s=" + sessionId,
                    "charset=UTF-8");
            fail();
        } catch (HttpNotFoundException e) {
            //expected result - entity should not be found
            assertEquals(404, e.getResponseCode());
        }

        WebResponse response = GET("app-portal/api/find.json?e=ref_Car-" + redCarUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        JSONObject carObject = new JSONObject(response.getText());

        assertNotNull(carObject.getString("__securityToken"));
        assertEquals(JSONObject.NULL, carObject.get("colour"));

        JSONArray filteredAttributes = carObject.getJSONArray("__filteredAttributes");
        assertNotNull(filteredAttributes);
        HashSet<String> attributes = new HashSet<>(Arrays.asList(filteredAttributes.getString(0), filteredAttributes.getString(1)));
        HashSet<String> expectedAttributes = new HashSet<>(Arrays.asList("colour", "repairs"));
        assertEquals(expectedAttributes, attributes);

        JSONArray repairs = carObject.getJSONArray("repairs");
        JSONObject repair = repairs.getJSONObject(0);
        assertEquals(1, repairs.length());
        assertEquals("ref$Repair-" + repair2UuidString, repair.getString("id"));
    }

    @Test
    public void queryXml() throws Exception {
        String url = "app-portal/api/query.xml?e=ref$Colour&q=select c from ref$Colour c&s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List colours = document.selectNodes("/instances/instance");

        assertEquals(1, colours.size());
        Element colourObject = (Element) colours.get(0);
        assertEquals("Black", getFieldValue(colourObject, "name"));
        assertNotNull(colourObject.element("__securityToken"));

        url = "app-portal/api/query.xml?e=ref_Car&q=select c from ref_Car c where c.id = '" + redCarUuidString + "'&s=" + sessionId + "&view=carEdit";
        response = GET(url, "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        List cars = document.selectNodes("/instances/instance");
        assertEquals(1, colours.size());
        Element carObject = (Element) cars.get(0);

        assertNull(getFieldValue(carObject, "colour"));

        assertNotNull(carObject.element("__securityToken"));
        assertNotNull(carObject.element("__filteredAttributes"));
        List<Element> filteredAttributes = carObject.element("__filteredAttributes").elements("a");

        HashSet<String> attributes = new HashSet<>(Arrays.asList(filteredAttributes.get(0).getText(), filteredAttributes.get(1).getText()));
        HashSet<String> expectedAttributes = new HashSet<>(Arrays.asList("colour", "repairs"));
        assertEquals(expectedAttributes, attributes);

        List<Node> repairs = carObject.selectNodes("collection[@name='repairs']/instance");
        assertEquals(1, repairs.size());
        assertEquals("ref$Repair-" + repair2UuidString, ((Element)repairs.get(0)).attributeValue("id"));
    }

    @Test
    public void findXml() throws Exception {
        try {
            WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-" + redColourUuidString + "&s=" + sessionId,
                    "charset=UTF-8");
            fail();
        } catch (HttpNotFoundException e) {
            //expected result - entity should not be found
            assertEquals(404, e.getResponseCode());
        }

        WebResponse response = GET("app-portal/api/find.xml?e=ref_Car-" + redCarUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List cars = document.selectNodes("/instances/instance");
        assertEquals(1, cars.size());
        Element carObject = (Element) cars.get(0);

        assertNull(getFieldValue(carObject, "colour"));

        assertNotNull(carObject.element("__securityToken"));
        assertNotNull(carObject.element("__filteredAttributes"));
        List<Element> filteredAttributes = carObject.element("__filteredAttributes").elements("a");

        HashSet<String> attributes = new HashSet<>(Arrays.asList(filteredAttributes.get(0).getText(), filteredAttributes.get(1).getText()));
        HashSet<String> expectedAttributes = new HashSet<>(Arrays.asList("colour", "repairs"));
        assertEquals(expectedAttributes, attributes);

        List<Node> repairs = carObject.selectNodes("collection[@name='repairs']/instance");
        assertEquals(1, repairs.size());
        assertEquals("ref$Repair-" + repair2UuidString, ((Element)repairs.get(0)).attributeValue("id"));
    }

    private String login(String login, String password) throws JSONException, IOException, SAXException {
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("username", login);
        loginJSON.put("password", password);
        loginJSON.put("locale", "ru");
        WebResponse response = POST("/app-portal/api/login",
                loginJSON.toString(), "application/json;charset=UTF-8");
        return response.getText();
    }

    private void logout() throws JSONException {
        if (sessionId == null)
            return;
        try {
            GET("app-portal/api/logout?session=" + sessionId, "charset=UTF-8");
        } catch (Exception e) {
            System.out.println("Error on logout: " + e);
        }
    }

    private void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    private WebResponse POST(String uri, String s, String contentType) throws IOException, SAXException {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        return conv.sendRequest(new PostMethodWebRequest(URI_BASE + uri, is, contentType));
    }

    private WebResponse GET(String uri, String acceptedFormat) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(URI_BASE + uri);
        request.setHeaderField("Accept", acceptedFormat);
        return conv.sendRequest(request);
    }

    protected String prepareFile(String fileName, Map<String, String> replacements) throws IOException {
        InputStream is = getClass().getResourceAsStream(fileName);
        String fileContent = IOUtils.toString(is);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            fileContent = fileContent.replace(entry.getKey(), entry.getValue());
        }
        return fileContent;
    }

    private String getFieldValue(Element instanceEl, String fieldName) {
        Element fieldEl = (Element) instanceEl.selectSingleNode("field[@name='" + fieldName + "']");
        return fieldEl != null ? fieldEl.getText() : null;
    }
}
