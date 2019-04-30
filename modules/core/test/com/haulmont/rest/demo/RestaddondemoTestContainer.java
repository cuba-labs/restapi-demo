package com.haulmont.rest.demo;

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.testsupport.TestContainer;
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;

public class RestaddondemoTestContainer extends TestContainer {

    public RestaddondemoTestContainer() {
        super();
        appComponents = new ArrayList<>(Arrays.asList(
                "com.haulmont.cuba",
                // add CUBA premium add-ons here
                "com.haulmont.bpm",
                // "com.haulmont.charts",
                // "com.haulmont.fts",
                "com.haulmont.reports",
                // and custom app components if any
                "com.haulmont.addon.restapi"
        ));
        appPropertiesFiles = Arrays.asList(
                // List the files defined in your web.xml
                // in appPropertiesConfig context parameter of the core module
                "com/haulmont/rest/demo/app.properties",
                // Add this file which is located in CUBA and defines some properties
                // specifically for test environment. You can replace it with your own
                // or add another one in the end.
                "com/haulmont/cuba/testsupport/test-app.properties");

        dbDriver = "org.hsqldb.jdbc.JDBCDriver";
        dbUrl = "jdbc:hsqldb:hsql://localhost/rest_demo";
        dbUser = "sa";
        dbPassword = "";

    }

    @Override
    protected void initDataSources() {
        super.initDataSources();
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

            TestDataSource ds1 = new TestDataSource("jdbc:hsqldb:mem:db1", "sa", "");
            TestContext.getInstance().bind(AppContext.getProperty("cuba.dataSourceJndiName_db1"), ds1);

        } catch (ClassNotFoundException | NamingException e) {
            throw new RuntimeException("Error initializing datasource", e);
        }
    }

    public static class Common extends RestaddondemoTestContainer {

        public static final RestaddondemoTestContainer.Common INSTANCE = new RestaddondemoTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
            }
            setupContext();
        }

        @Override
        public void after() {
            cleanupContext();
            // never stops - do not call super
        }
    }
}