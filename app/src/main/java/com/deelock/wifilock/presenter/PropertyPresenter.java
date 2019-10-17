package com.deelock.wifilock.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.contrarywind.interfaces.IPickerViewData;
import com.deelock.wifilock.entity.Building;
import com.deelock.wifilock.entity.City;
import com.deelock.wifilock.entity.Community;
import com.deelock.wifilock.entity.Property;
import com.deelock.wifilock.entity.PropertyInfo;
import com.deelock.wifilock.entity.PropertyResult;
import com.deelock.wifilock.entity.Province;
import com.deelock.wifilock.model.PropertyModel;
import com.deelock.wifilock.model.PropertyModelImpl;
import com.deelock.wifilock.utils.InputUtil;
import com.deelock.wifilock.utils.ToastUtil;
import com.deelock.wifilock.view.IPropertyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by forgive for on 2018\5\3 0003.
 */
public class PropertyPresenter implements PropertyModelImpl {

    private PropertyModel model;
    private List<Prov> provinceList = new ArrayList<>();
    private List<Community> communityList;
    private List<Property> propertyList;
    private List<List<City>> cities = new ArrayList<>();
    private List<List<Building>> buildingList;
    private OptionsPickerView cityPicker;
    private Context context;
    private IPropertyView view;

    private int prosId = -1;
    private int commId = -1;
    private int buildingId = -1;
    private int selectMode = -1;
    private final int SELECT_CITY = 1;
    private final int SELECT_COMMUNITY = 2;
    private int isProsHelp;

    public PropertyPresenter(final Context context, IPropertyView view, String sdlId, int isProsHelp) {
        this.context = context;
        this.view = view;
        model = new PropertyModel(context, this, sdlId);
        this.isProsHelp = isProsHelp;
    }

    public void apply(String name, String address, String number) {
        if (TextUtils.isEmpty(name) || name.length() < 2) {
            ToastUtil.toastLong(context, "用户名格式有误");
            return;
        }
        if (commId == -1 || prosId == -1 || buildingId == -1) {
            ToastUtil.toastLong(context, "请选择小区及楼栋号");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            ToastUtil.toastLong(context, "单元和户格式有误");
            return;
        }
        if (!InputUtil.checkPhoneNumber(context, number)) {
            return;
        }
        model.addOwner(name, prosId, commId, buildingId, address, number);
        view.opening();
    }

    public void cancelApply() {
        model.cancelApply();
    }

    public void selectCity() {
        if (provinceList == null || provinceList.size() == 0) {
            return;
        }
        selectMode = SELECT_CITY;
        initCityPicker();
        cityPicker.setPicker(provinceList, cities);
        cityPicker.show();
    }

    public void selectCommunity() {
        if (communityList == null || communityList.size() == 0) {
            return;
        }
//        if (buildingList.size() == 0){
//            return;
//        }
        if (buildingList == null) {
            buildingList = new ArrayList<>();
        } else {
            buildingList.clear();
        }

        for (Community c : communityList) {
            buildingList.add(c.getBuildingList());
        }
        selectMode = SELECT_COMMUNITY;
        initCityPicker();
        cityPicker.setPicker(communityList, buildingList);
        cityPicker.show();
    }

    private void initCityPicker() {
        if (cityPicker == null) {
            cityPicker = new OptionsPickerBuilder(context, new OnOptionsSelectListener() {
                @SuppressLint("CheckResult")
                @Override
                public void onOptionsSelect(final int options1, final int options2, int options3, View v) {
                    //返回的分别是三个级别的选中位置
                    switch (selectMode) {
                        case SELECT_CITY:
                            model.getCommunity(cities.get(options1).get(options2).getCityId());
                            String tx = provinceList.get(options1).getName()
                                    + cities.get(options1).get(options2).getPickerViewText();
                            view.setCity(tx);
                            view.setAddress("");
                            prosId = -1;
                            commId = -1;
                            buildingId = -1;
                            break;
                        case SELECT_COMMUNITY:
                            commId = communityList.get(options1).getCommId();
                            prosId = communityList.get(options1).getPropertyList().get(0).getProsId();
                            buildingId = buildingList.get(options1).get(options2).getBuildingId();
                            String t = communityList.get(options1).getCommName()
                                    + buildingList.get(options1).get(options2).getBuildingName();
                            view.setAddress(t);
                            break;
                    }
                }
            })
                    .setContentTextSize(20)//设置滚轮文字大小
                    .setDividerColor(Color.LTGRAY)//设置分割线的颜色
                    .setSelectOptions(0, 0)//默认选中项
                    .setBgColor(0xfff0f0f0)
                    .setTitleBgColor(Color.LTGRAY)
                    .setCancelColor(Color.BLUE)
                    .setSubmitColor(Color.BLUE)
                    .setTextColorCenter(Color.BLACK)
                    .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    .setLabels("", "", "")
                    .build();
        }
    }

    public void switchPros(boolean isOpen) {
        if (isProsHelp != 1) {
            ToastUtil.toastLong(context, "物业联动尚未开通！");
            return;
        }

        model.switchPros(isOpen);
    }

    @Override
    public void getCityList(List<Province> list) {
        for (Province p : list) {
            Prov pp = new Prov();
            pp.setId(p.getProvId());
            pp.setName(p.getPickerViewText());
            provinceList.add(pp);
            cities.add(p.getList());
        }
    }

    @Override
    public void getCommunityList(List<Community> list) {
        communityList = list;
    }

    @Override
    public void getBuildingList(List<List<Building>> list, List<Property> propertyList) {
        buildingList = list;
        this.propertyList = propertyList;
    }

    @Override
    public void applySucceed() {
        view.opening();
    }

    @Override
    public void getResult(PropertyResult result) {
        if (result == null) {
            return;
        }

        switch (result.getResult()) {
            case 1:
                isProsHelp = 1;
                view.opened();
                break;
            case 0:
                view.opening();
                break;
            case -1:
                view.openFailed();
                break;
            case -2:
                view.openRefused();
                break;
            default:
                view.notOpen();
                break;
        }
    }

    @Override
    public void cancelSucceed() {
        isProsHelp = 0;
        view.notOpen();
    }

    @Override
    public void getInfo(PropertyInfo info) {
        view.setInfo(info);
    }

    @Override
    public void onFinish() {

    }

    class Prov implements IPickerViewData {
        int id;
        String name;

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getPickerViewText() {
            return name;
        }
    }
}
