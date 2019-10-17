package com.deelock.wifilock.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deelock.wifilock.entity.BuildingList;
import com.deelock.wifilock.entity.Community;
import com.deelock.wifilock.entity.CommunityList;
import com.deelock.wifilock.entity.PropertyInfo;
import com.deelock.wifilock.entity.PropertyResult;
import com.deelock.wifilock.entity.ProvList;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by forgive for on 2018\5\4 0004.
 */
public class PropertyModel implements IPropertyModel {

    private Context context;
    private String sdlId;
    private PropertyModelImpl model;
    private ProvList provList;
    private List<Community> commList;
//    private List<List<Building>> buildingList = new ArrayList<>();
//    private List<Property> propertyList = new ArrayList<>();
    private volatile PropertyInfo info;
    private PropertyResult result;
    private int communitySize = 0;

    public PropertyModel(Context context, PropertyModelImpl model, String sdlId){
        this.context = context;
        this.sdlId = sdlId;
        this.model = model;
        getOwner();
        getCity();
        getApplyResult();
    }

    @Override
    public void getCity(){
        if (provList != null){
            model.getCityList(provList.getList());
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("pid", sdlId);
        RequestUtils.request(RequestUtils.CITY_LIST, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                provList = new Gson().fromJson(content, ProvList.class);
                model.getCityList(provList.getList());
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(context,message);
            }
        });
    }

    @Override
    public void getCommunity(int cityId){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("cityId", cityId);
        RequestUtils.request(RequestUtils.COMMUNITY_LIST, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                commList = new Gson().fromJson(content, CommunityList.class).getList();
                for (Community c: commList){
                    getBuilding(c.getCommId());
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(context,message);
            }
        });
    }

    @Override
    public void getBuilding(final int commId){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("commId", commId);
        RequestUtils.request(RequestUtils.COMMUNITY_DETAIL, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code != 1){
                    return;
                }
                BuildingList b = new Gson().fromJson(content, BuildingList.class);
                Collections.sort(b.getList());
                for (Community c: commList){
                    if (c.getCommId() == commId){
                        c.setBuildingList(b.getList());
                        c.setPropertyList(b.getProsList());
                        communitySize++;
                        break;
                    }
                }
                if (communitySize == commList.size()){
                    model.getCommunityList(commList);
                    communitySize = 0;
                }
            }

            @Override
            protected void onFailure(int code, String message) {
                super.onFailure(code, message);
                ToastUtil.toastShort(context,message);
            }
        });
    }

    @Override
    public void apply(int commId, int prosId, String number){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("commId", commId);
        params.put("prosId", prosId);
        params.put("devId", sdlId);
        params.put("phoneNumber", number);
        RequestUtils.request(RequestUtils.ADD_PROPERTY, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1){
                    model.applySucceed();
                }
                if (result == null){
                    result = new PropertyResult();
                }
                int index = content.indexOf("orderId");
                if (index != -1){
                    result.setOrderId(content.substring(index + 10, content.length() - 2));
                }
            }
        });
    }

    @Override
    public void getApplyResult() {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("devId", sdlId);
        RequestUtils.request(RequestUtils.GET_PROPERTY_RESULT, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1){
                    result = new Gson().fromJson(content, PropertyResult.class);
                    model.getResult(result);
                }
            }
        });
    }

    @Override
    public void cancelApply() {
        if (result == null){
            return;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("devId", sdlId);
        params.put("cityId", 1);
        params.put("orderId", result.getOrderId());
        RequestUtils.request(RequestUtils.CANCEL_PROPERTY, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                model.cancelSucceed();

            }
        });
    }

    @Override
    public void addOwner(String name, final int prosId, final int commId, final int buildingId, String address, final String number){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("masterName", name);
        params.put("commId", commId);
        params.put("buildingId", buildingId);
        params.put("floor", address);
        params.put("devId", sdlId);
        params.put("phoneNumber", number);
        params.put("prosId",prosId);
        RequestUtils.request(RequestUtils.ADD_OWNER, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1){
                    apply(commId, prosId, number);
                    //getOwner();
                }
            }
        });
    }

    @Override
    public void modifyOwner(String name, final int prosId, final int commId, final int buildingId, String address, final String number) {
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("masterName", name);
        params.put("commId", commId);
        params.put("buildingId", buildingId);
        params.put("floor", address);
        params.put("devId", sdlId);
        params.put("phoneNumber", number);
        params.put("prosId",prosId);
        RequestUtils.request(RequestUtils.MODIFY_OWNER, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1){
                    info = new Gson().fromJson(content, PropertyInfo.class);
                    model.getInfo(info);
                }
            }
        });
    }

    @Override
    public void getOwner(){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(context));
        params.put("devId", sdlId);
        RequestUtils.request(RequestUtils.GET_OWNER, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {
            @Override
            protected void onSuccess(int code, String content) {
                if (code == 1){
                    info = new Gson().fromJson(content, PropertyInfo.class);
                    model.getInfo(info);
                }
            }
        });
    }

    @Override
    public void switchPros(final boolean isOpen){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int isProsHelp = 0;
                if (isOpen){
                    isProsHelp = 1;
                }
                Map params = new HashMap();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(context));
                params.put("pid", sdlId);
                params.put("isProsHelp", isProsHelp);
                RequestUtils.request(RequestUtils.UPDATE_LOCK, context, params).enqueue(new ResponseCallback<BaseResponse>((Activity) context) {

                });
            }
        }).start();
    }
}
