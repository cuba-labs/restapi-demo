/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.haulmont.masquerade.Connectors;
import com.haulmont.addon.restaddondemo.http.rest.jmx.CoreCachingFacadeJmxService;
import com.haulmont.addon.restaddondemo.http.rest.jmx.WebCachingFacadeJmxService;
import com.meterware.httpunit.*;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static com.haulmont.addon.restaddondemo.http.api.RestXmlUtils.*;
import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class DataServiceControllerFT {
    private static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";
    private static final String URI_BASE = "http://localhost:8080/";

    private static final String userLogin = "admin";
    private static final String userPassword = "admin";

    private String sessionId;

    private Connection conn;

    private String carUuidString;
    private String repairUuidString;
    private String modelUuidString;
    private String driverGroupUuidString;
    private String caseUuidString;
    private String debtUuidString;

    private String modelName = "Audi A3";
    private int modelNumberOfSeats = 5;

    private DataSet dirtyData = new DataSet();
    private WebConversation conv;

    private String apiPath;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"app/dispatch"},
                {"app-portal"}
        });
    }

    public DataServiceControllerFT(String api) {
        this.apiPath = api;
    }

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

        Connectors.jmx(CoreCachingFacadeJmxService.class)
                .clearDynamicAttributesCache();
        Connectors.jmx(WebCachingFacadeJmxService.class)
                .clearDynamicAttributesCache();
    }

    private WebResponse updateDynamicAttributesCache() {
        try {
            return GET(apiPath + "/api/service.json?s=" + sessionId + "&service=cuba_DynamicAttributesCacheService&method=loadCache", "text/html;charset=UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareDb() throws SQLException {
        UUID categoryId = dirtyData.createCategoryId();
        UUID categoryAttrId = dirtyData.createCategoryAttributeId();
        UUID categoryAttrId2 = dirtyData.createCategoryAttributeId();
        UUID categoryAttrId3 = dirtyData.createCategoryAttributeId();
        UUID categoryAttrValueId = dirtyData.createCategoryAttributeValueId();
        UUID categoryAttrValueId2 = dirtyData.createCategoryAttributeValueId();
        UUID categoryAttrValueId3 = dirtyData.createCategoryAttributeValueId();

        executePrepared("insert into sys_category(id, name, entity_type, discriminator) values (?, ?, ?, ?)",
                new PostgresUUID(categoryId),
                "Car default category",
                "ref_Car",
                0
        );

        executePrepared("insert into sys_category_attr (id, category_id, category_entity_type, name, code, data_type, is_collection) values (?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(categoryAttrId),
                new PostgresUUID(categoryId),
                "ref_Car",
                "Attribute 1",
                "attribute1",
                "STRING",
                false
        );

        executePrepared("insert into sys_category_attr (id, category_id, category_entity_type, name, code, data_type, entity_class, is_collection) values (?, ?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(categoryAttrId2),
                new PostgresUUID(categoryId),
                "ref_Car",
                "Attribute 2",
                "attribute2",
                "ENTITY",
                "com.haulmont.refapp.core.entity.Car",
                false
        );

        executePrepared("insert into sys_category_attr (id, category_id, category_entity_type, name, code, data_type, enumeration, is_collection) values (?, ?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(categoryAttrId3),
                new PostgresUUID(categoryId),
                "ref_Car",
                "Attribute 3",
                "attribute3",
                "ENUMERATION",
                "1,2,3,4",
                false
        );

        updateDynamicAttributesCache();

        UUID carUuid = dirtyData.createCarUuid();
        carUuidString = carUuid.toString();
        executePrepared("insert into ref_car(id, version, vin) values (?, ?, ?)",
                new PostgresUUID(carUuid),
                1L,
                "VWV000"
        );

        executePrepared("insert into sys_attr_value(id, category_attr_id, code, entity_id, string_value, version) values (?, ?, ?, ?, ?, 1)",
                new PostgresUUID(categoryAttrValueId),
                new PostgresUUID(categoryAttrId),
                "attribute1",
                new PostgresUUID(carUuid),
                "The value of dynamic attribute"
        );

        executePrepared("insert into sys_attr_value(id, category_attr_id, code, entity_id, entity_value, version) values (?, ?, ?, ?, ?, 1)",
                new PostgresUUID(categoryAttrValueId2),
                new PostgresUUID(categoryAttrId2),
                "attribute2",
                new PostgresUUID(carUuid),
                null
        );

        executePrepared("insert into sys_attr_value(id, category_attr_id, code, entity_id, string_value, version) values (?, ?, ?, ?, ?, 1)",
                new PostgresUUID(categoryAttrValueId3),
                new PostgresUUID(categoryAttrId3),
                "attribute3",
                new PostgresUUID(carUuid),
                null
        );

        UUID modelId = dirtyData.createModelUuid();
        modelUuidString = modelId.toString();
        executePrepared("insert into ref_model(id, name, number_of_seats, DTYPE, version) values (?, ?, ?, ?, 1)",
                new PostgresUUID(modelId), modelName, modelNumberOfSeats, "ref$ExtModel");

        UUID driverId = dirtyData.createDriverUuid();
        executePrepared("insert into ref_driver(id, DTYPE, version) values (?, 'ref$ExtDriver', 1)",
                new PostgresUUID(driverId));

        UUID driverAllocId = dirtyData.createDriverAllocUuid();
        executePrepared("insert into ref_driver_alloc(id, driver_id, car_id, create_ts) values(?, ?, ?, ?)",
                new PostgresUUID(driverAllocId),
                new PostgresUUID(driverId),
                new PostgresUUID(carUuid),
                Timestamp.valueOf("2012-01-13 12:24:48"));

        UUID repairId = dirtyData.createRepairUuid();
        repairUuidString = repairId.toString();
        executePrepared("insert into ref_repair(id, car_id, repair_date, version) values (?, ?, ?, 1)",
                new PostgresUUID(repairId),
                new PostgresUUID(carUuid),
                Date.valueOf("2012-01-13"));

        UUID driverGroupId = dirtyData.createDriverGroupUuid();
        driverGroupUuidString = driverGroupId.toString();
        executePrepared("insert into ref_driver_group(id, in_use, version) values (?, ?, 1)",
                new PostgresUUID(driverGroupId),
                Boolean.TRUE);


        UUID debtorId = dirtyData.createDebtorUUID();
        debtUuidString = debtorId.toString();
        executePrepared("insert into debt_debtor(id, title, version) values (?, ?, 1)",
                new PostgresUUID(debtorId),
                "copy paste");

        UUID caseId = dirtyData.createCaseUuid();
        caseUuidString = caseId.toString();
        executePrepared("insert into debt_case(id, test1, test2, test3, test4, debtor_id) values (?, ?, ?, ?, ?, ?)",
                new PostgresUUID(caseId),
                "test1",
                "test2",
                "test3",
                "test4",
                new PostgresUUID(debtorId));
    }

    private void assignModelToCar() throws Exception {
        executePrepared("update ref_car set model_id = ? where id = ?",
                new PostgresUUID(UUID.fromString(modelUuidString)),
                new PostgresUUID(UUID.fromString(carUuidString)));
    }

    @After
    public void tearDown() throws Exception {
        logout();
        dirtyData.cleanup(conn);

        if (conn != null) {
            conn.close();
        }
    }

    private void executePrepared(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            int count = stmt.executeUpdate();
        }
    }

    @Test
    public void find_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("ref_Car-" + carUuidString, car.getString("id"));
    }

    @Test
    public void find_parameters_non_persistent_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.json?e=debt$Case-" + caseUuidString +
                "-load-non-persistent-rest&s=" + sessionId, "charset=UTF-8");
        JSONObject caseEntity = new JSONObject(response.getText());

        assertNotNull(caseEntity);

        assertTrue(caseEntity.has("debtorFake"));
        assertEquals("debt$Debtor-" + debtUuidString, caseEntity.getJSONObject("debtorFake").getString("id"));
        assertTrue(caseEntity.getJSONObject("debtorFake").has("title"));

        assertTrue(caseEntity.has("doubleDebtor"));
        assertEquals(2, caseEntity.getJSONArray("doubleDebtor").length());

        assertTrue(caseEntity.has("nonPersistent1"));

        assertTrue(caseEntity.has("nonPersistent2"));
        assertEquals("test3 test4", caseEntity.getString("nonPersistent2"));

        assertTrue(caseEntity.has("nonPersistent3"));
    }

    @Test
    public void find_parameters_non_persistent_XML() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=debt$Case-" + caseUuidString +
                "-load-non-persistent-rest&s=" + sessionId, "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);

        assertEquals("debt$Case-" + caseUuidString, instanceEl.attributeValue("id"));

        Element debtorFakeInstanceEl = referenceInstanceElement(instanceEl, "debtorFake");
        assertEquals("debt$Debtor-" + debtUuidString, debtorFakeInstanceEl.attributeValue("id"));
        assertNotNull(debtorFakeInstanceEl.selectSingleNode("field[@name='title']"));

        List doubleDebtorInstanceElements = instanceEl.selectNodes("collection[@name='doubleDebtor']/instance");
        assertEquals(2, doubleDebtorInstanceElements.size());

        Element nonPersistent1El = fieldElement(instanceEl, "nonPersistent1");
        assertEquals("true", nonPersistent1El.attributeValue("null"));

        assertFieldValueEquals("test3 test4", instanceEl, "nonPersistent2");
        assertFieldValueEquals("copy paste", instanceEl, "nonPersistent3");
    }

    @Test
    public void find_view_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "-_minimal&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("ref_Car-" + carUuidString, car.getString("id"));
    }

    @Test
    public void find_XML() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("ref_Car-" + carUuidString, instanceEl.attributeValue("id"));
    }

    @Test
    public void query_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin&vin=VWV000&" + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.vin = :vin");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "vin");
        params.put("value", "VWV000");
        content.put("params", Collections.singletonList(params));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_view_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin&vin=VWV000" + "&s=" + sessionId +
                "&view=_minimal";
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_view_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.vin = :vin");
        content.put("view", "_minimal");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "vin");
        params.put("value", "VWV000");
        content.put("params", Collections.singletonList(params));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_XML_GET() throws IOException, SAXException {
        String url = apiPath + "/api/query.xml?e=ref_Car&q=select c from ref_Car c where c.vin = :vin&vin=VWV000&" + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("ref_Car-" + carUuidString, instanceEl.attributeValue("id"));
    }

    @Test
    public void query_XML_POST() throws IOException, SAXException {
        String url = apiPath + "/api/query?s=" + sessionId;
        String xml = prepareFile("query-car.xml", MapUtils.asMap("$ENTITY-TO_BE_REPLACED_ID$", "ref_Car",
                "$QUERY-TO_BE_REPLACED_ID$", "select c from ref_Car c where c.vin = :vin",
                "$PARAM_NAME-TO_BE_REPLACED$", "vin",
                "$PARAM_VALUE-TO_BE_REPLACED$", "VWV000"));

        WebResponse response = POST(url, xml, "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("ref_Car-" + carUuidString, instanceEl.attributeValue("id"));
    }

    @Test
    public void query_withFirstNumberAndMaxResults_JSON_GET() throws Exception {
        UUID newUuid = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, version, vin) values(?, ?, ?)",
                new PostgresUUID(newUuid),
                2l,
                "VWV000");
        UUID newUuid2 = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, version, vin) values(?, ?, ?)",
                new PostgresUUID(newUuid2),
                3l,
                "VWV000");

        String url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin order by c.version" +
                "&vin=VWV000&max=1&" + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));

        url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin order by c.version" +
                "&vin=VWV000&first=1&" + "s=" + sessionId;
        response = GET(url, "charset=UTF-8");
        cars = new JSONArray(response.getText());
        assertEquals(2, cars.length());
        assertEquals("ref_Car-" + newUuid.toString(), ((JSONObject) cars.get(0)).getString("id"));
        assertEquals("ref_Car-" + newUuid2.toString(), ((JSONObject) cars.get(1)).getString("id"));

        url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin order by c.version" +
                "&vin=VWV000&first=1&max=1&" + "s=" + sessionId;
        response = GET(url, "charset=UTF-8");
        cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + newUuid.toString(), ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_withFirstNumberAndMaxResults_JSON_POST() throws Exception {
        UUID newUuid = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, version, vin) values(?, ?, ?)", new PostgresUUID(newUuid), 2L, "VWV000");
        UUID newUuid2 = dirtyData.createCarUuid();
        executePrepared("insert into ref_car(id, version, vin) values(?, ?, ?)", new PostgresUUID(newUuid2), 3L, "VWV000");

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.vin = :vin order by c.version");
        content.put("max", 1);

        Map<String, Object> param = new HashMap<>();
        param.put("name", "vin");
        param.put("value", "VWV000");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);
        String url = apiPath + "/api/query?s=" + sessionId;
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + carUuidString, ((JSONObject) cars.get(0)).getString("id"));

        content.remove("max");
        content.put("first", 1);
        jsonObject = new JSONObject(content);
        response = POST(url, jsonObject.toString(), "application/json");
        cars = new JSONArray(response.getText());
        assertEquals("ref_Car-" + newUuid.toString(), ((JSONObject) cars.get(0)).getString("id"));
        assertEquals("ref_Car-" + newUuid2.toString(), ((JSONObject) cars.get(1)).getString("id"));

        content.put("max", 1);
        jsonObject = new JSONObject(content);
        response = POST(url, jsonObject.toString(), "application/json");
        cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("ref_Car-" + newUuid.toString(), ((JSONObject) cars.get(0)).getString("id"));
    }

    @Test
    public void query_parameters_timestamp_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref$AllocatedCar&" +
                "q=select a from ref$AllocatedCar a where a.allocTs = :allocTs&"
                + "allocTs=2012-01-13 12:24:48.000&"
                + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("ref$AllocatedCar-" + carUuidString, car.getString("id"));
    }

    @Test
    public void query_parameters_timestamp_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref$AllocatedCar");
        content.put("query", "select a from ref$AllocatedCar a where a.allocTs = :allocTs");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "allocTs");
        param.put("value", "2012-01-13 12:24:48.000");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("ref$AllocatedCar-" + carUuidString, car.getString("id"));
    }

    @Test
    public void query_parameters_date_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref$Repair&" +
                "q=select r from ref$Repair r where r.date = :repairDate&"
                + "repairDate=2012-01-13&"
                + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray repairs = new JSONArray(response.getText());
        assertEquals(1, repairs.length());
        JSONObject repair = repairs.getJSONObject(0);
        assertEquals("ref$Repair-" + repairUuidString, repair.getString("id"));
    }

    @Test
    public void query_parameters_date_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref$Repair");
        content.put("query", "select r from ref$Repair r where r.date = :repairDate");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "repairDate");
        param.put("value", "2012-01-13");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);

        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray repairs = new JSONArray(response.getText());
        assertEquals(1, repairs.length());
        JSONObject repair = repairs.getJSONObject(0);
        assertEquals("ref$Repair-" + repairUuidString, repair.getString("id"));
    }

    @Test
    public void query_parameters_uuid_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.id = :testId&"
                + "testId=" + carUuidString + "&"
                + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("VWV000", car.getString("vin"));
    }

    @Test
    public void query_parameters_uuid_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.id = :testId");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "testId");
        param.put("value", carUuidString);
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("VWV000", car.getString("vin"));
    }

    @Test
    public void query_parameters_boolean_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref$DriverGroup&" +
                "q=select g from ref$DriverGroup g where g.inUse = :inUse&"
                + "inUse=trUe&"
                + "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray driverGroups = new JSONArray(response.getText());
        assertEquals(1, driverGroups.length());
        JSONObject driverGroup = driverGroups.getJSONObject(0);
        assertEquals("ref$DriverGroup-" + driverGroupUuidString, driverGroup.getString("id"));
    }

    @Test
    public void query_parameters_boolean_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref$DriverGroup");
        content.put("query", "select g from ref$DriverGroup g where g.inUse = :inUse");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "inUse");
        param.put("value", "trUe");
        content.put("params", Collections.singletonList(param));
        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray driverGroups = new JSONArray(response.getText());
        assertEquals(1, driverGroups.length());
        JSONObject driverGroup = driverGroups.getJSONObject(0);
        assertEquals("ref$DriverGroup-" + driverGroupUuidString, driverGroup.getString("id"));
    }

    @Test
    public void query_parameters_string_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.vin = :vin&vin=VWV000&" +
                "s=" + sessionId;
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("ref_Car-" + carUuidString, car.getString("id"));
    }

    @Test
    public void query_parameters_string_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.vin = :vin");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "vin");
        param.put("value", "VWV000");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);
        WebResponse response = POST(url, jsonObject.toString(), "application/json");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        JSONObject car = cars.getJSONObject(0);
        assertEquals("ref_Car-" + carUuidString, car.getString("id"));
    }

    //Tests Integer, Long, BigDecimal & Double parameters
    @Test
    public void query_parameters_numeric_JSON_GET() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version = :version&version=1&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            fail("fail on using integer parameter in query");
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version %3C :version&version=1234567891234567&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail on using long parameter in query", e);
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version %3C :version&version=123456789123456789123456789&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail on using big decimal parameter in query", e);
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version %3C :version&version=3.14159&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail on using double parameter in query", e);
        }
    }

    @Test
    public void query_parameters_numeric_JSON_POST() throws Exception {
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.version = :version");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "version");
        param.put("value", "1");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            fail("fail on using integer parameter in query");
        }

        content.put("query", "select c from ref_Car c where c.version < :version");
        param.put("value", "1234567891234567");
        jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail on using long parameter in query", e);
        }

        param.put("value", "123456789123456789123456789");
        jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail on using big decimal parameter in query", e);
        }

        param.put("value", "3.14159");
        jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail on using double parameter in query", e);
        }
    }

    //Tests queries with specified parameters types
    @Test
    public void query_parameters_types_JSON_GET() throws Exception {
        //specify string type, when string could be parsed as numeric type
        String url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.vin = :vin&vin=007&vin_type=string&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail if string type specified using query", e);
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version = :version&version=1&version_type=int&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail if int type specified using query", e);
        }

        url = apiPath + "/api/query.json?e=ref$DriverGroup&" +
                "q=select g from ref$DriverGroup g where g.inUse = :inUse&"
                + "inUse=true&inUse_type=boolean&"
                + "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
        } catch (Exception e) {
            exceptionFail("fail if boolean parameter specified using query", e);
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version = :version&version=1&version_type=string&" +
                "s=" + sessionId;
        try {
            GET(url, "charset=UTF-8");
            fail("using string type instead of int succeed");
        } catch (HttpInternalErrorException e) {
            //ok
        }
    }

    //Tests queries with specified parameters types
    @Test
    public void query_parameters_types_JSON_POST() throws Exception {
        //specify string type, when string could be parsed as numeric type
        String url = apiPath + "/api/query?s=" + sessionId;

        Map<String, Object> content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.vin = :vin");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "vin");
        param.put("value", "007");
        param.put("type", "string");
        content.put("params", Collections.singletonList(param));

        JSONObject jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail if string type specified using query", e);
        }

        content.put("query", "select c from ref_Car c where c.version = :version");

        param = new HashMap<>();
        param.put("name", "version");
        param.put("value", "1");
        param.put("type", "int");
        content.put("params", Collections.singletonList(param));

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail if int type specified using query", e);
        }

        content = new HashMap<>();
        content.put("entity", "ref$DriverGroup");
        content.put("query", "select g from ref$DriverGroup g where g.inUse = :inUse");

        param = new HashMap<>();
        param.put("name", "inUse");
        param.put("value", "true");
        param.put("type", "boolean");
        content.put("params", Collections.singletonList(param));

        jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
        } catch (Exception e) {
            exceptionFail("fail if boolean parameter specified using query", e);
        }

        url = apiPath + "/api/query.json?e=ref_Car&" +
                "q=select c from ref_Car c where c.version = :version&version=1&version_type=string&" +
                "s=" + sessionId;

        content = new HashMap<>();
        content.put("entity", "ref_Car");
        content.put("query", "select c from ref_Car c where c.version = :version");

        param = new HashMap<>();
        param.put("name", "version");
        param.put("value", "1");
        param.put("type", "string");
        content.put("params", Collections.singletonList(param));

        jsonObject = new JSONObject(content);

        try {
            POST(url, jsonObject.toString(), "application/json");
            fail("using string type instead of int succeed");
        } catch (HttpInternalErrorException e) {
            //ok
        }
    }

    @Test
    public void commit_insertInstance_JSON() throws Exception {
        UUID newUuid = dirtyData.createCarUuid();
        String json = prepareFile("new_car.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car-" + newUuid)
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref_Car-" + newUuid.toString(), res.getJSONObject(0).getString("id"));

        response = GET(apiPath + "/api/find.json?e=ref_Car-" + newUuid + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("VWV111", car.getString("vin"));

        UUID repairUuid = dirtyData.createRepairUuid();
        json = prepareFile("new_repair.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Repair-" + repairUuid,
                "$CAR-TO_BE_REPLACED_ID$", "ref_Car-" + newUuid.toString()));

        response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        res = new JSONArray(response.getText());
        assertEquals("ref$Repair-" + repairUuid.toString(), res.getJSONObject(0).getString("id"));
    }

    @Test
    public void commit_insertInstance_XML() throws Exception {
        UUID newUuid = dirtyData.createCarUuid();
        String xml = prepareFile("new_car.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car-" + newUuid,
                "$TO_BE_REPLACED_ID$", newUuid.toString())
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        Element instanceEl = (Element) instanceNodes.get(0);
        assertFieldValueEquals("VWV-XML-NEW", instanceEl, "vin");

        response = GET(apiPath + "/api/find.xml?e=ref_Car-" + newUuid + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());
        instanceEl = (Element) instanceNodes.get(0);
        assertFieldValueEquals("VWV-XML-NEW", instanceEl, "vin");
    }

    @Test
    public void commit_insertInstance_longId_JSON() throws Exception {
        Long newId = dirtyData.createSellerId();
        String json = prepareFile("new_seller.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Seller-" + newId)
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$Seller-" + newId.toString(), res.getJSONObject(0).getString("id"));

        response = GET(apiPath + "/api/find.json?e=ref$Seller-" + newId + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject seller = new JSONObject(response.getText());
        assertEquals("Rolf", seller.getString("name"));
    }

    @Test
    public void commit_insertInstance_longId_XML() throws IOException, SAXException {
        Long newId = dirtyData.createSellerId();
        String xml = prepareFile("new_seller.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Seller-" + newId,
                "$TO_BE_REPLACED_ID$", newId.toString())
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instance = (Element) instanceElements.get(0);
        Element nameEl = fieldElement(instance, "name");
        assertEquals("Rolf", nameEl.getText());

        response = GET(apiPath + "/api/find.xml?e=ref$Seller-" + newId + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        instance = (Element) instanceElements.get(0);
        nameEl = fieldElement(instance, "name");
        assertEquals("Rolf", nameEl.getText());
    }

    @Test
    public void commit_insertInstance_stringId_JSON() throws Exception {
        String json = prepareFile("new_currency.json", Collections.emptyMap());
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$Currency-{usd}", res.getJSONObject(0).getString("id"));

        response = GET(apiPath + "/api/find.json?e=ref$Currency-%7Busd%7D&s=" + sessionId,
                "charset=UTF-8");
        JSONObject seller = new JSONObject(response.getText());
        assertEquals("usd", seller.getString("code"));
        assertEquals("US Dollars", seller.getString("name"));

        deleteCurrency("usd");
    }

    @Test
    public void commit_insertInstance_stringId_XML() throws IOException, SAXException, SQLException {
        String xml = prepareFile("new_currency.xml", Collections.emptyMap());
        WebResponse response = POST(apiPath + "/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instance = (Element) instanceElements.get(0);
        Element nameEl = fieldElement(instance, "name");
        assertEquals("US Dollars", nameEl.getText());

        response = GET(apiPath + "/api/find.xml?e=ref$Currency-%7Busd%7D&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        instance = (Element) instanceElements.get(0);

        Element codeEl = fieldElement(instance, "code");
        assertEquals("usd", codeEl.getText());

        nameEl = fieldElement(instance, "name");
        assertEquals("US Dollars", nameEl.getText());

        deleteCurrency("usd");
    }

    @Test
    public void commit_insertInstance_autogenerateUUID_JSON() throws Exception {
        String json = prepareFile("new_car.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car")
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        String id = res.getJSONObject(0).getString("id");
        assertTrue(id.startsWith("ref_Car-"));

        //delete created car
        deleteCar(id.substring("ref_Car-".length()));
    }

    @Test
    public void commit_insertInstance_autogenerateUUID_XML() throws IOException, SAXException, SQLException {
        String xml = prepareFile("new_car.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car",
                "$TO_BE_REPLACED_ID$", "NEW")
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instance = (Element) instanceElements.get(0);
        Element nameEl = fieldElement(instance, "vin");
        assertEquals("VWV-XML-NEW", nameEl.getText());

        String id = instance.attributeValue("id");
        deleteCar(id.substring("ref_Car-".length()));
    }

    @Test
    public void commit_insertInstance_autogenerateLongId_JSON() throws Exception {
        String json = prepareFile("new_seller.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Seller")
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        String id = res.getJSONObject(0).getString("id");
        assertTrue(id.startsWith("ref$Seller-"));

        deleteSeller(id.substring("ref$Seller-".length()));
    }

    @Test
    public void commit_insertInstance_autogenerateLongId_XML() throws IOException, SAXException, SQLException {
        String xml = prepareFile("new_seller.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Seller",
                "$TO_BE_REPLACED_ID$", "NEW")
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instance = (Element) instanceElements.get(0);
        Element nameEl = fieldElement(instance, "name");
        assertEquals("Rolf", nameEl.getText());

        String id = instance.attributeValue("id");
        deleteSeller(id.substring("ref$Seller-".length()));
    }

    @Test
    public void commit_insertInstance_twoInstancesReferencingTheSame_JSON() throws Exception {
        UUID colorUuid = dirtyData.createColourUuid();
        UUID newUuid1 = dirtyData.createCarUuid();
        UUID newUuid2 = dirtyData.createCarUuid();
        String json = prepareFile("two_new_cars.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID_1$", "NEW-ref_Car-" + newUuid1.toString(),
                "$TO_BE_REPLACED_ID_1$", newUuid1.toString(),
                "$ENTITY-TO_BE_REPLACED_ID_2$", "NEW-ref_Car-" + newUuid2.toString(),
                "$TO_BE_REPLACED_ID_2$", newUuid2.toString(),
                "$ENTITY-COLOUR_ID$", "NEW-ref$Colour-" + colorUuid.toString(),
                "$COLOUR_ID$", "ref$Colour-" + colorUuid.toString()
        ));
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals(3, res.length());
        for (int i = 0; i < res.length(); i++) {
            String id = res.getJSONObject(i).getString("id");
            assertTrue(id.equals("ref$Colour-" + colorUuid) || id.equals("ref_Car-" + newUuid1) || id.equals("ref_Car-" + newUuid2));
        }
    }

    @Test
    public void commit_insertInstance_referencingInstances_JSON() throws Exception {
        UUID colorUuid = dirtyData.createColourUuid();
        UUID newUuid = dirtyData.createCarUuid();
        String json = prepareFile("referencing.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car-" + newUuid.toString(),
                "$ENTITY-COLOUR_ID$", "NEW-ref$Colour-" + colorUuid.toString(),
                "$COLOUR_ID$", "ref$Colour-" + colorUuid.toString()
        ));
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());

        for (int i = 0; i < res.length(); i++) {
            String id = res.getJSONObject(i).getString("id");
            assertTrue(id.equals("ref$Colour-" + colorUuid) || id.equals("ref_Car-" + newUuid));
        }
    }

    @Test
    public void commit_insertInstance_referencingInstances_XML() throws IOException, SAXException {
        UUID colorUuid = dirtyData.createColourUuid();
        UUID newCarUuid = dirtyData.createCarUuid();
        String carsXml = prepareFile("referencing.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref_Car-" + newCarUuid.toString(),
                "$ENTITY-COLOUR_ID$", "NEW-ref$Colour-" + colorUuid.toString(),
                "$COLOUR_ID$", "ref$Colour-" + colorUuid.toString()
        ));
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, carsXml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(2, instanceElements.size());

        Element carInstanceEl = (Element) document.selectSingleNode("/instances/instance[@id='ref_Car-" + newCarUuid + "']");
        assertNotNull(carInstanceEl);
        assertFieldValueEquals("VWV-XML-NEW", carInstanceEl, "vin");
        Element nestedColorInstanceEl = (Element) carInstanceEl.selectSingleNode("reference[@name='colour']/instance");
        assertEquals("ref$Colour-" + colorUuid, nestedColorInstanceEl.attributeValue("id"));
        fieldElement(carInstanceEl, "vin");

        Element colourInstanceEl = (Element) document.selectSingleNode("/instances/instance[@id='ref$Colour-" + colorUuid + "']");
        assertNotNull(colourInstanceEl);
        assertFieldValueEquals("RED", colourInstanceEl, "name");

        String url = apiPath + "/api/query.xml?e=ref_Car&"
                + "q=select c from ref_Car c where c.colour.name = :color&color=RED&"
                + "s=" + sessionId;
        response = GET(url, "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        carInstanceEl = (Element) instanceElements.get(0);
        assertEquals("ref_Car-" + newCarUuid, carInstanceEl.attributeValue("id"));

    }

    @Test
    public void commit_insertInstance_twoInstancesReferencingTheSame_XML() throws IOException, SAXException {
        UUID colorUuid = dirtyData.createColourUuid();
        UUID newUuid1 = dirtyData.createCarUuid();
        UUID newUuid2 = dirtyData.createCarUuid();
        String carsXml = prepareFile("two_new_cars.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID_1$", "NEW-ref_Car-" + newUuid1.toString(),
                "$TO_BE_REPLACED_ID_1$", newUuid1.toString(),
                "$ENTITY-TO_BE_REPLACED_ID_2$", "NEW-ref_Car-" + newUuid2.toString(),
                "$TO_BE_REPLACED_ID_2$", newUuid2.toString(),
                "$ENTITY-COLOUR_ID$", "NEW-ref$Colour-" + colorUuid.toString(),
                "$COLOUR_ID$", colorUuid.toString()
        ));
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, carsXml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(3, instanceElements.size());

        String url = apiPath + "/api/query.xml?e=ref_Car&"
                + "q=select c from ref_Car c where c.colour.name = :color&color=RED&"
                + "s=" + sessionId;
        response = GET(url, "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(2, instanceElements.size());

        Element carInstanceEl = (Element) document.selectSingleNode("/instances/instance[@id='ref_Car-" + newUuid1 + "']");
        assertNotNull(carInstanceEl);

        carInstanceEl = (Element) document.selectSingleNode("/instances/instance[@id='ref_Car-" + newUuid2 + "']");
        assertNotNull(carInstanceEl);
    }

    @Test
    public void commit_insertInstance_withEmbedded_JSON() throws Exception {
        UUID newUuid = dirtyData.createDriverUuid();
        String json = prepareFile("new_driver.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Driver-" + newUuid,
                "$TO_BE_REPLACED_ID$", newUuid.toString())
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");

        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref$ExtDriver-" + newUuid.toString(), res.getJSONObject(0).getString("id"));

        response = GET(apiPath + "/api/find.json?e=ref$Driver-" + newUuid + "-driverEdit" + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("Sam Smith", car.getString("name"));
        JSONObject address = car.getJSONObject("address");
        assertEquals("Austin", address.getString("city"));
    }

    @Test
    public void commit_insertInstance_withEmbedded_XML() throws IOException, SAXException {
        UUID newUuid = dirtyData.createDriverUuid();
        String xml = prepareFile("new_driver.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "NEW-ref$Driver-" + newUuid,
                "$TO_BE_REPLACED_ID$", newUuid.toString())
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("Sam Smith", instanceEl, "name");

        response = GET(apiPath + "/api/find.xml?e=ref$Driver-" + newUuid + "-driverEdit" + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("Sam Smith", instanceEl, "name");
        Element addressInstanceEl = (Element) instanceEl.selectSingleNode("reference[@name='address']/instance");
        assertFieldValueEquals("USA", addressInstanceEl, "country");
        assertFieldValueEquals("TX", addressInstanceEl, "state");
        assertFieldValueEquals("Austin", addressInstanceEl, "city");
    }

    @Test
    public void commit_updateInstance_JSON() throws Exception {
        String json = prepareFile("modified_car.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref_Car-" + carUuidString, res.getJSONObject(0).getString("id"));
        assertEquals(2, res.getJSONObject(0).getInt("version"));

        response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("VWV-XML-MODIFIED", car.getString("vin"));
    }

    @Test
    public void commit_updateInstance_nullField_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.json?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject model = new JSONObject(response.getText());
        assertEquals(modelName, model.getString("name"));
        assertEquals(modelNumberOfSeats, model.getInt("numberOfSeats"));

        String json = prepareFile("modify_model_null_field.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Model-" + modelUuidString)
        );
        POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");

        response = GET(apiPath + "/api/find.json?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        model = new JSONObject(response.getText());
        assertTrue(model.isNull("name"));
        assertTrue(model.isNull("numberOfSeats"));
    }

    @Test
    public void commit_updateInstance_nullField_XML() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        Element model = (Element) document.selectSingleNode("instances/instance");
        assertEquals(modelName, fieldValue(model, "name"));
        assertEquals(String.valueOf(modelNumberOfSeats), fieldValue(model, "numberOfSeats"));

        String xml = prepareFile("modify_model_null_field.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Model-" + modelUuidString)
        );
        POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");

        response = GET(apiPath + "/api/find.xml?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        model = (Element) document.selectSingleNode("instances/instance");
        assertEquals("true", fieldElement(model, "name").attributeValue("null"));
        assertEquals("true", fieldElement(model, "numberOfSeats").attributeValue("null"));
    }

    @Test
    public void commit_updateInstance_nullRef_JSON() throws Exception {
        assignModelToCar();
        WebResponse response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        JSONObject model = car.getJSONObject("model");
        assertEquals("ref$ExtModel-" + modelUuidString, model.getString("id"));

        String json = prepareFile("modified_car_null_ref.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$VIEW_NAME$", "carEdit")
        );
        POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");

        response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        car = new JSONObject(response.getText());
        Object newModel = car.get("model");
        assertEquals(JSONObject.NULL, newModel);
    }

    @Test
    public void commit_updateInstance_nullRef_XML() throws Exception {
        assignModelToCar();
        String xml = prepareFile("modified_car_null_ref.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$VIEW_NAME$", "carEdit")
        );
        POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");

        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        Element instanceEl = (Element) document.selectSingleNode("/instances/instance");
        Element modelEl = referenceElement(instanceEl, "model");
        assertEquals("true", modelEl.attributeValue("null"));
    }

    @Test
    public void commit_updateInstance_nullRef_withEl_XML() throws Exception {
        assignModelToCar();
        String xml = prepareFile("modified_car_null_ref_with_null_el.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$VIEW_NAME$", "carEdit")
        );
        POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");

        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "-carEdit&s=" + sessionId,
                "charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        Element instanceEl = (Element) document.selectSingleNode("/instances/instance");
        Element modelEl = referenceElement(instanceEl, "model");
        assertEquals("true", modelEl.attributeValue("null"));
    }

    @Test
    public void commit_updateInstance_XML() throws Exception {
        String xml = prepareFile("modified_car.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("2", instanceEl, "version");
        assertFieldValueEquals("VWV-XML-MODIFIED", instanceEl, "vin");

        response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("VWV-XML-MODIFIED", instanceEl, "vin");
    }

    @Test
    public void commit_removeInstance_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        String json = prepareFile("removed_car.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );
        response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref_Car-" + carUuidString, res.getJSONObject(0).getString("id"));
        assertFalse(res.getJSONObject(0).isNull("version"));

        try {
            GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                    "charset=UTF-8");
            fail();
        } catch (HttpNotFoundException e) {
        }
    }

    @Test
    public void commit_removeInstance_XML() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        String xml = prepareFile("removed_car.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString));
        response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instanceEl = (Element) instanceElements.get(0);
        assertFieldValueEquals("2", instanceEl, "version");
        assertFieldValueEquals("VWV000", instanceEl, "vin");
        assertFieldValueEquals("admin", instanceEl, "deletedBy");

        exception.expect(HttpNotFoundException.class);
        GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");

        int count = selectCount("ref_car", UUID.fromString(carUuidString));
        assertEquals(1, count);
    }

    @Test
    public void commit_removeInstanceSoft_JSON() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        String json = prepareFile("soft_removed_car.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );
        response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref_Car-" + carUuidString, res.getJSONObject(0).getString("id"));
        assertEquals(2, res.getJSONObject(0).getInt("version"));
        assertNotNull(res.getJSONObject(0).getString("deleteTs"));
        assertEquals("admin", res.getJSONObject(0).getString("deletedBy"));

        try {
            GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                    "charset=UTF-8");
            fail();
        } catch (HttpNotFoundException e) {
        }
    }

    @Test
    public void commit_removeInstanceSoft_XML() throws Exception {
        WebResponse response = GET(apiPath + "/api/find.xml?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        String xml = prepareFile("soft_removed_entity.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref$Model-" + modelUuidString,
                "$TO_BE_REPLACED_ID$", modelUuidString));
        response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());

        exception.expect(HttpNotFoundException.class);
        GET(apiPath + "/api/find.xml?e=ref$Model-" + modelUuidString + "&s=" + sessionId,
                "charset=UTF-8");

        int count = selectCount("ref_model", UUID.fromString(modelUuidString));
        assertEquals(0, count);
    }

    @Test
    public void deployViews() throws Exception {
        String xml = prepareFile("new_views.xml", Collections.EMPTY_MAP);
        POST(apiPath + "/api/deployViews?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");

        WebResponse response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());

        Document document = Dom4j.readDocument(response.getText());
        Element car = (Element) document.selectSingleNode("instances/instance");
        assertFieldValueEquals("VWV000", car, "vin");

        response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "-car.noVinListData&s=" + sessionId,
                "charset=UTF-8");
        assertNotNull(response.getText());
        document = Dom4j.readDocument(response.getText());
        car = (Element) document.selectSingleNode("instances/instance");
        assertNull(car.selectSingleNode("field[@name='vin']"));
    }

    @Test
    public void printDomain() throws IOException, SAXException {
        WebResponse response = GET(apiPath + "/api/printDomain?" + "s=" + sessionId,
                "text/html;charset=UTF-8");
        String txt = response.getText();
        assertTrue(txt.contains("<h1>Domain model description</h1>"));
    }

    @Test
    public void query_view_JSON_dynamicAttributes() throws Exception {
        String url = apiPath + "/api/query.json?e=ref_Car&q=select c from ref_Car c where c.vin = :vin&vin=VWV000" + "&s=" + sessionId +
                "&view=_minimal&dynamicAttributes=true";
        WebResponse response = GET(url, "charset=UTF-8");
        JSONArray cars = new JSONArray(response.getText());
        assertEquals(1, cars.length());
        assertEquals("The value of dynamic attribute", ((JSONObject) cars.get(0)).getString("+attribute1"));
    }

    @Test
    public void query_view_XML_dynamicAttributes() throws Exception {
        String url = apiPath + "/api/query.xml?e=ref_Car&q=select c from ref_Car c where c.vin = :vin&vin=VWV000&"
                + "s=" + sessionId + "&dynamicAttributes=true";
        WebResponse response = GET(url, "charset=UTF-8");

        Document document = Dom4j.readDocument(response.getText());

        LoggerFactory.getLogger("ERROR").warn("Response {}", response.getText());

        List instanceNodes = document.selectNodes("/instances/instance");
        assertEquals(1, instanceNodes.size());

        Element instanceEl = (Element) instanceNodes.get(0);
        assertEquals("ref_Car-" + carUuidString, instanceEl.attributeValue("id"));

        String dynamicAttributeValue = null;
        for (Element field : instanceEl.elements("field")) {
            if (field.attributeValue("name").equals("+attribute1")) {
                dynamicAttributeValue = field.getText();
            }
        }
        assertNotNull(dynamicAttributeValue);
        assertEquals("The value of dynamic attribute", dynamicAttributeValue);
    }

    @Test
    public void commit_updateInstance_dynamicAttribute_JSON() throws Exception {
        String json = prepareFile("modified_car_dynamic_attribute.json", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );
        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, json,
                "application/json;charset=UTF-8");
        JSONArray res = new JSONArray(response.getText());
        assertEquals("ref_Car-" + carUuidString, res.getJSONObject(0).getString("id"));

        response = GET(apiPath + "/api/find.json?e=ref_Car-" + carUuidString + "&s=" + sessionId + "&dynamicAttributes=true",
                "charset=UTF-8");
        JSONObject car = new JSONObject(response.getText());
        assertEquals("Modified dynamic attribute", car.getString("+attribute1"));
        assertEquals("3", car.getString("+attribute3"));
        assertEquals("ref_Car-" + carUuidString, car.getJSONObject("+attribute2").getString("id"));
    }

    @Test
    public void commit_updateInstance_dynamicAttribute_XML() throws Exception {
        String xml = prepareFile("modified_car_dynamic_attribute.xml", MapUtils.asMap(
                "$ENTITY-TO_BE_REPLACED_ID$", "ref_Car-" + carUuidString,
                "$TO_BE_REPLACED_ID$", carUuidString)
        );

        WebResponse response = POST("/app/dispatch/api/commit?" + "s=" + sessionId, xml,
                "text/xml;charset=UTF-8");
        Document document = Dom4j.readDocument(response.getText());
        List instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        Element instanceEl = (Element) instanceElements.get(0);

        response = GET(apiPath + "/api/find.xml?e=ref_Car-" + carUuidString + "&s=" + sessionId + "&dynamicAttributes=true",
                "charset=UTF-8");
        document = Dom4j.readDocument(response.getText());
        instanceElements = document.selectNodes("/instances/instance");
        assertEquals(1, instanceElements.size());
        instanceEl = (Element) instanceElements.get(0);

        String attribute2Value = null;
        assertFieldValueEquals("Modified dynamic attribute", instanceEl, "+attribute1");
        assertFieldValueEquals("3", instanceEl, "+attribute3");

        for (Element field : instanceEl.elements("reference")) {
            if (field.attributeValue("name").equals("+attribute2")) {
                Element instance = field.element("instance");
                attribute2Value = instance.attributeValue("id");
            }
        }
        assertEquals("ref_Car-" + carUuidString, attribute2Value);
    }

    private void deleteCar(String id) throws SQLException {
        executePrepared("delete from ref_car where id = ?",
                new PostgresUUID(UUID.fromString(id)));
    }

    private void deleteSeller(String id) throws SQLException {
        executePrepared("delete from ref_seller where id = ?", Long.valueOf(id));
    }

    private void deleteCurrency(String code) throws SQLException {
        executePrepared("delete from ref_currency where code = ?", code);
    }

    private int selectCount(String table, UUID uuid) throws SQLException {
        String sql = "select count(*) from " + table + "where id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        Object param = new PostgresUUID(uuid);
        stmt.setObject(1, param);
        ResultSet rs = stmt.executeQuery();
        return rs.getInt(0);
    }

    private String login(String login, String password) throws JSONException, IOException, SAXException {
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("username", login);
        loginJSON.put("password", password);
        loginJSON.put("locale", "ru");

        WebResponse response = POST("/app/dispatch/api/login",
                loginJSON.toString(), "application/json;charset=UTF-8");
        return response.getText();
    }

    private void logout() throws JSONException {
        if (sessionId == null)
            return;
        try {
            GET(apiPath + "/api/logout?session=" + sessionId, "charset=UTF-8");
        } catch (Exception e) {
            System.out.println("Error on logout: " + e);
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

    private void assertFieldValueEquals(String value, Element instanceEl, String fieldName) {
        Element fieldEl = (Element) instanceEl.selectSingleNode("field[@name='" + fieldName + "']");
        assertNotNull(fieldEl);
        assertEquals(value, fieldEl.getText());
    }

    protected void exceptionFail(String message, Throwable e) {
        throw new AssertionError(message, e);
    }
}
