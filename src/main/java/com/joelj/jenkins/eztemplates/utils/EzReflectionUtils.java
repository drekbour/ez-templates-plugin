package com.joelj.jenkins.eztemplates.utils;


import java.lang.reflect.Field;

import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.makeAccessible;
import static org.springframework.util.ReflectionUtils.setField;

public class EzReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Class c, Object instance, String name) {
        Field f = findField(c, name);
        makeAccessible(f);
        return (T) getField(f, instance);
    }

    public static void setFieldValue(Class c, Object instance, String name, Object value) {
        Field f = findField(c, name);
        makeAccessible(f);
        setField(f, instance, value);
    }

    public static boolean isAssignable(String className, Class<?> type) {
        try {
            return Class.forName(className).isAssignableFrom(type);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
