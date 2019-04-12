package com.haulmont.rest.demo;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentTest extends RestUtils {

    @Test
    public void getToken() throws Exception {
        String token = getAuthToken();
        assertNotNull(token);
    }

    @Test
    public void unavailableAttributesMustBeHiddenInQueryResult() throws Exception {
        String url = "/entities/sec$User";

        try (CloseableHttpResponse response = sendGet(url, getAuthToken(), null)) {
            assertEquals(org.apache.http.HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertEquals(2, (int) ctx.read("$.length()", Integer.class));
        }
    }
}
