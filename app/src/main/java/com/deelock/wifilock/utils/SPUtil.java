package com.deelock.wifilock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.deelock.wifilock.entity.HelpInfo;
import com.deelock.wifilock.entity.Login;
import com.deelock.wifilock.entity.Pid;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binChuan on 2017\9\14 0014.
 */

public class SPUtil {

    private static final String WIFI_LOCK = "deelock";

    private static final String PID = "pid";
    private static final String UID = "uid";
    private static final String TOKEN = "token";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String NICK_NAME = "nickName";
    private static final String HEAD_ICON = "headIcon";
    private static final String SEX = "sex";
    private static final String ACCOUNT_MARK = "accountMark";
    private static final String DTFT = "dtft";
    private static final String FIRST_TIME_EQUIPMENT_PAGE = "equipment";
    private static final String FIRST_TIME_HISTORY_PAGE = "history";
    private static final String FIRST_TIME_SECURITY_PAGE = "security";
    private static final String GESTURE = "gesture";
    private static final String DAILY_CHECK_DATE = "dailyCheckDate";
    private static final String AD_CHECK_DATE = "adCheckDate";
    private static final String HEAD_URL = "headUrl";
    private static final String HELP_NUMBER = "helpNumber";
    private static final String HELP_NAME = "helpName";
    private static final String NOTICE_NAME = "noticeName";
    private static final String AD_URI = "adUri";
    private static final String IS_SHOW_NOTIFY = "isShowNotify";
    public static final String LOCK_STATE = "lockState";
    public static final String GATE_DEVICE_ID = "deviceId";


    public static void saveData(final Context context, String key, Object value) {
        if (key == null) {
            return;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        }
        editor.apply();
    }

    public static String getStringData(final Context context, String key) {
        if (context == null) {
            return null;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static boolean getBooleanData(final Context context, String key) {
        if (context == null) {
            return false;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    public static int getIntData(final Context context, String key) {
        if (context == null) {
            return -1;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getInt(key, -1);
    }

    /**
     * 保存登录信息
     *
     * @param context
     * @param login
     */
    public static void setLoginInfo(final Context context, Login login) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(UID, login.getPid().replace(".0", ""));
        editor.putString(TOKEN, login.getToken());
        editor.putString(PHONE_NUMBER, login.getPhoneNumber());
        editor.putString(NICK_NAME, login.getNickName());
        editor.putInt(HEAD_ICON, login.getHeadIcon());
        editor.putInt(SEX, login.getSex());
        editor.putString(HEAD_URL, login.getHeadUrl());
        editor.putString(ACCOUNT_MARK, login.getAccountMark());
        editor.apply();
    }

    public static void logout(final Context context) {
        Login login = new Login();
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(UID, "");
        editor.putString(TOKEN, "");
        editor.putString(NICK_NAME, "");
        editor.putInt(HEAD_ICON, 0);
        editor.putInt(SEX, 0);
        editor.putString(ACCOUNT_MARK, "");
        editor.apply();
    }

    public static void setUid(final Context context, Pid pid) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(UID, pid.getPid());
        editor.apply();
    }

    public static String getUid(final Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(UID, "");
    }

    public static void setIsShowNotify(final Context context, boolean isShow) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(IS_SHOW_NOTIFY, isShow);
        editor.apply();
    }

    public static boolean getIsShowNotify(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getBoolean(IS_SHOW_NOTIFY, false);
    }

    public static String getToken(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(TOKEN, "");
    }

    public static String getKey(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(ACCOUNT_MARK, "");
    }

    public static String getPhoneNumber(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(PHONE_NUMBER, "");
    }

    public static String getNickName(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(NICK_NAME, "");
    }

    public static String getHeadIcon(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(HEAD_ICON, "");
    }

    public static String getSex(final Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(SEX, "");
    }

    public static void setNickName(final Context context, String nickName) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(NICK_NAME, nickName);
        editor.apply();
    }

    public static void setAdUri(Context context, String uri) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(AD_URI, uri);
        editor.apply();
    }

    public static String getAdUri(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(AD_URI, "");
    }


    /**
     * 保存列表
     *
     * @param context
     * @param tag     用类名标识
     * @param list
     * @param <T>
     */
    public static <T> void setList(Context context, String tag, List<T> list) {
        if (context == null) {
            return;
        }
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //转换成json数据，再保存
        String strJson = "";
        if (null != list && list.size() > 0) {
            strJson = new Gson().toJson(list);
        }
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取列表
     *
     * @param context
     * @param tag
     * @return
     */
    public static String getListContent(Context context, String tag) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(tag, null);
    }

    /**
     * 获取列表
     *
     * @param context
     * @param tag
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(Context context, String tag, Class<T> cls) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                WIFI_LOCK, Context.MODE_PRIVATE);
        List<T> list = new ArrayList<>();
        String strJson = sp.getString(tag, null);
        if (TextUtils.isEmpty(strJson)) {
            return list;
        }
        try {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                list.add(gson.fromJson(jsonElement, cls));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("list", strJson);
        }
        return list;
    }

    /**
     * 保存实体类
     *
     * @param context
     * @param tag
     * @param entity
     */
    public static <T> void setEntity(Context context, String tag, T entity) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //转换成json数据，再保存
        String strJson = new Gson().toJson(entity);
        editor.putString(tag, strJson);
        editor.apply();
    }

    /**
     * 获取实体类
     *
     * @param context
     * @param tag
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getEntity(Context context, String tag, Class<T> cls) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        String strJson = sp.getString(tag, null);
        return new Gson().fromJson(strJson, cls);
    }

//    public static void setDtft(final Context context, long dtft) {
//        SharedPreferences sp = context.getSharedPreferences(
//                WIFI_LOCK, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putLong(DTFT, dtft);
//        editor.commit();
//    }
//
//    public static long getDtft(final Context context) {
//        SharedPreferences sp = context.getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
//        return sp.getLong(DTFT, -1);
//    }

    public static boolean isFirstTimeIntoEquipmentPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getBoolean(FIRST_TIME_EQUIPMENT_PAGE, true);
    }

    public static void setFirstTimeIntoEquipmentPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FIRST_TIME_EQUIPMENT_PAGE, false);
        editor.apply();
    }

    public static boolean isFirstTimeIntoHistoryPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getBoolean(FIRST_TIME_HISTORY_PAGE, true);
    }

    public static void setFirstTimeIntoHistoryPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FIRST_TIME_HISTORY_PAGE, false);
        editor.apply();
    }

    public static boolean isFirstTimeIntoSecurityPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getBoolean(FIRST_TIME_SECURITY_PAGE, true);
    }

    public static void setFirstTimeIntoSecurityPage(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(FIRST_TIME_SECURITY_PAGE, false);
        editor.apply();
    }

    public static void setSdlName(Context context, String sdlId, String nickname) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(sdlId, nickname);
        editor.apply();
    }

    public static String getSdlName(Context context, String sdlId) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        String string = sp.getString(sdlId, "");
        return string;
    }


    public static void setGesture(Context context, int[] gesture) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < gesture.length; i++) {
            builder.append(gesture[i]);
        }
        editor.putString(GESTURE, builder.toString());
        editor.apply();
    }

    public static int[] getGesture(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        String g = sp.getString(GESTURE, "");
        if (g.length() == 0) {
            return null;
        }
        int gesture[] = new int[g.length()];
        for (int i = 0; i < gesture.length; i++) {
            gesture[i] = Integer.parseInt(g.substring(i, i + 1));
        }
        return gesture;
    }

    public static boolean getDailyCheck(Context context, String date) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        String d = sp.getString(DAILY_CHECK_DATE, "");
        if (!date.equals(d)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(DAILY_CHECK_DATE, date);
            editor.apply();
            return true;
        }
        return false;
    }

    public static boolean getAdCheck(Context context, String date) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        String d = sp.getString(AD_CHECK_DATE, "");
        if (!date.equals(d)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(AD_CHECK_DATE, date);
            editor.apply();
            return true;
        }
        return false;
    }

    public static void setHeadUrl(Context context, String url) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(HEAD_URL, url);
        editor.apply();
    }

    public static String getHeadUrl(Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        return sp.getString(HEAD_URL, "");
    }

    public static void setHelpInfo(Context context, HelpInfo info) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(HELP_NUMBER, info.getHelpNumber());
        editor.putString(HELP_NAME, info.getHelpName());
        editor.putString(NOTICE_NAME, info.getNoticeName());
        editor.apply();
    }

    public static HelpInfo getHelpInfo(Context context) {
        HelpInfo info = new HelpInfo();
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(WIFI_LOCK, Context.MODE_PRIVATE);
        info.setHelpNumber(sp.getString(HELP_NUMBER, ""));
        info.setHelpName(sp.getString(HELP_NAME, ""));
        info.setNoticeName(sp.getString(NOTICE_NAME, ""));
        return info;
    }
}
