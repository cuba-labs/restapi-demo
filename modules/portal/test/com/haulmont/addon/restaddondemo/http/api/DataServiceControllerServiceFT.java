/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.haulmont.cuba.restapi.XMLConverter;
import com.haulmont.addon.restaddondemo.core.app.PortalTestService;
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

import static com.haulmont.addon.restaddondemo.http.api.RestXmlUtils.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataServiceControllerServiceFT {
    private static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";
    private static final String URI_BASE = "http://localhost:8080/";

    private static final String userLogin = "admin";
    private static final String userPassword = "admin";

    private String sessionId;
    private Connection conn;
    private String carUuidString;
    private String secondCarUuidString;
    private String colourUuidString;
    private String repairUuidString;

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
        sessionId = login(userLogin, userPassword);

        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(DB_URL, "cuba", "cuba");

        prepareDb();
    }

    private void prepareDb() throws SQLException {
        UUID colourId = dirtyData.createColourUuid();
        colourUuidString = colourId.toString();
        executePrepared("insert into ref_colour(id, name) values (?, ?)",
                new PostgresUUID(colourId),
                "Red");

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                new PostgresUUID(carUuid),
                1L,
                "VWV000",
                new PostgresUUID(colourId)
        );

        UUID secondCarUuid = dirtyData.createCarUuid();
        secondCarUuidString = secondCarUuid.toString();
        executePrepared("insert into ref_car(id, version, vin, colour_id) values(?, ?, ?, ?)",
                new PostgresUUID(secondCarUuid),
                1L,
                "VWV002",
                new PostgresUUID(colourId)
        );

        UUID repairId = dirtyData.createRepairUuid();
        repairUuidString = repairId.toString();
        executePrepared("insert into ref_repair(id, car_id, repair_date) values (?, ?, ?)",
                new PostgresUUID(repairId),
                new PostgresUUID(carUuid),
                java.sql.Date.valueOf("2012-01-13"));


        dirtyData.createRepairUuid();
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
    public void testNoParamsJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "emptyMethod");
        JSONObject result = new JSONObject(response.getText());
        assertEquals(JSONObject.NULL, result.get("result"));
    }

    @Test
    public void testNoParamsXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodGet("xml", "emptyMethod");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("true", rootElement.attributeValue("null"));
    }

    @Test
    public void testExplicitParamTypesJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodWithTypes("json", "sum", new String[]{"2", "3"}, new String[]{"int", "java.lang.String"});
        JSONObject result = new JSONObject(response.getText());
        assertEquals(5, result.getInt("result"));
    }

    @Test
    public void testExplicitParamTypesXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodWithTypes("xml", "sum", new String[] {"2", "3"}, new String[] {"int", "java.lang.String"});
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("5", rootElement.getText());
    }

    @Test
    public void testImplicitParamTypesJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "sum", "2", "3");
        JSONObject result = new JSONObject(response.getText());
        assertEquals("5", result.get("result"));
    }

    @Test
    public void testImplicitParamTypesXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodGet("xml", "sum", "2", "3");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("5", rootElement.getText());
    }

    @Test
    public void testDateParamGetJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "testDateParam", "2015-01-02");
        JSONObject result = new JSONObject(response.getText());
        assertEquals("2015-01-02 00:00:00.000", result.get("result"));
    }

    @Test
    public void testDateParamGetXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodGet("xml", "testDateParam", "2015-01-02");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("2015-01-02 00:00:00.000", rootElement.getText());
    }

    @Test
    public void testDateTimeParamGetJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "testDateParam", "2015-01-02 01:02:03.004");
        JSONObject result = new JSONObject(response.getText());
        assertEquals("2015-01-02 01:02:03.004", result.get("result"));
    }

    @Test
    public void testDateTimeParamGetXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodGet("xml", "testDateParam", "2015-01-02 01:02:03.004");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("2015-01-02 01:02:03.004", rootElement.getText());
    }

    @Test
    public void testBigDecimalParamGetJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "testBigDecimalParam", "1.234");
        JSONObject result = new JSONObject(response.getText());
        assertEquals("1.234", result.get("result"));
    }

    @Test
    public void testBigDecimalParamGetXML() throws IOException, SAXException {
        WebResponse response = invokeServiceMethodGet("xml", "testBigDecimalParam", "1.234");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        assertEquals("1.234", rootElement.getText());
    }

    @Test
    public void testExceptionIfSeveralMethodsAndImplicitTypes() throws IOException, SAXException {
        exception.expect(HttpException.class);
        invokeServiceMethodGet("json", "overloadedMethod", "2");
    }

    @Test
    public void testExceptionIfNoMethodFound() throws IOException, SAXException {
        exception.expect(HttpException.class);
        exception.expectMessage(containsString("500 Internal Error"));
        invokeServiceMethodGet("json", "nonExistingMethod");
    }

    @Test
    public void testExceptionIfNoMethodPermissions() throws IOException, SAXException {
        exception.expect(HttpException.class);
        exception.expectMessage(containsString("Error on HTTP request: 403"));
        invokeServiceMethodGet("json", "notPermittedMethod");
    }

    @Test
    public void testReturnEntityJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "findCar", carUuidString, "carEdit");

        JSONObject responseObject = new JSONObject(response.getText());
        JSONObject resultObject = responseObject.getJSONObject("result");
        assertNotNull(resultObject);
        assertEquals("ref_Car-" + carUuidString, resultObject.getString("id"));

        JSONObject colourObject = resultObject.getJSONObject("colour");
        assertNotNull(colourObject);
        assertEquals("ref$Colour-" + colourUuidString, colourObject.getString("id"));

        //test cyclic references
        JSONArray repairs = resultObject.getJSONArray("repairs");
        JSONObject repairObject = repairs.getJSONObject(0);
        JSONObject repairCar = repairObject.getJSONObject("car");
        assertEquals("ref_Car-" + carUuidString, repairCar.getString("id"));
        assertEquals(1, repairCar.length());
    }

    @Test
    public void testReturnEntityXML() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("xml", "findCar", carUuidString, "carEdit");
        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        Element instances = rootElement.element(XMLConverter.ROOT_ELEMENT_INSTANCE);
        List<Element> instanceList = Dom4j.elements(instances, XMLConverter.ELEMENT_INSTANCE);
        assertEquals(1, instanceList.size());
        Element instance = instanceList.get(0);
        assertEquals("ref_Car-" + carUuidString, instance.attributeValue("id"));

        Element colourInstance = (Element) instance.selectSingleNode("reference[@name='colour']/instance");
        assertNotNull(colourInstance);
        assertEquals("ref$Colour-" + colourUuidString, colourInstance.attributeValue("id"));

        //test cyclic references
        Element repairCarInstanceEl = (Element) instance.selectSingleNode("collection[@name='repairs']/instance/reference[@name='car']/instance");
        assertEquals("ref_Car-" + carUuidString, repairCarInstanceEl.attributeValue("id"));
        assertEquals(0, repairCarInstanceEl.elements().size());
    }

    @Test
    public void testReturnEntityListJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("json", "findAllCars", "carBrowse");

        JSONObject responseObject = new JSONObject(response.getText());
        JSONArray resultArray = responseObject.getJSONArray("result");
        assertNotNull(resultArray);
        assertEquals(2, resultArray.length());

        JSONObject carObject = resultArray.getJSONObject(0);

        String colourId = carObject.getJSONObject("colour").getString("id");
        assertEquals("ref$Colour-" + colourUuidString, colourId);
    }

    @Test
    public void testReturnEntityListXML() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodGet("xml", "findAllCars", "carBrowse");

        Document document = Dom4j.readDocument(response.getText());
        Element rootElement = document.getRootElement();
        Element instancesEl = rootElement.element(XMLConverter.ROOT_ELEMENT_INSTANCE);
        List<Element> instanceList = Dom4j.elements(instancesEl, XMLConverter.ELEMENT_INSTANCE);
        assertEquals(2, instanceList.size());

        Node car1El = instancesEl.selectSingleNode("instance[@id='ref_Car-" + carUuidString + "']");
        assertNotNull(car1El);

        Node car2El = instancesEl.selectSingleNode("instance[@id='ref_Car-" + secondCarUuidString + "']");
        assertNotNull(car2El);
    }

    @Test
    public void testPostWithEntityParamJSON() throws IOException, SAXException, JSONException {
        Map<String, String> replacements = new HashMap<>();
        String carVin = "VF0001";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$CAR_VIN$", carVin);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        WebResponse response = invokeServiceMethodPost("update_car_vin_service_post.json", replacements, "application/json;charset=UTF-8");
        JSONObject responseObject = new JSONObject(response.getText());
        JSONObject resultObject = responseObject.getJSONObject("result");
        assertNotNull(resultObject);
        assertEquals("ref_Car-" + carUuidString, resultObject.getString("id"));
        assertEquals(carNewVin, resultObject.getString("vin"));

        JSONObject colourObject = resultObject.getJSONObject("colour");
        assertEquals("ref$Colour-" + colourUuidString, colourObject.getString("id"));
        assertEquals(colourName, colourObject.getString("name"));

        JSONArray driverAllocations = resultObject.getJSONArray("driverAllocations");
        assertEquals(2, driverAllocations.length());

        JSONArray repairs = resultObject.getJSONArray("repairs");
        assertEquals(2, repairs.length());

    }

    @Test
    public void testPostWithEntityParamXML() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin = "VF0001";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID$", carUuidString);
        replacements.put("$CAR_VIN$", carVin);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        WebResponse response = invokeServiceMethodPost("update_car_vin_service_post.xml", replacements, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        Element instanceEl = (Element) document.selectSingleNode("result/instances/instance");
        assertNotNull(instanceEl);

        assertEquals("ref_Car-" + carUuidString, instanceEl.attributeValue("id"));
        Element colourEl = referenceInstanceElement(instanceEl, "colour");
        assertEquals("ref$Colour-" + colourUuidString, idAttr(colourEl));
        assertEquals(colourName, fieldValue(colourEl, "name"));

        List<Element> driverAllocations = collectionInstanceElements(instanceEl, "driverAllocations");
        assertEquals(2, driverAllocations.size());

        List<Element> repairs = collectionInstanceElements(instanceEl, "repairs");
        assertEquals(2, repairs.size());
    }

    @Test
    public void testPostWithCollectionParamJSON() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin1 = "VF0001";
        String carVin2 = "VF0002";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID_1$", carUuidString);
        replacements.put("$CAR_VIN_1$", carVin1);
        replacements.put("$CAR_ID_2$", secondCarUuidString);
        replacements.put("$CAR_VIN_2$", carVin2);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        WebResponse response = invokeServiceMethodPost("update_car_vins_service_post.json", replacements, "application/json;charset=UTF-8");
        JSONObject responseObject = new JSONObject(response.getText());
        JSONArray resultArray = responseObject.getJSONArray("result");
        assertEquals(2, resultArray.length());

        JSONObject car1 = resultArray.getJSONObject(0);
        assertEquals("ref_Car-" + carUuidString, car1.getString("id"));
        assertEquals(carNewVin, car1.getString("vin"));

        JSONObject colourObject = car1.getJSONObject("colour");
        assertEquals("ref$Colour-" + colourUuidString, colourObject.getString("id"));
        assertEquals(colourName, colourObject.getString("name"));

        JSONArray driverAllocations = car1.getJSONArray("driverAllocations");
        assertEquals(2, driverAllocations.length());

        JSONArray repairs = car1.getJSONArray("repairs");
        assertEquals(2, repairs.length());

        JSONObject car2 = resultArray.getJSONObject(1);
        assertEquals("ref_Car-" + secondCarUuidString, car2.getString("id"));
    }

    @Test
    public void testPostWithCollectionParamXML() throws Exception {
        Map<String, String> replacements = new HashMap<>();
        String carVin1 = "VF0001";
        String carVin2 = "VF0002";
        String carNewVin = "NEW_VIN";
        String colourName = "New colour";
        String driverAllocationId1 = UUID.randomUUID().toString();
        String driverAllocationId2 = UUID.randomUUID().toString();
        String repairId1 = UUID.randomUUID().toString();
        String repairId2 = UUID.randomUUID().toString();
        replacements.put("$CAR_ID_1$", carUuidString);
        replacements.put("$CAR_VIN_1$", carVin1);
        replacements.put("$CAR_ID_2$", secondCarUuidString);
        replacements.put("$CAR_VIN_2$", carVin2);
        replacements.put("$CAR_NEW_VIN$", carNewVin);
        replacements.put("$COLOUR_ID$", colourUuidString);
        replacements.put("$COLOUR_NAME$", colourName);
        replacements.put("$DRIVER_ALLOCATION_ID_1$", driverAllocationId1);
        replacements.put("$DRIVER_ALLOCATION_ID_2$", driverAllocationId2);
        replacements.put("$REPAIR_ID_1$", repairId1);
        replacements.put("$REPAIR_ID_2$", repairId2);

        WebResponse response = invokeServiceMethodPost("update_car_vins_service_post.xml", replacements, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instances = document.selectNodes("result/instances/instance");
        assertEquals(2, instances.size());

        Element car1El = (Element) instances.get(0);

        assertEquals("ref_Car-" + carUuidString, car1El.attributeValue("id"));
        Element colourEl = referenceInstanceElement(car1El, "colour");
        assertEquals("ref$Colour-" + colourUuidString, idAttr(colourEl));
        assertEquals(colourName, fieldValue(colourEl, "name"));

        List<Element> driverAllocations = collectionInstanceElements(car1El, "driverAllocations");
        assertEquals(2, driverAllocations.size());

        List<Element> repairs = collectionInstanceElements(car1El, "repairs");
        assertEquals(2, repairs.size());

        Element car2El = (Element) instances.get(1);
        assertEquals("ref_Car-" + secondCarUuidString, car2El.attributeValue("id"));
    }

    @Test
    public void testPostWithImplicitParamTypesJSON() throws IOException, SAXException, JSONException {
        WebResponse response = invokeServiceMethodPost("service_explicit_param_post.json", Collections.EMPTY_MAP, "application/json;charset=UTF-8");
        JSONObject jsonObject = new JSONObject(response.getText());
        assertEquals("String", jsonObject.get("result"));
    }

    @Test
    public void testPostWithImplicitParamTypesXML() throws Exception {
        WebResponse response = invokeServiceMethodPost("service_explicit_param_post.xml", Collections.EMPTY_MAP, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        assertEquals("String", document.getRootElement().getText());
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

    private WebResponse invokeServiceMethodGet(String type, String methodName, String... params) throws IOException, SAXException {
        String serviceName = PortalTestService.NAME;
        StringBuilder sb = new StringBuilder();
        sb.append("app-portal/api/service.").append(type);
        sb.append("?s=").append(sessionId)
                .append("&service=").append(serviceName)
                .append("&method=").append(methodName);
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            sb.append("&param").append(i).append("=").append(param);
        }
        return GET(sb.toString(), "text/html;charset=UTF-8");
    }

    private WebResponse invokeServiceMethodPost(String fileName, Map<String, String> replacements, String type) throws IOException, SAXException {
        StringBuilder sb = new StringBuilder();
        sb.append("app-portal/api/service")
            .append("?s=").append(sessionId);
        String fileContent = getFileContent(fileName, replacements);
        return POST(sb.toString(), fileContent, type);
    }

    protected String getFileContent(String fileName, Map<String, String> replacements) throws IOException {
        InputStream is = getClass().getResourceAsStream(fileName);
        String fileContent = IOUtils.toString(is);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            fileContent = fileContent.replace(entry.getKey(), entry.getValue());
        }
        return fileContent;
    }

    private WebResponse invokeServiceMethodWithTypes(String type, String methodName, String[] paramValues, String[] paramTypes) throws IOException, SAXException {
        String serviceName = PortalTestService.NAME;
        StringBuilder sb = new StringBuilder();
        sb.append("app-portal/api/service.").append(type);
        sb.append("?s=").append(sessionId)
                .append("&service=").append(serviceName)
                .append("&method=").append(methodName);
        for (int i = 0; i < paramValues.length; i++) {
            String paramValue = paramValues[i];
            sb.append("&param").append(i).append("=").append(paramValue);
        }
        for (int i = 0; i < paramTypes.length; i++) {
            String paramType = paramTypes[i];
            sb.append("&param").append(i).append("_type=").append(paramType);
        }
        return GET(sb.toString(), "text/html;charset=UTF-8");
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
}
