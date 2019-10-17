package com.deelock.wifilock.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonUtil {
    private static Gson gson = new GsonBuilder().create();
    private static JsonParser parser = new JsonParser();

    public static <T> T json2Bean(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }

    public static String object2String(Object object) {
        return gson.toJson(object);
    }

    public static String getValueByKey(String key, String json) {
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        return jsonObject.get(key).getAsString();
    }
}
