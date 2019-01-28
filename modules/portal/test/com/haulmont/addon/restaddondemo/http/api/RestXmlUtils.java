/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.addon.restaddondemo.http.api;

import org.dom4j.Element;

import java.util.*;

/**
 * Utility class for working with xml documents used in REST API
 *
 */
public class RestXmlUtils {

    public static Element referenceElement(Element instanceEl, String propertyName) {
        return (Element) instanceEl.selectSingleNode("reference[@name = '" + propertyName + "']");
    }

    public static Element referenceInstanceElement(Element instanceEl, String propertyName) {
        return (Element) instanceEl.selectSingleNode("reference[@name = '" + propertyName + "']/instance");
    }

    public static Element fieldElement(Element instanceEl, String propertyName) {
        return (Element) instanceEl.selectSingleNode("field[@name = '" + propertyName + "']");
    }

    public static String fieldValue(Element instanceEl, String propertyName) {
        return instanceEl.selectSingleNode("field[@name = '" + propertyName + "']").getText();
    }

    public static List<Element> collectionInstanceElements(Element instance, String propertyName) {
        List<Element> result = new ArrayList<>();
        List nodes = instance.selectNodes("collection[@name = '" + propertyName + "']/instance");
        for (Object node : nodes) {
            result.add((Element) node);
        }
        return result;
    }

    public static String idAttr(Element element) {
        return element.attributeValue("id");
    }
}
