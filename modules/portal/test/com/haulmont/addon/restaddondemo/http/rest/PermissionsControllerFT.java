/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.rest;

import com.haulmont.cuba.core.sys.encryption.BCryptEncryptionModule;
import com.haulmont.cuba.core.sys.encryption.EncryptionModule;
import com.haulmont.cuba.core.sys.persistence.PostgresUUID;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.addon.restaddondemo.http.api.DataSet;
import com.jayway.jsonpath.ReadContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static com.haulmont.addon.restaddondemo.http.rest.RestTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PermissionsControllerFT {

    private static final String DB_URL = "jdbc:postgresql://localhost/refapp_6";

    private Connection conn;
    private DataSet dirtyData = new DataSet();
    private String oauthToken;
    private UUID roleId;

    private static EncryptionModule encryption = new BCryptEncryptionModule();
    private UUID groupUuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93");
    private String testUserLogin = "testUser";
    private String testUserPassword = "test";


    @Before
    public void setUp() throws Exception {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(DB_URL, "root", "root");
        prepareDb();
        oauthToken = getAuthToken(testUserLogin, testUserPassword);
    }

    @After
    public void tearDown() throws SQLException {
        dirtyData.cleanup(conn);

        if (conn != null)
            conn.close();
    }

    @Test
    public void getPermissions() throws Exception {
        String url = "/permissions";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.<Collection>read("$").size() > 0);
            assertEquals("MODIFY", ctx.<Collection>read("$[?(@.target == 'ref$Currency.name')].value").iterator().next());
            assertEquals("DENY", ctx.<Collection>read("$[?(@.target == 'ref_Car:update')].value").iterator().next());
        }
    }

    private void prepareDb() throws SQLException {

        UUID testUserId = dirtyData.createUserUuid();
        String pwd = encryption.getPasswordHash(testUserId, testUserPassword);
        executePrepared("insert into sec_user(id, version, login, password, password_encryption, group_id, login_lc) " +
                        "values(?, ?, ?, ?, ?, ?, ?)",
                new PostgresUUID(testUserId),
                1l,
                testUserLogin,
                pwd,
                encryption.getHashMethod(),
                new PostgresUUID(groupUuid), //"Company" group
                testUserLogin.toLowerCase()
        );


        roleId = dirtyData.createRoleUuid();
        executePrepared("insert into sec_role(id, role_type, name) values(?, ?, ?)",
                new PostgresUUID(roleId),
                RoleType.READONLY.getId(),
                "testRole"
        );

        int ALLOW = 1;
        int DENY = 0;
        int PROPERTY_MODIFY = 2;

        //testRole forbids to update cars
        UUID cantUpdateCarPrmsId = dirtyData.createPermissionUuid();
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(cantUpdateCarPrmsId),
                new PostgresUUID(roleId),
                PermissionType.ENTITY_OP.getId(),
                "ref_Car:update",
                DENY
        );

        //testRole forbids currencies browser screen
        UUID currenciesScreenPrmsId = dirtyData.createPermissionUuid();
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(currenciesScreenPrmsId),
                new PostgresUUID(roleId),
                PermissionType.SCREEN.getId(),
                "ref$Currency.browse",
                DENY
        );

        //testRole forbids currencies browser screen
        UUID entityAttrModifyAllowPrmsId = dirtyData.createPermissionUuid();
        executePrepared("insert into sec_permission(id, role_id, permission_type, target, value_) values(?, ?, ?, ?, ?)",
                new PostgresUUID(entityAttrModifyAllowPrmsId),
                new PostgresUUID(roleId),
                PermissionType.ENTITY_ATTR.getId(),
                "ref$Currency.name",
                PROPERTY_MODIFY
        );

        UUID id = UUID.randomUUID();
        //colorReadUser has colorReadRole role (read-only)
        executePrepared("insert into sec_user_role(id, user_id, role_id) values(?, ?, ?)",
                new PostgresUUID(id),
                new PostgresUUID(testUserId),
                new PostgresUUID(roleId)
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

}
