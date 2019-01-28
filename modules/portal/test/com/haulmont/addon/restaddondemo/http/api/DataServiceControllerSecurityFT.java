/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */


package com.haulmont.addon.restaddondemo.http.api;

import com.haulmont.cuba.core.sys.encryption.BCryptEncryptionModule;
import com.haulmont.cuba.core.sys.encryption.EncryptionModule;
import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.security.entity.RoleType;
import com.meterware.httpunit.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class DataServiceControllerSecurityFT {

    private static final String URI_BASE = "http://localhost:8080/";
    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;

    private WebConversation conv;
    public static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";
    private Connection conn;

    /**
     * Entitites ids
     */
    private String carUuidString;
    private String colourUuidString;
    private String driverUuidString;

    /**
     * User ids
     */
    private UUID colorReadUserId;
    private UUID colorUpdateUserId;
    private UUID colorCreateUserId;
    private UUID colorDeleteUserId;
    private UUID carReadUserId;

    /**
     * Logins
     */
    private String colorReadUserLogin = "colorReadUser";
    private String colorUpdateUserLogin = "colorUpdateUser";
    private String colorCreateUserLogin = "colorCreateUser";
    private String colorDeleteUserLogin = "colorDeleteUser";
    private String carReadUserLogin = "carReadUser";

    private static EncryptionModule encryption = new BCryptEncryptionModule();

    private String colorReadUserPassword = "colorReadUser";
    private String colorUpdateUserPassword = "colorUpdateUser";
    private String colorCreateUserPassword = "colorCreateUser";
    private String colorDeleteUserPassword = "colorDeleteUser";
    private String carReadUserPassword = "carReadUser";

    /**
     * Roles
     */
    private UUID colorReadRoleId;
    private UUID colorUpdateRoleId;
    private UUID colorCreateRoleId;
    private UUID colorDeleteRoleId;
    private UUID carReadRoleId;
    private UUID noColorReadRoleId;

    /**
     * User sessions
     */
    private String colorReadUserSessionId;
    private String colorUpdateUserSessionId;
    private String colorCreateUserSessionId;
    private String colorDeleteUserSessionId;
    private String carReadUserSessionId;

    private UUID groupUuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");

    private DataSet dirtyData = new DataSet();

    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        prepareDb();

        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);

        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        conv = new WebConversation();

        colorReadUserSessionId = login(colorReadUserLogin, colorReadUserPassword);
        colorUpdateUserSessionId = login(colorUpdateUserLogin, colorUpdateUserPassword);
        colorCreateUserSessionId = login(colorCreateUserLogin, colorCreateUserPassword);
        colorDeleteUserSessionId = login(colorDeleteUserLogin, colorDeleteUserPassword);
        carReadUserSessionId = login(carReadUserLogin, carReadUserPassword);
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

    @After
    public void tearDown() throws SQLException {
        dirtyData.cleanup(conn);

        if (conn != null)
            conn.close();
    }

    @Test
    public void testInvalidSessionId() throws Exception {
        WebResponse response = GET("app-portal/api/find.json?e=ref$Colour-"
                + colourUuidString + "&s=" + UUID.randomUUID(),
                "charset=UTF-8");
        assertEquals(HTTP_UNAUTHORIZED, response.getResponseCode());
    }

    @Test
    public void find_permitted_JSON() throws Exception {
        //trying to get entity with permitted read access
        WebResponse response = GET("app-portal/api/find.json?e=ref$Colour-"
                + colourUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());
        JSONObject color = new JSONObject(response.getText());
        assertEquals("ref$Colour-" + colourUuidString, color.getString("id"));
    }

    @Test
    public void find_permitted_XML() throws Exception {
        //trying to get entity with permitted read access
        WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-"
                + colourUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        Document xml = builder.parse(new InputSource(new StringReader(response.getText())));
        assertEquals(HTTP_OK, response.getResponseCode());
        assertEquals(1, xml.getChildNodes().getLength());
        Node instancesNode = xml.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        assertEquals("instance", instancesNode.getChildNodes().item(1).getNodeName());
        Node instanceNode = instancesNode.getChildNodes().item(1);
        assertEquals("ref$Colour-" + colourUuidString, instanceNode.getAttributes().getNamedItem("id").getNodeValue());
    }

    @Test
    public void find_forbidden_JSON() throws Exception {
        //trying to get entity with forbidden read access
        WebResponse response = GET("app-portal/api/find.json?e=ref_Car-"
                + carUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void find_forbidden_XML() throws Exception {
        //trying to get entity with forbidden read access
        WebResponse response = GET("app-portal/api/find.xml?e=ref_Car-"
                + carUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void find_attributes_JSON() throws Exception {
        WebResponse response = GET("app-portal/api/find.json?e=ref_Car-"
                + carUuidString + "&s=" + carReadUserSessionId,
                "charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());
        JSONObject car = new JSONObject(response.getText());
        assertEquals("ref_Car-" + carUuidString, car.getString("id"));
        assertFalse(car.has("driverAllocations"));
        assertFalse(car.has("colour"));
        assertFalse(car.has("model"));
        assertTrue(car.has("vin"));
        assertTrue(car.has("createTs"));
        assertEquals("VWV000", car.get("vin"));
    }

    @Test
    public void find_attributes_XML() throws Exception {
        WebResponse xmlResponse = GET("app-portal/api/find.xml?e=ref_Car-" +
                carUuidString + "&s=" + carReadUserSessionId, "charset=UTF-8");
        Document xmlCar = builder.parse(new InputSource(new StringReader(xmlResponse.getText())));
        assertEquals(1, xmlCar.getChildNodes().getLength());
        Node instancesNode = xmlCar.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        assertEquals("instance", instancesNode.getChildNodes().item(1).getNodeName());
        Node instanceNode = instancesNode.getChildNodes().item(1);
        assertEquals("ref_Car-" + carUuidString, instanceNode.getAttributes().getNamedItem("id").getNodeValue());
        for (int i = 0; i < instanceNode.getChildNodes().getLength(); i++) {
            Node node = instanceNode.getChildNodes().item(i);
            if (!(node instanceof Element)) continue;
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            String nodeContent = element.getTextContent();

            if ("vin".equals(nodeName))
                assertEquals("VWV000", nodeContent);
            else if ("colour".equals(nodeContent))
                fail("Colour must not be accessible");
            else if ("driverAllocations".equals(nodeContent))
                fail("Driver allocations must not be accessible");
            else if ("model".equals(nodeContent))
                assertNull(element.getNodeValue());
        }
    }

    @Test
    public void find_noPermissions_JSON() throws Exception {
        //trying to get entity without any permissions (should be accessible)
        WebResponse response = GET("app-portal/api/find.json?e=ref$ExtDriver-"
                + driverUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());
        JSONObject driver = new JSONObject(response.getText());
        assertEquals("ref$ExtDriver-" + driverUuidString, driver.getString("id"));
    }

    @Test
    public void find_noPermissions_XML() throws Exception {
        //trying to get entity without any permissions (should be accessible)
        WebResponse response = GET("app-portal/api/find.xml?e=ref$ExtDriver-"
                + driverUuidString + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        Document xml = builder.parse(new InputSource(new StringReader(response.getText())));
        assertEquals(HTTP_OK, response.getResponseCode());
        assertEquals(1, xml.getChildNodes().getLength());
        Node instancesNode = xml.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        assertEquals("instance", instancesNode.getChildNodes().item(1).getNodeName());
        Node instanceNode = instancesNode.getChildNodes().item(1);
        assertEquals("ref$ExtDriver-" + driverUuidString, instanceNode.getAttributes().getNamedItem("id").getNodeValue());
    }

    @Test
    public void query_permitted_JSON() throws Exception {
        String url = "app-portal/api/query.json?e=ref$Colour&q=select c from ref$Colour c " +
                "where c.name = :name&name=Red&" + "s=" + colorReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray colours = new JSONArray(response.getText());
        assertEquals(HTTP_OK, response.getResponseCode());
        assertEquals(1, colours.length());
        assertEquals("ref$Colour-" + colourUuidString, ((JSONObject) colours.get(0)).getString("id"));
    }

    @Test
    public void query_permitted_XML() throws Exception {
        String url = "app-portal/api/query.xml?e=ref$Colour&q=select c from ref$Colour c " +
                "where c.name = :name&name=Red&" + "s=" + colorReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        Document xml = builder.parse(new InputSource(new StringReader(response.getText())));
        assertEquals(HTTP_OK, response.getResponseCode());
        assertEquals(1, xml.getChildNodes().getLength());
        Node instancesNode = xml.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        assertEquals("instance", instancesNode.getChildNodes().item(1).getNodeName());
        Node instanceNode = instancesNode.getChildNodes().item(1);
        assertEquals("ref$Colour-" + colourUuidString, instanceNode.getAttributes().getNamedItem("id").getNodeValue());
    }

    @Test
    public void query_forbidden_JSON() throws Exception {
        String url = "app-portal/api/query.json?e=ref_Car&q=select c from ref_Car c " +
                "where c.vin = :vin&vin=VWV000&" + "s=" + colorReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void query_forbidden_XML() throws Exception {
        String url = "app-portal/api/query.xml?e=ref_Car&q=select c from ref_Car c " +
                "where c.vin = :vin&vin=VWV000&" + "s=" + colorReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void query_attributes_JSON() throws Exception {
        String url = "app-portal/api/query.json?e=ref_Car&q=select c from ref_Car c" +
                "&" + "s=" + carReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());
        JSONArray cars = new JSONArray(response.getText());
        for (int i = 0; i < cars.length(); i++) {
            JSONObject car = (JSONObject) cars.get(i);
            assertFalse(car.has("driverAllocations"));
            assertFalse(car.has("colour"));
            assertFalse(car.has("model"));
            assertTrue(car.has("vin"));
            assertTrue(car.has("createTs"));
        }
    }

    @Test
    public void query_attributes_XML() throws Exception {
        String url = "app-portal/api/query.xml?e=ref_Car&q=select c from ref_Car c" +
                "&" + "s=" + carReadUserSessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());

        Document xmlCar = builder.parse(new InputSource(new StringReader(response.getText())));
        assertEquals(1, xmlCar.getChildNodes().getLength());
        Node instancesNode = xmlCar.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        NodeList content = instancesNode.getChildNodes();
        int childsNumber = content.getLength();
        assertEquals("instance", content.item(1).getNodeName());

        for (int i = 1; i < childsNumber; i++) {
            Node instance = content.item(i);
            for (int j = 0; j < instance.getChildNodes().getLength(); j++) {
                Node node = instance.getChildNodes().item(j);
                if (!(node instanceof Element)) continue;
                String nodeName = node.getAttributes().getNamedItem("name").getNodeValue();
                if ("colour".equals(nodeName))
                    fail("Colour must not be accessible");
                else if ("driverAllocations".equals(nodeName))
                    fail("Driver allocations must not be accessible");
            }
        }
    }

    @Test
    public void commit_updateForbidden_JSON() throws Exception {
        String json = prepareJson(DataServiceControllerSecurityFT.class, "modified_colour.json",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId,
                json, "application/json;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void commit_updateForbidden_XML() throws Exception {
        String xmlRequest = prepareXml(DataServiceControllerFT.class, "modified_colour.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId,
                xmlRequest, "text/xml;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void commit_updatePermitted_JSON() throws Exception {
        String json = prepareJson(DataServiceControllerSecurityFT.class, "modified_colour.json",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId,
                json, "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$Colour-" + colourUuidString, res.getJSONObject(0).getString("id"));
    }

    @Test
    public void commit_updatePermitted_XML() throws Exception {
        String xmlRequest = prepareXml(DataServiceControllerSecurityFT.class, "modified_colour.xml",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId,
                xmlRequest, "text/xml;charset=UTF-8");
        Document xml = builder.parse(new InputSource(new StringReader(response.getText())));
        Node instanceNode = getInstanceNode(xml, 0);
        assertFieldValue(instanceNode, "name", "green");
        assertFieldValue(instanceNode, "description", "green");
        assertFieldValue(instanceNode, "version", "2");
    }

    @Test
    public void commit_createForbidden_JSON() throws Exception {
        UUID newUuid = dirtyData.createColourUuid();
        String json = prepareJson(DataServiceControllerSecurityFT.class, "new_colour.json",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Colour-" + newUuid.toString()));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId,
                json, "application/json;charset=UTF-8");

        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());

        response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId,
                json, "application/json;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }

    @Test
    public void commit_createForbidden_XML() throws Exception {
        UUID newUuid = dirtyData.createColourUuid();
        String xmlRequest = prepareXml(DataServiceControllerSecurityFT.class, "new_colour.xml",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Colour-" + newUuid.toString()));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId,
                xmlRequest, "text/xml;charset=UTF-8");

        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());

        response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId,
                xmlRequest, "text/xml;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }


    @Test
    public void commit_createPermitted_JSON() throws Exception {
        UUID newUuid = dirtyData.createColourUuid();
        String json = prepareJson(DataServiceControllerSecurityFT.class, "new_colour.json",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Colour-" + newUuid.toString()));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorCreateUserSessionId,
                json, "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$Colour-" + newUuid.toString(), res.getJSONObject(0).getString("id"));
    }

    @Test
    public void commit_createPermitted_XML() throws Exception {
        UUID newUuid = dirtyData.createColourUuid();
        String xmlRequest = prepareXml(DataServiceControllerSecurityFT.class, "new_colour.xml",
                MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Colour-" + newUuid.toString()));
        WebResponse response = POST("/app-portal/api/commit?" + "s=" + colorCreateUserSessionId,
                xmlRequest, "text/xml;charset=UTF-8");
        assertEquals(HTTP_OK, response.getResponseCode());

        Document xmlDoc = builder.parse(new InputSource(new StringReader(response.getText())));
        Node instanceNode = getInstanceNode(xmlDoc, 0);
        assertFieldValue(instanceNode, "name", "Red");

        response = GET("app-portal/api/find.xml?e=ref$Colour-" + newUuid + "&s=" + colorReadUserSessionId,
                "charset=UTF-8");
        Document xmlColour = builder.parse(new InputSource(new StringReader(response.getText())));
        assertEquals(1, xmlColour.getChildNodes().getLength());
        Node instancesNode = xmlColour.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        instanceNode = instancesNode.getChildNodes().item(1);
        assertFieldValue(instanceNode, "name", "Red");
    }

    @Test
    public void commit_deletePermitted_JSON() throws Exception {
        WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-" + colourUuidString +
                "&s=" + colorDeleteUserSessionId, "charset=UTF-8");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        String json = prepareJson(DataServiceControllerFT.class, "removed_colour.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString,
                "$TO_BE_REPLACED_ID$", colourUuidString)
        );

        response = POST("/app-portal/api/commit?" + "s=" + colorDeleteUserSessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$Colour-" + colourUuidString, res.getJSONObject(0).get("id"));
        assertEquals("colorDeleteUser", res.getJSONObject(0).get("deletedBy"));
    }

    @Test
    public void commit_deletePermitted_XML() throws Exception {
        WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-"
                + colourUuidString + "&s=" + colorDeleteUserSessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        String xml = prepareXml(DataServiceControllerFT.class, "removed_colour.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString,
                "$TO_BE_REPLACED_ID$", colourUuidString)
        );

        response = POST("/app-portal/api/commit?" + "s=" + colorDeleteUserSessionId, xml,
                "text/xml;charset=UTF-8");
        Document xmlDoc = builder.parse(new InputSource(new StringReader(response.getText())));
        Node instanceNode = getInstanceNode(xmlDoc, 0);
        assertFieldValue(instanceNode, "version", "2");
        assertFieldValue(instanceNode, "deletedBy", "colorDeleteUser");

        response = GET("app-portal/api/find.xml?e=ref$Colour-" + colourUuidString
                + "&s=" + colorDeleteUserSessionId, "charset=UTF-8");
        assertEquals(HTTP_NOT_FOUND, response.getResponseCode());
    }

    @Test
    public void commit_deleteForbidden_JSON() throws Exception {
        WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-" + colourUuidString +
                "&s=" + colorDeleteUserSessionId, "charset=UTF-8");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        String json = prepareJson(DataServiceControllerFT.class, "removed_colour.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString,
                "$TO_BE_REPLACED_ID$", colourUuidString)
        );

        //update permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId, json,
                "application/json;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());

        //create permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorCreateUserSessionId, json,
                "application/json;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());


        //read permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId, json,
                "application/json;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());

    }

    @Test
    public void commit_deleteForbidden_XML() throws Exception {
        WebResponse response = GET("app-portal/api/find.xml?e=ref$Colour-" + colourUuidString +
                "&s=" + colorDeleteUserSessionId, "charset=UTF-8");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        String xml = prepareXml(DataServiceControllerFT.class, "removed_colour.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Colour-" + colourUuidString,
                "$TO_BE_REPLACED_ID$", colourUuidString)
        );

        //update permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorUpdateUserSessionId, xml,
                "text/xml;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());

        //create permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorCreateUserSessionId, xml,
                "text/xml;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());


        //read permission should be not enough to delete
        response = POST("/app-portal/api/commit?" + "s=" + colorReadUserSessionId, xml,
                "text/xml;charset=UTF-8");
        assertEquals(HTTP_FORBIDDEN, response.getResponseCode());
    }
    
    private String prepareXml(Class baseClass, String resourceName, Map<String, String> map)
            throws TransformerException, XMLStreamException, IOException, SAXException, XPathExpressionException {
        String xml = IOUtils.toString(baseClass.getResourceAsStream(resourceName));
        return replaceIds(xml, map);
    }

    private Node getMappingPair(Document xmlMapping, int pairIndex) {
        assertEquals(1, xmlMapping.getChildNodes().getLength());
        Node mappingNode = xmlMapping.getFirstChild();
        assertEquals("mapping", mappingNode.getNodeName());

        Node pairNode = mappingNode.getChildNodes().item(pairIndex);
        assertEquals("pair", pairNode.getNodeName());
        assertEquals(2, pairNode.getChildNodes().getLength());

        return pairNode;
    }

    private void assertFieldValue(Node instanceNode, String fieldName, @Nullable String value) {
        NodeList fields = instanceNode.getChildNodes();
        for (int i = 0; i < fields.getLength(); i++) {
            Node fieldNode = fields.item(i);
            if (!(fieldNode instanceof Element)) continue;
            if (fieldName.equals(getAttributeValue(fieldNode, "name"))) {
                if (value == null) {
                    assertEquals(0, fieldNode.getChildNodes().getLength());
                } else {
                    assertEquals(value, fieldNode.getTextContent());
                }
                return;
            }
        }
        fail("No field found with name: " + fieldName);
    }

    private String replaceIds(String xml, Map<String, String> key2value)
            throws IOException, SAXException, XPathExpressionException, XMLStreamException, TransformerException {
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("//instance");
        NodeList items = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < items.getLength(); i++) {
            Node result = items.item(i);
            String value = getAttributeValue(result, "id");
            for (Map.Entry<String, String> entry : key2value.entrySet()) {
                value = value.replace(entry.getKey(), entry.getValue());
            }
            setAttributeValue(result, value);
        }

        expr = xpath.compile("//ref");
        items = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < items.getLength(); i++) {
            Node result = items.item(i);
            Node idAttribute = result.getAttributes().getNamedItem("id");
            if (idAttribute != null) {
                String value = idAttribute.getTextContent();
                for (Map.Entry<String, String> entry : key2value.entrySet()) {
                    value = value.replace(entry.getKey(), entry.getValue());
                }
                idAttribute.setNodeValue(value);
            } else {
                String value = result.getTextContent();
                for (Map.Entry<String, String> entry : key2value.entrySet()) {
                    value = value.replace(entry.getKey(), entry.getValue());
                }
                result.setTextContent(value);
            }
        }
        return writeXML(doc);
    }

    private void setAttributeValue(Node result, String newValue) {
        result.getAttributes().getNamedItem("id").setNodeValue(newValue);
    }

    private String getAttributeValue(Node node, String attributeName) {
        return node.getAttributes().getNamedItem(attributeName).getNodeValue();
    }

    private String writeXML(Node document) throws TransformerException, IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        Source source = new DOMSource(document);
        transformer.transform(source, result);
        writer.close();
        return writer.toString();
    }

    private WebResponse GET(String uri, String acceptedFormat) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(URI_BASE + uri);
        request.setHeaderField("Accept", acceptedFormat);
        return conv.sendRequest(request);
    }

    private WebResponse POST(String uri, String s, String contentType) throws IOException, SAXException {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        return conv.sendRequest(new PostMethodWebRequest(URI_BASE + uri, is, contentType));
    }

    private void prepareDb() throws Exception {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(DB_URL, "cuba", "cuba");
        createDbData();
        createDbUsers();
        createDbRoles();
        createDbUserRoles();
        createDbPermissions();
    }

    private void createDbPermissions() throws SQLException {
        createEntityOpPermissions();
        createEntityAttrPermissions();
    }

    private void createEntityOpPermissions() throws SQLException {
        Integer PERMIT = 1;
        Integer FORBID = 0;

        UUID canReadColorPrmsId = dirtyData.createPermissionUuid();
        //colourReadRole allows to read colours
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canReadColorPrmsId),
                new PostgresUUID(colorReadRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref$Colour:read",
                PERMIT
        );

        UUID cantReadCarPrmsID = dirtyData.createPermissionUuid();
        //colorReadRole prohibits to read cars
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(cantReadCarPrmsID),
                new PostgresUUID(colorReadRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref_Car:read",
                FORBID
        );

        UUID canUpdateColorPrmsId = dirtyData.createPermissionUuid();
        //colorUpdateRole allows to update colours
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canUpdateColorPrmsId),
                new PostgresUUID(colorUpdateRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref$Colour:update",
                PERMIT
        );

        UUID canCreateColorPrmsId = dirtyData.createPermissionUuid();
        //colorCreateRole allows to create colours
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canCreateColorPrmsId),
                new PostgresUUID(colorCreateRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref$Colour:create",
                PERMIT
        );

        UUID canDeleteColorPrmsId = dirtyData.createPermissionUuid();
        //colorDeleteRole allows to delete colours
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canDeleteColorPrmsId),
                new PostgresUUID(colorDeleteRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref$Colour:delete",
                PERMIT
        );

        UUID cantUpdateCarPrmsId = dirtyData.createPermissionUuid();
        //colorUpdateRole prohibits to update cars
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(cantUpdateCarPrmsId),
                new PostgresUUID(colorUpdateRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref_Car:update",
                FORBID
        );

        UUID cantReadColorPrmsId = dirtyData.createPermissionUuid();
        //noColorReadRole prohibits to view colors
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(cantReadColorPrmsId),
                new PostgresUUID(noColorReadRoleId),
                PermissionType.ENTITY_OP.getId(),
                "ref$Colour:read",
                FORBID
        );
    }

    private void createEntityAttrPermissions() throws SQLException {
        Integer MODIFY = 2;
        Integer VIEW = 1;
        Integer DENY = 0;

        UUID canReadCarModelPrmsId = dirtyData.createPermissionUuid();
        //carReadRole allows to view car's model
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canReadCarModelPrmsId),
                new PostgresUUID(carReadRoleId),
                PermissionType.ENTITY_ATTR.getId(),
                "ref_Car:model",
                VIEW
        );

        UUID canReadCarColorPrmsId = dirtyData.createPermissionUuid();
        //carReadRole allows to view car's model
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canReadCarColorPrmsId),
                new PostgresUUID(carReadRoleId),
                PermissionType.ENTITY_ATTR.getId(),
                "ref_Car:colour",
                VIEW
        );

        UUID canModifyCarVinPrmsId = dirtyData.createPermissionUuid();
        //carReadRole allows to modify car's vin
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(canModifyCarVinPrmsId),
                new PostgresUUID(carReadRoleId),
                PermissionType.ENTITY_ATTR.getId(),
                "ref_Car:vin",
                MODIFY
        );

        UUID denyCarDriverAllocsPrmsId = dirtyData.createPermissionUuid();
        //carReadRole cannot read or modify car's driver allocations
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(denyCarDriverAllocsPrmsId),
                new PostgresUUID(carReadRoleId),
                PermissionType.ENTITY_ATTR.getId(),
                "ref_Car:driverAllocations",
                DENY
        );
    }

    private void createDbUserRoles() throws SQLException {
        UUID id = UUID.randomUUID();
        //colorReadUser has colorReadRole role (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(colorReadUserId),
                new PostgresUUID(colorReadRoleId)
        );

        id = UUID.randomUUID();
        //colorUpdateUser has colorUpdateRole (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(colorUpdateUserId),
                new PostgresUUID(colorUpdateRoleId)
        );

        id = UUID.randomUUID();
        //colorCreateUser has colorCreateRole (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(colorCreateUserId),
                new PostgresUUID(colorCreateRoleId)
        );

        id = UUID.randomUUID();
        //colorDeleteUser has colorDeleteRole (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(colorDeleteUserId),
                new PostgresUUID(colorDeleteRoleId)
        );

        id = UUID.randomUUID();
        //carReadUser has carReadUser (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(carReadUserId),
                new PostgresUUID(carReadRoleId)
        );

        id = UUID.randomUUID();
        //carReadUser has carReadUser (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(carReadUserId),
                new PostgresUUID(noColorReadRoleId)
        );
    }

    private void createDbRoles() throws SQLException {
        //read-only role. can read colours, can't read cars
        colorReadRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(colorReadRoleId),
                RoleType.READONLY.getId(),
                "colorReadRole"
        );

        //read_only role. can update colours, can't update cars
        colorUpdateRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(colorUpdateRoleId),
                RoleType.READONLY.getId(),
                "colorUpdateRole"
        );

        //read-only role. can create colours
        colorCreateRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(colorCreateRoleId),
                RoleType.READONLY.getId(),
                "colorCreateRole"
        );

        //read-only role. can delete colours
        colorDeleteRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(colorDeleteRoleId),
                RoleType.READONLY.getId(),
                "colorDeleteRole"
        );

        //read-only role for attributes access tests
        carReadRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(carReadRoleId),
                RoleType.READONLY.getId(),
                "carReadRole"
        );

        //read-only role, prohibiting viewing the colors
        noColorReadRoleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(noColorReadRoleId),
                RoleType.READONLY.getId(),
                "noColorReadRole"
        );
    }

    private void createDbData() throws SQLException {
        UUID colourUuid = dirtyData.createColourUuid();
        colourUuidString = colourUuid.toString();
        executePrepared("insert into ref_colour(id, version, name) values(?, ?, ?)",
                new PostgresUUID(colourUuid),
                1L,
                "Red"
        );

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                new PostgresUUID(carUuid),
                1l,
                "VWV000",
                new PostgresUUID(colourUuid)
        );

        UUID driverUuid = dirtyData.createDriverUuid();
        driverUuidString = driverUuid.toString();
        executePrepared("insert into ref_driver(id, version, name, DTYPE) values(?, ?, ?, 'ref$ExtDriver')",
                new PostgresUUID(driverUuid),
                1l,
                "Driver"
        );
    }

    private void createDbUsers() throws SQLException {
        //can read colours, cant read cars
        colorReadUserId = dirtyData.createUserUuid();
        String pwd = encryption.getPasswordHash(colorReadUserId, colorReadUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(colorReadUserId),
                1l,
                colorReadUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                "colorreaduser"
        );

        //can update colours
        colorUpdateUserId = dirtyData.createUserUuid();
        pwd = encryption.getPasswordHash(colorUpdateUserId, colorUpdateUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(colorUpdateUserId),
                1l,
                colorUpdateUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                "colorupdateuser"
        );

        //can create colours
        colorCreateUserId = dirtyData.createUserUuid();
        pwd = encryption.getPasswordHash(colorCreateUserId, colorCreateUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(colorCreateUserId),
                1l,
                colorCreateUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                "colorcreateuser"
        );

        //can delete colours
        colorDeleteUserId = dirtyData.createUserUuid();
        pwd = encryption.getPasswordHash(colorDeleteUserId, colorDeleteUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(colorDeleteUserId),
                1l,
                colorDeleteUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                "colordeleteuser"
        );

        //can read cars, used for attributes access testing
        carReadUserId = dirtyData.createUserUuid();
        pwd = encryption.getPasswordHash(carReadUserId, carReadUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(carReadUserId),
                1l,
                carReadUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                "carreaduser"
        );
    }

    private void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    private String prepareJson(Class baseClass, String resourceName, Map<String, String> replacements)
            throws IOException {
        String json = IOUtils.toString(baseClass.getResourceAsStream(resourceName));
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            json = json.replace(entry.getKey(), entry.getValue());
        }
        return json;
    }

    private Node getInstanceNode(Document xmlDoc, int idx) {
        assertEquals(1, xmlDoc.getChildNodes().getLength());
        Node instancesNode = xmlDoc.getFirstChild();
        assertEquals("instances", instancesNode.getNodeName());
        assertEquals("instance", instancesNode.getChildNodes().item(idx + 1).getNodeName());
        return instancesNode.getChildNodes().item(idx + 1);
    }
}