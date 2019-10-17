package com.deelock.wifilock.utils;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class InputUtil {


    public static boolean checkLogin(Context context, String number, String password) {
        if (checkPhoneNumber(context, number) && checkPassword(context, password)){
            return true;
        }
        return false;
    }

    /**
     *
     * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
     * 联通：130、131、132、152、155、156、176、185、186
     * 电信：133、153、173、180、189、（1349卫通）
     * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
     *
     * @param number
     * @return
     */
    public static boolean checkPhoneNumber(Context context, String number){
        if (TextUtils.isEmpty(number)){
            ToastUtil.toastShort(context, "请输入手机号");
            return false;
        } else if (number.length() < 11) {
            ToastUtil.toastShort(context, "请输入完整手机号");
            return false;
        }

        String num = "[1]\\d{10}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (!number.matches(num)){
            ToastUtil.toastShort(context, "手机号格式有误");
            return false;
        }
        return true;
    }

    /**
     *
     * 判断密码符合要求
     * @param password
     * @return
     */
    public static boolean checkPassword(Context context, String password){
        if (TextUtils.isEmpty(password)){
            ToastUtil.toastShort(context, "请输入密码");
            return false;
        } else if (password.length() < 6){
            ToastUtil.toastShort(context, "请输入6~12位密码");
            return false;
        }
        return true;
    }

    public static boolean checkForget(Context context, String number, String msg, String password){
        if (checkPhoneNumber(context, number) && checkMessage(context, msg) && checkPassword(context, password)){
            return true;
        }
        return false;
    }

    public static boolean checkMessage(Context context, String msg){
        if (TextUtils.isEmpty(msg)){
            ToastUtil.toastShort(context, "请输入验证码");
            return false;
        } else if (msg.length() < 6){
            ToastUtil.toastShort(context, "请输入6~8位验证码");
            return false;
        }
        return true;
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
