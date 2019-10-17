package com.deelock.wifilock.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.deelock.wifilock.entity.PushMessage;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            sendMessage(context, bundle);
            Logger.e("[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                Logger.e("JPushId : " + bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));
//                SPUtil.saveData(context, "Jpush_did", bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID));
            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                Logger.e("ACTION_MESSAGE_RECEIVED : " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                Logger.e("ACTION_NOTIFICATION_RECEIVED :" + bundle.getString(JPushInterface.EXTRA_NOTIFICATION_ID));
            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            } else {
            }
        } catch (Exception e) {

        }
    }

    /**
     * 设置消息回调
     *
     * @param bundle
     */
    private void sendMessage(final Context context, Bundle bundle) {
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();
                    PushMessage message = new PushMessage();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        if (myKey.equals("title")) {
                            message.setTitle(json.optString(myKey));
                        } else if (myKey.equals("deviceId")) {
                            message.setDeviceId(json.optString(myKey));
                        } else if (myKey.equals("code")) {
                            message.setCode(json.optInt(myKey));
                        } else if (myKey.equals("content")) {
                            JSONObject json1 = new JSONObject(json.optString(myKey));
                            Iterator<String> it1 = json1.keys();
                            while (it1.hasNext()) {
                                String k = it1.next();
                                if (k.equals("data")) {
                                    message.setData(json1.optString(k));
                                } else if (k.equals("timePush")) {
                                    message.setTimePush(json1.optLong(k));
                                }
                            }
                        }
                    }

                    Intent intent = new Intent("com.deelock.wifilock.LOCALBROADCAST");
                    intent.putExtra("push", message);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                } catch (JSONException e) {
                }
            }
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Logger.e("Get message extra JSON error!");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 方法1：检查某表列是否存在
     *
     * @param db
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    private boolean checkColumnExist(SQLiteDatabase db, String tableName
            , String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            //查询一行
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }
}
