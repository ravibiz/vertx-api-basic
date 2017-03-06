package com.vk.vertxapi.util;

import java.util.Map;
import java.util.stream.Collectors;

public class JsonUtil {

    public static String getJson(Map<String, Object> params) {

        return "{" + params.entrySet().stream()
                .map(entry -> jsonValue(entry.getKey(), entry.getValue() instanceof String ? (String) entry.getValue() : getJson((Map<String, Object>) entry.getValue())))
                .collect(Collectors.joining(",")) + "}";
    }

    private static String jsonValue(String key, String value) {
        return "\"" + key + "\": " + "\"" + value + "\"";
    }

}
