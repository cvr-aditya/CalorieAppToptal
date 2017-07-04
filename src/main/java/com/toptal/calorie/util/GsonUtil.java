package com.toptal.calorie.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by asirna on 23/06/2017.
 */
public class GsonUtil {

    private static Gson gson;

    public static String toJson(Object object) {
        return getGson().toJson(object);
    }

    public static JSONObject toJsonObject(Object object) throws JSONException {
        return new JSONObject(toJson(object));
    }

    public static Object fromJson(JSONObject object, Class<?> className)
            throws ClassNotFoundException {
        return fromJson(object.toString(), className);
    }

    public static Object fromJson(String jsonString, Class<?> className)
            throws ClassNotFoundException {
        return getGson().fromJson(jsonString, className);
    }

    public static Gson getGson() {
        if (gson == null)
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson;
    }

    /**
     * Serialize nulls
     *
     * @param object
     * @return
     */
    public static String serializedNullsToJson(Object object) {
        return new GsonBuilder().serializeNulls().create().toJson(object);
    }
}
