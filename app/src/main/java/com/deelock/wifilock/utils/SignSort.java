package com.deelock.wifilock.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\13 0013.
 */

public class SignSort {

    public static String getSign(JSONObject obj) {
        String ret = null;
        if (obj == null || obj.isEmpty())
            return null;
        else {
            String[] array = sortStringArray(obj);
            if (array != null) {
                String key = "";
                for (int i = 0; i < array.length; i++) {
                    key += array[i];
                }
//                ret = MD5Util.string2MD5(key);
            }

        }
        return ret;
    }

    /**
     * 对json的value进行排序
     *
     * @param obj
     * @return
     */
    public static String[] sortStringArray(JSONObject obj) {
        String keys[] = new String[1];
        keys[0] = "";
        String[] array = null;
        if (obj == null || obj.isEmpty()) {
            array = new String[1];
            array[0] = "";
        } else {

            array = new String[obj.size()];
            int i = 0;
            LinkedHashMap<String, Object> jsonMap = JSON.parseObject(obj.toJSONString(),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                array[i++] = entry.getValue() + "";
            }
//            keys = sortStringArray(array);
        }
        return keys;
    }

    /**
     * 对字符串数组进行排序
     *
     * @param keys
     * @return
     */
    public static String sortStringArray(String[] keys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length - 1; i++) {
            for (int j = 0; j < keys.length - i - 1; j++) {
                String pre = keys[j];
                String next = keys[j + 1];
                if (isMoreThan(pre, next)) {
                    String temp = pre;
                    keys[j] = next;
                    keys[j + 1] = temp;
                }
            }
        }
        for (int i = 0; i < keys.length ; i++) {
            sb.append(keys[i]);
        }
        return sb.toString();
    }

    /**
     * 比较两个字符串的大小，按字母的ASCII码比较
     *
     * @param pre
     * @param next
     * @return
     */
    private static boolean isMoreThan(String pre, String next) {
        if (null == pre || null == next || "".equals(pre) || "".equals(next)) {
//            System.out.println("字符串比较数据不能为空！");
            return false;
        }
        char[] c_pre = pre.toCharArray();
        char[] c_next = next.toCharArray();
        int minSize = Math.min(c_pre.length, c_next.length);
        for (int i = 0; i < minSize; i++) {
            if ((int) c_pre[i] > (int) c_next[i]) {
                return true;
            } else if ((int) c_pre[i] < (int) c_next[i]) {
                return false;
            }
        }
        if (c_pre.length > c_next.length) {
            return true;
        }
        return false;
    }
}
