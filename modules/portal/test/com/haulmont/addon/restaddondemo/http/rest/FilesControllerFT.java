/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.rest;

import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static com.haulmont.addon.restaddondemo.http.rest.RestTestUtils.parseResponse;
import static com.haulmont.addon.restaddondemo.http.rest.RestTestUtils.statusCode;
import static org.junit.Assert.*;

/**
 */
public class FilesControllerFT extends AbstractRestControllerFT {

    public String URI_BASE = "http://localhost:8080/app/rest/v2";

    @Test
    public void uploadFile() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");

        String fileName = "fileToUpload.txt";
        uriBuilder.addParameter("name", fileName);
        URL fileUrl = FilesControllerFT.class.getResource("data/" + fileName);

        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Bearer " + oauthToken);
        httpPost.setHeader("Content-Type", "text/plain");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
            ReadContext ctx = RestTestUtils.parseResponse(response);

            assertEquals(fileName, ctx.read("$.name"));
            String fileDescriptorId = ctx.read("$.id");
            assertEquals(0, (int) ctx.read("$.size"));
            assertNotNull(fileDescriptorId);

            Header location = response.getFirstHeader("Location");
            assertEquals(URI_BASE + "/files/" + fileDescriptorId, location.getValue());

            try (PreparedStatement stmt = conn.prepareStatement("select NAME, EXT, FILE_SIZE from SYS_FILE where ID = ?")) {
                stmt.setObject(1, new PostgresUUID(UUID.fromString(fileDescriptorId)));
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                assertEquals(fileName, rs.getString("NAME"));
                assertEquals("txt", rs.getString("EXT"));
                assertEquals(0, rs.getLong("FILE_SIZE"));
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void uploadFileWithoutName() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");

        String fileName = "fileToUpload.txt";
        URL fileUrl = FilesControllerFT.class.getResource("data/" + fileName);

        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Bearer " + oauthToken);
        httpPost.setHeader("Content-Type", "text/plain");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = RestTestUtils.parseResponse(response);

            String fileDescriptorId = ctx.read("$.id");
            assertNotNull(fileDescriptorId);
            assertEquals(fileDescriptorId, ctx.read("$.name"));
            assertEquals(0, (int) ctx.read("$.size"));

            Header location = response.getFirstHeader("Location");
            assertEquals(URI_BASE + "/files/" + fileDescriptorId, location.getValue());

            try (PreparedStatement stmt = conn.prepareStatement("select NAME, EXT, FILE_SIZE from SYS_FILE where ID = ?")) {
                stmt.setObject(1, new PostgresUUID(UUID.fromString(fileDescriptorId)));
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                assertEquals(fileDescriptorId, rs.getString("NAME"));
                assertEquals("", rs.getString("EXT"));
                assertEquals(0, rs.getLong("FILE_SIZE"));
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void downloadFile() throws Exception {
        String fileId = _uploadFile("test-file.pdf");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URI_BASE + "/files/" + fileId);
        httpGet.setHeader("Authorization", "Bearer " + oauthToken);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals("application/pdf", response.getFirstHeader("Content-Type").getValue());
            assertEquals("no-cache", response.getFirstHeader("Cache-Control").getValue());
            assertEquals("inline; filename=\"test-file.pdf\"", response.getFirstHeader("Content-Disposition").getValue());
//            assertEquals("123", response.getFirstHeader("Content-Length").getValue());
            byte[] fileContent = EntityUtils.toByteArray(response.getEntity());
            assertTrue(fileContent.length > 0);
//            assertEquals("Test data", fileContent);
        }
    }

    @Test
    public void downloadMissingFile() throws Exception {
        String fileId = UUID.randomUUID().toString();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URI_BASE + "/files/" + fileId);
        httpGet.setHeader("Authorization", "Bearer " + oauthToken);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
            ReadContext ctx = RestTestUtils.parseResponse(response);
            assertEquals("File not found", ctx.read("$.error"));
        }
    }

    @Test
    public void downloadFileWithInvalidId() throws Exception {
        String invalidId = "nonUuidValue";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(URI_BASE + "/files/" + invalidId);
        httpGet.setHeader("Authorization", "Bearer " + oauthToken);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals("Invalid entity ID", ctx.read("$.error"));
            assertEquals(String.format("Cannot convert %s into valid entity ID", invalidId), ctx.read("$.details"));
        }
    }

    protected String _uploadFile(String fileName) throws URISyntaxException, IOException {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");
        uriBuilder.addParameter("name", fileName);

        URL fileUrl = FilesControllerFT.class.getResource("data/" + fileName);
        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(new File(fileUrl.toURI())));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Bearer " + oauthToken);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            ReadContext ctx = RestTestUtils.parseResponse(response);
            return ctx.read("$.id");
        }
    }

    @Test
    public void uploadFileMultipart() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(URI_BASE + "/files");

        String fileName = "fileToUpload.txt";
        URL fileUrl = FilesControllerFT.class.getResource("data/" + fileName);
        FileBody fileBody = new FileBody(new File(fileUrl.toURI()));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        HttpEntity entity = builder.build();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(entity);
        httpPost.setHeader("Authorization", "Bearer " + oauthToken);
//        httpPost.setHeader("Content-Type", "multipart/form-data");
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
            ReadContext ctx = RestTestUtils.parseResponse(response);

            String fileDescriptorId = ctx.read("$.id");
            assertNotNull(fileDescriptorId);
            assertEquals(fileName, ctx.read("$.name"));
            assertTrue(ctx.read("$.size", Integer.class) > 0);

            try (PreparedStatement stmt = conn.prepareStatement("select NAME, EXT, FILE_SIZE from SYS_FILE where ID = ?")) {
                stmt.setObject(1, new PostgresUUID(UUID.fromString(fileDescriptorId)));
                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next());
                assertEquals(fileName, rs.getString("NAME"));
                assertEquals("txt", rs.getString("EXT"));
                assertEquals(9, rs.getLong("FILE_SIZE"));
                assertFalse(rs.next());
            }
        }
    }
}
