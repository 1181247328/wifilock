package com.deelock.wifilock.utils;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by binChuan on 2017\9\12 0012.
 */

public class Base64Util {
    private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();

    /**
     * @param data
     * @param charsetName
     *            If charsetName is null,the default charsetName is "utf-8"
     * @return
     */
    public static String encodeCommon(String data, String charsetName) {
        String charset = charsetName;
        String ret = null;
        try {
            if (charset == null)
                charset = "utf-8";
            ret = Base64.encodeToString(data.getBytes("utf-8"), Base64.NO_WRAP);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return ret;
    }

    /**
     *
     * @param data
     * @return
     */
    public static String encodeCommon(byte[] data) {
        int start = 0;
        int len = data.length;
        StringBuffer buf = new StringBuffer(data.length * 3 / 2);

        int end = len - 3;
        int i = start;
        int n = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 0x0ff) << 8)
                    | (((int) data[i + 2]) & 0x0ff);

            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append(legalChars[(d >> 6) & 63]);
            buf.append(legalChars[d & 63]);

            i += 3;

            if (n++ >= 14) {
                n = 0;
                buf.append(" ");
            }
        }

        if (i == start + len - 2) {
            int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 255) << 8);

            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append(legalChars[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == start + len - 1) {
            int d = (((int) data[i]) & 0x0ff) << 16;

            buf.append(legalChars[(d >> 18) & 63]);
            buf.append(legalChars[(d >> 12) & 63]);
            buf.append("==");
        }

        return buf.toString();
    }

    /**
     * @param data
     * @param charsetName
     *            charsetName If charsetName is null,the default charsetName is
     *            "utf-8"
     * @return
     */
    public static String decodeCommon(String data, String charsetName) {
        String charset = charsetName;
        byte[] retArray = null;
        String ret = null;
        try {
            if (charset == null)
                charset = "utf-8";
            retArray = Base64.decode(data, Base64.NO_WRAP);
            if (retArray != null)
                ret = new String(retArray, charset);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Decodes the given Base64 encoded String to a new byte array. The byte array
     * holding the decoded data is returned.
     */

    public static byte[] decodeCommon(String data) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            decode(data, bos);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        byte[] decodedBytes = bos.toByteArray();
        try {
            bos.close();
            bos = null;
        } catch (IOException ex) {
            System.err.println("Error while decoding BASE64: " + ex.toString());
        }
        return decodedBytes;
    }

    /**
     *
     * @param s
     * @param os
     * @throws IOException
     */
    private static void decode(String s, OutputStream os) throws IOException {
        int i = 0;

        int len = s.length();

        while (true) {
            while (i < len && s.charAt(i) <= ' ')
                i++;

            if (i == len)
                break;

            int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6)
                    + (decode(s.charAt(i + 3)));

            os.write((tri >> 16) & 255);
            if (s.charAt(i + 2) == '=')
                break;
            os.write((tri >> 8) & 255);
            if (s.charAt(i + 3) == '=')
                break;
            os.write(tri & 255);

            i += 4;
        }
    }

    /**
     *
     * @param c
     * @return
     */
    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z')
            return ((int) c) - 65;
        else if (c >= 'a' && c <= 'z')
            return ((int) c) - 97 + 26;
        else if (c >= '0' && c <= '9')
            return ((int) c) - 48 + 26 + 26;
        else
            switch (c) {
                case '+':
                    return 62;
                case '/':
                    return 63;
                case '=':
                    return 0;
                default:
                    throw new RuntimeException("unexpected code: " + c);
            }
    }

    /**
     * @param data
     * @param charsetName
     *            If charsetName is null,the default charsetName is "utf-8"
     * @return
     */
    public static String encodeSafe(String data, String charsetName) {
        String charset = charsetName;
        String ret = null;
        try {
            if (data == null)
                return ret;
            if (charset == null)
                charset = "utf-8";
            ret = Base64.encodeToString(data.getBytes("utf-8"), Base64.NO_WRAP);
            ret = ret.replace("+", "-");
            ret = ret.replace("/", "_");
            ret = ret.replace("=", ".");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param data
     * @param charsetName
     *            charsetName If charsetName is null,the default charsetName is
     *            "utf-8"
     * @return
     */
    public static String decodeSafe(String data, String charsetName) {
        String charset = charsetName;
        byte[] retArray = null;
        String ret = null;
        try {
            if (data == null)
                return ret;
            if (charset == null)
                charset = "utf-8";
            data = data.replace("-", "+");
            data = data.replace("_", "/");
            data = data.replace(".", "=");
            retArray = Base64.decode(data, Base64.NO_WRAP);
            if (retArray != null)
                ret = new String(retArray, charset);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param data
     *            If charsetName is null,the default charsetName is "utf-8"
     * @return
     */
    public static String encodeSafe(byte[] data) {
        String ret = null;
        try {
            if (data == null)
                return ret;
            ret = Base64.encodeToString(data, Base64.NO_WRAP);
            ret = ret.replace("+", "-");
            ret = ret.replace("/", "_");
            ret = ret.replace("=", ".");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param data
     *            charsetName If charsetName is null,the default charsetName is
     *            "utf-8"
     * @return
     */
    public static byte[] decodeSafe(String data) {
        byte[] retArray = null;
        try {
            if (data == null)
                return retArray;
            data = data.replace("-", "+");
            data = data.replace("_", "/");
            data = data.replace(".", "=");
            retArray = Base64.decode(data, Base64.NO_WRAP);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return retArray;
    }
}
