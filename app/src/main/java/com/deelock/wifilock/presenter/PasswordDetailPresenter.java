package com.deelock.wifilock.presenter;

import android.content.Context;

import com.deelock.wifilock.model.IPasswordDetailModel;
import com.deelock.wifilock.model.PasswordDetailModel;
import com.deelock.wifilock.view.IPasswordDetailView;

/**
 * Created by forgive for on 2018\5\3 0003.
 */
public class PasswordDetailPresenter implements IPasswordDetailModel {

    private Context context;
    private IPasswordDetailView view;
    private PasswordDetailModel model;

    public PasswordDetailPresenter(Context context, IPasswordDetailView view){
        this.context = context;
        this.view = view;
        model = new PasswordDetailModel(context);
    }

    public void clickSwitch(){
        model.resetProtection();
    }

    public void clickNumber(){

    }
}
