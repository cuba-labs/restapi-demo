package com.haulmont.rest.demo;

import com.haulmont.rest.demo.core.app.PortalTestService;
import org.junit.Test;

import static com.haulmont.rest.demo.RestUtils.*;
import static org.junit.Assert.assertNotNull;

public class DeploymentPortalTest {

    protected static final String URI_BASE_PORTAL = "http://localhost:8080/app-portal/rest/v2";

    @Test
    public void getTokenPortal() throws Exception {
        String oauthTokenPortal = getAuthToken(URI_BASE_PORTAL);
        assertNotNull(oauthTokenPortal);
    }

    @Test
    public void loadSomeListPortal() throws Exception {
        String url = URI_BASE_PORTAL + "/entities/sec$User";
        loadSomeList(url, getAuthToken(URI_BASE_PORTAL));
    }

    @Test
    public void executeQueryPortal() throws Exception {
        String url = URI_BASE_PORTAL + "/queries/sec$User/currentUser";
        executeQuery(url, getAuthToken(URI_BASE_PORTAL));
    }

    @Test
    public void executeServicePortal() throws Exception {
        String url = URI_BASE_PORTAL + "/services/" + PortalTestService.NAME + "/sum";
        executeService(url, getAuthToken(URI_BASE_PORTAL));
    }
}
