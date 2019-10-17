package com.deelock.wifilock.utils;

import android.text.TextUtils;
import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by binChuan on 2017\9\12 0012.
 */

public class AES7P256 {

    static {
        System.loadLibrary("encrypt");
    }

    public static native String getEncrypt();

    public static String encrypt(String content, String aes_key, String aes_iv) {
        try {
            if (content == null)
                return "";
            SecretKeySpec key = new SecretKeySpec(getPassword(aes_key).getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getIvTmp(aes_iv).getBytes());
            // 将提供程序添加到下一个可用位置
            Security.addProvider(new BouncyCastleProvider());
            // 创建一个实现指定转换的 Cipher对象，该转换由指定的提供程序提供。
            // "AES/CBC/PKCS7Padding"：转换的名称；
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] byteContent = content.getBytes("utf-8");
            byte[] cryptograph = cipher.doFinal(byteContent);

            return Base64Util.encodeSafe(cryptograph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(byte[] content, String token) {
        try {
            if (content == null)
                return "";
            if (TextUtils.isEmpty(token)) {
                token = getEncrypt();
            }
            SecretKeySpec key = new SecretKeySpec(getPassword(token).getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getIvTmp(token.substring(1, 17)).getBytes());
            // 将提供程序添加到下一个可用位置
            Security.addProvider(new BouncyCastleProvider());
            // 创建一个实现指定转换的 Cipher对象，该转换由指定的提供程序提供。
            // "AES/CBC/PKCS7Padding"：转换的名称；
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cryptograph = cipher.doFinal(content);
            return Base64Util.encodeSafe(cryptograph);
        } catch (Exception e) {
        }
        return null;
    }

    public static String decrypt(String cryptograph, String aes_key, String aes_iv) {
        try {
            if (cryptograph == null)
                return null;
            SecretKeySpec key = new SecretKeySpec(getPassword(getEncrypt()).getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getIvTmp(getEncrypt().substring(1, 17)).getBytes());
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] content = cipher.doFinal(Base64Util.decodeSafe(cryptograph));
            return new String(content, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptBytes(String cryptograph, String token) {
        try {
            if (cryptograph == null)
                return null;
            if (TextUtils.isEmpty(token)) {
                token = getEncrypt();
            }
            SecretKeySpec key = new SecretKeySpec(getPassword(token).getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getIvTmp(token.substring(1, 17)).getBytes());
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] content = cipher.doFinal(Base64Util.decodeSafe(cryptograph));
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String cryptograph) {
        try {
            if (cryptograph == null)
                return null;
            SecretKeySpec key = new SecretKeySpec(getPassword(getEncrypt()).getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(getIvTmp(getEncrypt().substring(1, 17)).getBytes());
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] content = cipher.doFinal(Base64Util.decodeSafe(cryptograph));
            return new String(content, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获得补位的字符
    public static String getPosition(String content) {
        if (content.length() < 16) {
            return String.valueOf(16 - content.length());
        } else {
            return String.valueOf(16 - content.length() % 16);
        }
    }

    // 秘钥补位
    public static String getPassword(String password) {
        int k = 32 - password.length();
        String position = getPosition(password);
        for (int i = 0; i < k; i++) {
            password += position;
        }
        return password;
    }

    // 向量补位
    public static String getIvTmp(String ivTmp) {
        int k = 16 - ivTmp.length();
        String position = getPosition(ivTmp);
        for (int i = 0; i < k; i++) {
            ivTmp += position;
        }
        return ivTmp;
    }

    // +/= 替换 为-_.
    public static String restoreString(String str) {
        char[] chrCharArray;
        chrCharArray = str.toCharArray();
        for (int i = 0; i < chrCharArray.length; i++) {
            char k = chrCharArray[i];
            if (k == '+') {
                chrCharArray[i] = '-';
            } else if (k == '=') {
                chrCharArray[i] = '.';
            } else if (k == '/') {
                chrCharArray[i] = '_';
            }
        }
        return String.valueOf(chrCharArray);
    }

    // +/= 替换 为-_.
    public static byte[] restoreByte(String str) {
        byte[] chrCharArray;
        chrCharArray = str.getBytes();
        for (int i = 0; i < chrCharArray.length; i++) {
            byte k = chrCharArray[i];
            if (k == '+') {
                chrCharArray[i] = '-';
            } else if (k == '=') {
                chrCharArray[i] = '.';
            } else if (k == '/') {
                chrCharArray[i] = '_';
            }
        }
        return chrCharArray;
    }

    // -_. 替换 为+/=
    public static String storeString(String str) {
        char[] chrCharArray;
        chrCharArray = str.toCharArray();
        for (int i = 0; i < chrCharArray.length; i++) {
            char k = chrCharArray[i];
            if (k == '-') {
                chrCharArray[i] = '+';
            } else if (k == '_') {
                chrCharArray[i] = '/';
            } else if (k == '.') {
                chrCharArray[i] = '=';
            }
        }
        return String.valueOf(chrCharArray);
    }

    // -_. 替换 为+/=
    public static byte[] storeByte(String str) {
        byte[] chrCharArray;
        chrCharArray = str.getBytes();
        for (int i = 0; i < chrCharArray.length; i++) {
            byte k = chrCharArray[i];
            if (k == '-') {
                chrCharArray[i] = '+';
            } else if (k == '_') {
                chrCharArray[i] = '/';
            } else if (k == '.') {
                chrCharArray[i] = '=';
            }
        }
        return chrCharArray;
    }

    public static String getData(String s, String token) {
        if (TextUtils.isEmpty(token)) {
            token = getEncrypt();
        }
        byte[] b = null;
        try {
            b = ZLibUtils.decompress(decryptBytes(storeString(s), token));
        } catch (Exception e) {
            b = ZLibUtils.decompress(decryptBytes(storeString(s), null));
        }

        String str = null;
        try {
            str = new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
