/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import com.meterware.httpunit.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FileControllerFT {

    private static final String URI_BASE = "http://localhost:8080/";
    private static final String userLogin = "admin";
    private static final String userPassword = "admin";

    private String sessionId;
    private WebConversation conv;

    @Before
    public void setUp() throws Exception {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        HttpUnitOptions.setScriptingEnabled(false);
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);

        conv = new WebConversation();
        sessionId = login(userLogin, userPassword);
    }

    @After
    public void tearDown() throws Exception {
        logout();
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

    private WebResponse GET(String uri, String acceptedFormat) throws IOException, SAXException {
        GetMethodWebRequest request = new GetMethodWebRequest(URI_BASE + uri);
        request.setHeaderField("Accept", acceptedFormat);
        return conv.sendRequest(request);
    }

    private WebResponse POST(String uri, String s, String contentType) throws IOException, SAXException {
        ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
        return conv.sendRequest(new PostMethodWebRequest(URI_BASE + uri, is, contentType));
    }

    @Test
    public void testUploadDownload() throws Exception {
        String content = "some text content";
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());

        WebResponse response = conv.sendRequest(new PostMethodWebRequest(
                URI_BASE + "/app-portal/api/upload?" + "s=" + sessionId + "&name=test.txt&ext=txt&size=" + content.length(),
                is,
                "application/octet-stream"));

        String fileDescrId = response.getText();

        response = conv.sendRequest(new GetMethodWebRequest(
                URI_BASE + "/app-portal/api/download?" + "s=" + sessionId + "&f=" + fileDescrId));

        String text = response.getText();

        assertEquals(content, text);

        // check FileDescriptor
        response = GET("app-portal/api/find.json?e=sys$FileDescriptor-" + fileDescrId + "&s=" + sessionId,
                "charset=UTF-8");
        JSONObject fd = new JSONObject(response.getText());
        assertEquals(content.length(), fd.getLong("size"));
    }
}
