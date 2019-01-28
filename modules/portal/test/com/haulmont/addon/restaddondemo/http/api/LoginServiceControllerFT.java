/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import com.meterware.httpunit.*;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class LoginServiceControllerFT {
    private static final String URI_BASE = "http://localhost:8080/";
    private WebConversation conv;

    @Before
    public void setUp() throws Exception {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);

        conv = new WebConversation();
    }

    @After
    public void tearDown() throws SQLException {
    }

    @Test
    public void login_JSON() throws Exception {
        JSONObject loginJSON = new JSONObject();
        loginJSON.put("username", "admin");
        loginJSON.put("password", "admin");
        loginJSON.put("locale", "ru");

        WebResponse response = POST("/app-portal/api/login",
                loginJSON.toString(), "application/json;charset=UTF-8");
        String sessionId = response.getText();
        assertNotNull(sessionId);
    }

    @Test
    public void login_FORM() throws Exception {
        Map<String, String> loginForm = new HashMap<>();
        loginForm.put("username", "admin");
        loginForm.put("password", "admin");
        loginForm.put("locale", "ru");

        String content = "";
        for (Map.Entry<String, String> entry : loginForm.entrySet()) {
            content += URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()) +"&";
        }

        WebResponse response = POST("/app-portal/api/login",
                content, "application/x-www-form-urlencoded;charset=UTF-8");
        String sessionId = response.getText();
        assertNotNull(sessionId);
    }

    @Test
    public void login_GET() throws Exception {
        String content = "?u=admin&p=" + "admin" + "&l=ru";
        WebResponse response = GET("/app-portal/api/login" + content);
        String sessionId = response.getText();
        assertNotNull(sessionId);
    }

    private WebResponse POST(String uri, String s, String contentType) throws IOException, SAXException {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        return conv.sendRequest(new PostMethodWebRequest(URI_BASE + uri, is, contentType));
    }

    private WebResponse GET(String uri) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(URI_BASE + uri);
        return conv.sendRequest(request);
    }
}
