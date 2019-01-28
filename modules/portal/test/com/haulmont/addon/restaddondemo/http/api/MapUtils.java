/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {
    static Map<String, String> asMap(String... keyValueKeyValue) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0, keyValueKeyValueLength = keyValueKeyValue.length; i < keyValueKeyValueLength; i += 2) {
            map.put(keyValueKeyValue[i], keyValueKeyValue[i + 1]);
        }
        return map;
    }
}