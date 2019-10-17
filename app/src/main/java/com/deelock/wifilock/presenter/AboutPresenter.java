package com.deelock.wifilock.presenter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.deelock.wifilock.model.IAboutView;
import com.deelock.wifilock.ui.activity.AboutActivity;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by forgive for on 2018\3\22 0022.
 */

public class AboutPresenter implements AboutPresenterImpl {

    private IAboutView view;
    private AboutActivity activity;

    public AboutPresenter(Activity activity, IAboutView view){
        this.view = view;
    }

    @Override
    public void getInfo() {
        String versionName = "";
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        int year = c.get(Calendar.YEAR);
        if (view != null){
            view.setCopyRight("成都迪洛可科技有限公司 版权所有\nCopyright © 2017 - "+year+" Chengdu Deelock Technology Co., Ltd. \nAll rights reserved.");
        }

        try {
            PackageInfo packageInfo = activity.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0);
            versionName = packageInfo.versionName;
            Log.d("TAG", "本软件的版本号。。" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (view != null){
            view.setVersion("v"+versionName);
        }
    }
}
