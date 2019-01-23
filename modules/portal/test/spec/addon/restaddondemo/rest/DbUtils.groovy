/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package spec.addon.restaddondemo.rest

import groovy.sql.Sql

class DbUtils {
    static Sql getSql() {
        return Sql.newInstance('jdbc:postgresql://localhost/refapp_6',
                'root', 'root', 'org.postgresql.Driver')
    }
}
