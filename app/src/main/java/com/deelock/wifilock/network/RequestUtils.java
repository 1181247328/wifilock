package com.deelock.wifilock.network;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.deelock.wifilock.utils.AES7P256;
import com.deelock.wifilock.utils.MD5Util;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ZLibUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/**
 * Created by binChuan on 2017\10\16 0016.
 */

public class RequestUtils {

    static int check;
    static String token;
    static String sign;
    static String content;
    static String accountMark;
    static String version;

    private static final String user = "user/";
    private static final String dev = "dev/";
    private static final String system = "system/";
    private static final String global = "global/";
    private static final String gateway = "gateway/";
    private static final String single = "single/";
    private static final String pros = "pros/";

    public static final String CHECK = user + "check.json";
    public static final String REGISTER = user + "register.json";
    public static final String GET_MESSAGE = system + "msg_code.json";

    public static final String LOGIN = user + "login.json";
    public static final String LOGOUT = user + "logout.json";
    public static final String FORGET = user + "pwd_forget.json";
    public static final String UPDATE_INFO = user + "update_info.json";

    public static final String UPDATE_LOCK = single + "wifi/lock/update.json";
    public static final String UPDATE_PASSWORD = user + "pwd_update.json";
    public static final String DETAIL_INFO = user + "detailed_info.json";
    public static final String VERIFY_PASSWORD = user + "pwd_check.json";
    public static final String LOCK_STATE = single + "wifi/lock/state.json";
    public static final String LOCK_LIST = global + "list.json";
    public static final String LOCK_DETAIL_INFO = single + "wifi/lock/detailed_info.json";
    public static final String HISTORY = single + "wifi/lock/history/paged_info.json";
    public static final String DELETE_ONE = single + "wifi/lock/history/del_single.json";
    public static final String DELETE_DAY = single + "wifi/lock/history/del_multi.json";
    public static final String ALL_PASSWORD = single + "wifi/lock/pwd/all_list_info.json";
    public static final String ALL_FPRINT = single + "wifi/lock/fprint/all_list_info.json";
    public static final String UPDATE_USER_PASSWORD_NAME = single + "wifi/lock/pwd/update_user.json";
    public static final String UPDATE_USER_PRINT_NAME = single + "wifi/lock/fprint/update_user.json";
    public static final String USER_LIST = single + "wifi/lock/user/list_info.json";
    public static final String USER_LIST_INFO = single + "wifi/lock/user/list_detail_info.json";

    public static final String ADD_AUTH_USER = single + "wifi/lock/user/add.json";
    public static final String DELETE_USER = single + "wifi/lock/user/del.json";

    public static final String ADD_AUTH_USER_1 = single + "wifi/bt_lock/user/add.json";
    public static final String DELETE_USER_1 = single + "wifi/bt_lock/user/del.json";

    public static final String ADD_USER_PASSWORD = single + "wifi/lock/pwd/add_user.json";
    public static final String ADD_TEMP_PASSWORD = single + "wifi/lock/pwd/add_temp.json";
    public static final String ADD_PRINT = single + "wifi/lock/fprint/add_user.json";
    public static final String DELETE_PASSWORD = single + "wifi/lock/pwd/del_user.json";
    public static final String DELETE_TEMP_PASSWORD = single + "wifi/lock/pwd/del_temp.json";
    public static final String DELETE_PRINT = single + "wifi/lock/fprint/del_user.json";
    public static final String ORDER_ID = single + "wifi/lock/fprint/order_id.json";
    public static final String CIRCLE_STATE = single + "wifi/lock/fprint/circle_state.json";
    public static final String BIND = single + "wifi/bind.json";
    public static final String UNBIND = single + "wifi/unbind.json";
    public static final String VERSION = system + "version.json";
    public static final String UPDATE_USER = single + "wifi/lock/user/update.json";
    public static final String EVALUATE_ITEMS = single + "wifi/lock/install/satisfaction_items.json";
    public static final String TEMP_DEL_CANCEL = single + "wifi/lock/pwd/del_temp_cancel.json";
    public static final String USER_DEL_CANCEL = single + "wifi/lock/pwd/del_user_cancel.json";
    public static final String PRINT_DEL_CANCEL = single + "wifi/lock/fprint/del_user_cancel.json";
    public static final String MESSAGE = global + "alert/paged_info.json";
    public static final String MESSAGE_DELETE = global + "alert/del_single.json";
    public static final String CITY_LIST = "single/" + pros + "city_list.json";
    public static final String COMMUNITY_LIST = "single/" + pros + "community_list.json";
    public static final String COMMUNITY_DETAIL = "single/" + pros + "community_detail.json";
    public static final String ADD_PROPERTY = "single/" + pros + "application_access.json";
    public static final String GET_PROPERTY_RESULT = "single/" + pros + "application_access/result.json";
    public static final String CANCEL_PROPERTY = "single/" + pros + "application_access/cancel.json";
    public static final String ADD_OWNER = "single/" + pros + "master_add.json";
    public static final String MODIFY_OWNER = "single/" + pros + "master_update.json";
    public static final String GET_OWNER = "single/" + pros + "master_detail.json";
    public static final String MAGNETOMETER_STATE = single + "wifi/door_sensor/state.json";
    public static final String MAGNETOMETER_DETAIL = single + "wifi/door_sensor/detailed_info.json";
    public static final String MAGNETOMETER_UPDATE = single + "wifi/door_sensor/update.json";
    public static final String MAGNETOMETER_HISTORY = single + "wifi/door_sensor/history/paged_info.json";
    public static final String MAG_HISTORY_DELETE = single + "wifi/door_sensor/history/del_single.json";
    public static final String MAG_HISTORY_DELETE_DAY = single + "wifi/door_sensor/history/del_multi.json";
    public static final String AD = system + "ad_flyer.json";
    public static final String ADD_KEY = single + "wifi/pwd/pwd_add_homestay.json";
    public static final String GET_KEY = single + "wifi/lock/key_part.json";
    public static final String ADD_HS_PWD = single + "wifi/lock/pwd/pwd_add_homestay.json";
    //房源接入猩家
    public static final String MAGNETIC_ACCESS = single + "wifi/sds/rent.json";
    //网关绑定
    public static final String GATEWAY_BIND = gateway + "bind.json";
    //网关解绑
    public static final String GATEWAY_UNBIND = gateway + "unbind.json";
    //获取网关列表
    public static final String GATEWAY_LIST = gateway + "list.json";
    //获取网关详情列表
    public static final String GATEWAY_DETAIL = gateway + "detail.json";
    //网关更新
    public static final String GATEWAY_UPDATE = gateway + "update.json";
    //获取网关详情
    public static final String GATEWAY_DEVICES = gateway + "sub_devices.json";
    //zigbee门磁绑定
    public static final String ZIGBEE_BIND = gateway + "zigbee/door_sensor/bind.json";
    //zigbee门磁获取详情
    public static final String ZIGBEE_DETAIL = gateway + "zigbee/door_sensor/detail.json";
    //zigbee门磁更新
    public static final String ZIGBEE_UPDATE = gateway + "zigbee/door_sensor/update.json";
    //zigbee分页获取历史记录
    public static final String ZIGBEE_PAGED_INFO = gateway + "zigbee/door_sensor/history/paged_info.json";
    //zigbee删除门磁单条历史记录
    public static final String ZIGBEE_DEL_SINGLE = gateway + "zigbee/door_sensor/history/del_single.json";
    //zigbee删除门磁某天历史记录
    public static final String ZIGBEE_DEL_MULTI = gateway + "zigbee/door_sensor/history/del_multi.json";
    //红外绑定
    public static final String INFRARED_BIND = gateway + "zigbee/infrared_detector/bind.json";
    //红外详情
    public static final String INFRARED_DETAIL = gateway + "zigbee/infrared_detector/detail.json";
    //红外更新
    public static final String INFRARED_UPDATE = gateway + "zigbee/infrared_detector/update.json";
    //红外分页获取历史记录
    public static final String INFRARED_HISTORY = gateway + "zigbee/infrared_detector/history/paged_info.json";
    //删除红外单条历史记录
    public static final String INFRARED_DEL_SINGLE = gateway + "zigbee/infrared_detector/history/del_single.json";
    //删除红外某天历史记录
    public static final String INFRARED_DEL_MULTI = gateway + "zigbee/infrared_detector/history/del_multi.json";
    //蓝牙锁绑定请求
    public static final String BLE_BIND = single + "wifi/bt_lock/bt_bind_result.json";
    //蓝牙锁请求秘钥
    public static final String BLE_KEY = single + "wifi/bt_lock/key_part.json";
    //蓝牙锁根据用户获取门锁密码列表
    public static final String BLE_PWD_LIST_N = single + "wifi/bt_lock/pwd/user_list_info.json";
    //蓝牙锁根据管理员获取门锁列表
    public static final String BLE_PWD_LIST_M = single + "wifi/bt_lock/pwd/all_list_info.json";
    //蓝牙锁增加用户密码
    public static final String BLE_PWD_ADD_U = single + "wifi/bt_lock/pwd/add_user.json";
    //蓝牙锁增加临时密码
    public static final String BLE_PWD_ADD_T = single + "wifi/bt_lock/pwd/add_temp.json";
    //蓝牙锁增加时段密码
    public static final String BLE_PWD_ADD_R = single + "wifi/bt_lock/pwd/add_regular.json";
    //蓝牙锁删除临时密码
    public static final String BLE_PWD_DEL_T = single + "wifi/bt_lock/pwd/del_temp.json";
    //蓝牙锁删除用户密码
    public static final String BLE_PWD_DEL_U = single + "wifi/bt_lock/pwd/del_user.json";
    //蓝牙锁删除时段密码
    public static final String BLE_PWD_DEL_R = single + "wifi/bt_lock/pwd/del_regular.json";
    //请求蓝牙锁操作指令
    public static final String BLE_CMD = single + "wifi/bt_lock/bt_product_cmd.json";
    //蓝牙锁增加用户指纹
    public static final String BLE_ADD_FPRINT = single + "wifi/bt_lock/fprint/add_user.json";
    //蓝牙锁上报删除用户指纹
    public static final String BLE_SEND_DELETE_FPRINT = single + "wifi/bt_lock/del_bt_fprint.json";
    //蓝牙锁上报删除用户普通密码
    public static final String BLE_SEND_N_PW = single + "wifi/bt_lock/del_bt_pwd.json";
    //蓝牙锁上报删除用户临时密码
    public static final String BLE_SEND_T_PW = single + "wifi/bt_lock/del_bt_pwd_temp.json";
    //蓝牙锁详情
    public static final String BLE_INFO = single + "wifi/bt_lock/detailed_info.json";
    //蓝牙锁上报解绑结果
    public static final String BLE_UNBIND = single + "wifi/bt_lock/del_bt_device.json";
    //蓝牙锁上报绑定结果
    public static final String BLE_BIND_RESULT = single + "wifi/bt_lock/bt_add_wifi.json";
    //蓝牙锁上报修改门锁信息接口
    public static final String BLE_UPDATE = single + "wifi/bt_lock/update.json";
    //蓝牙锁转发设备指令
    public static final String BLE_FORWARD = single + "wifi/bt_lock/bt_accept_cmd.json";
    //蓝牙锁修改管理员密码
    public static final String BLE_UPDATE_PWD = single + "wifi/bt_lock/pwd/update_user.json";

    //蓝牙锁获取用户列表
    public static final String BLE_USER_LIST = single + "wifi/bt_lock/user/list_info.json";
    //蓝牙锁删除授权用户
    public static final String BLE_DELETE_USER = single + "wifi/bt_lock/user/del.json";
    //蓝牙锁获取授权用户详情
    public static final String BLE_USER_DETAIL = single + "wifi/bt_lock/user/list_detail_info.json";
    //蓝牙锁修改授权用户
    public static final String BLE_UPDATE_AUTH_USER = single + "wifi/bt_lock/user/update.json";

    //请求WIFI配置信息
    public static final String BLE_WIFI = single + "wifi/bt_lock/bt_product_cmd.json";

    private static void unLoggedData(Context context, Map params) {
        check = 0;
        sign = MD5Util.string2MD5(params);
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = "av" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        byte[] zip = ZLibUtils.compress(new Gson().toJson(params));
        content = AES7P256.encrypt(zip, null);
    }

    private static void loggedData(Context context, Map params) {
        check = 1;
        token = SPUtil.getToken(context);
        accountMark = SPUtil.getKey(context);
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = "av" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sign = MD5Util.string2MD5(params);
        byte[] zip = ZLibUtils.compress(new Gson().toJson(params));
        content = AES7P256.encrypt(zip, accountMark);
        //TODO 使用token加密
//        content = AES7P256.encrypt(zip, token);
    }

    public static Call<BaseResponse> request(String url, Context context, Map params) {
//        Iterator it = params.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry entry = (Map.Entry) it.next();
//            Log.e("main_上传", entry.getKey() + ": " + entry.getValue());
//        }
//        Log.e("main_url", url);
        RequestService rs = ServerFactory.create(RequestService.class);
        loggedData(context, params);
        return rs.commonRequest(url, version, sign, token, check, content);
    }

    public static Call<BaseResponse> deleteImage(Context context, String url) {
        RequestService rs = ServerFactory.create(RequestService.class);
        return rs.deleteImage(token, SPUtil.getUid(context), TimeUtil.getTime(), url);
    }

    public static Call<BaseResponse> requestUnLogged(String url, Context context, Map params) {
        RequestService rs = ServerFactory.create(RequestService.class);
        unLoggedData(context, params);
        return rs.commonRequest(url, version, sign, null, check, content);
    }

    public static Gson getGson() {
        Gson gson = new GsonBuilder().
                registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue())
                            return new JsonPrimitive(src.longValue());
                        return new JsonPrimitive(src);
                    }
                }).create();
        return gson;
    }

    public static <T> T fromJson(String json, TypeToken<T> typeToken) {
        Gson gson = new GsonBuilder()
/**
 * 重写map的反序列化
 */
                .registerTypeAdapter(new TypeToken<Map<String, Object>>() {
                }.getType(), new MapTypeAdapter()).create();
//MapTypeAdapter是继承了TypeAdapter类，并单独处理Map类型的反序列化。注意：目前只绑定了Map类型，其子类（HashMap）的处理没有变化。具体代码见本文最后或GitHub（发布后会给出地址）。
        return gson.fromJson(json, typeToken.getType());
    }

    public static class MapTypeAdapter extends TypeAdapter<Object> {

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    /**
                     * 改写数字的处理逻辑，将数字值分为整型与浮点型。
                     */
                    double dbNum = in.nextDouble();

                    // 数字超过long的最大值，返回浮点类型
                    if (dbNum > Long.MAX_VALUE) {
                        return dbNum;
                    }

                    // 判断数字是否为整数值
                    long lngNum = (long) dbNum;
                    if (dbNum == lngNum) {
                        return lngNum;
                    } else {
                        return dbNum;
                    }

                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            // 序列化无需实现
        }

    }
}
