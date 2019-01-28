/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.rest;

import com.haulmont.addon.restaddondemo.http.api.DataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.haulmont.addon.restaddondemo.http.rest.RestTestUtils.getAuthToken;

@Ignore
public abstract class AbstractRestControllerFT {

    protected static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";

    protected Connection conn;
    protected DataSet dirtyData = new DataSet();
    protected String oauthToken;

    @Before
    public void setUp() throws Exception {
        oauthToken = getAuthToken("admin", "admin");
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(DB_URL, "cuba", "cuba");
        prepareDb();
    }

    @After
    public void tearDown() throws Exception {
        dirtyData.cleanup(conn);
        if (conn != null) {
            conn.close();
        }
    }

    public void prepareDb() throws Exception {}
}
