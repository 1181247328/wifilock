package com.deelock.wifilock.utils;

import android.util.Log;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public class MD5Util {

    /**
     * MD5 32-bit md5
     */
    public static String string2MD5(Map params) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        Collection c = params.values();
        Iterator it = c.iterator();
        String[] array = new String[c.size()];
        int j = 0;
        while (it.hasNext()) {
            array[j] = String.valueOf(it.next());
        }
        String inStr = SignSort.sortStringArray(array);
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    /**
     * Encryption, decryption algorithm. Performed once encryption, decryption
     * twice
     */
    public static String convertMD5(String inStr) {

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;
    }
}
