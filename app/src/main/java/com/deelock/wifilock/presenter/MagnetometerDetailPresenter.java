package com.deelock.wifilock.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.LockDetail;
import com.deelock.wifilock.entity.MagDetail;
import com.deelock.wifilock.model.MagnetometerDetailModel;
import com.deelock.wifilock.model.MagnetometerDetailModelImpl;
import com.deelock.wifilock.ui.activity.AboutHardwareActivity;
import com.deelock.wifilock.ui.activity.ChangeWifiActivity;
import com.deelock.wifilock.ui.dialog.AccessDialog;
import com.deelock.wifilock.ui.dialog.DeleteDialog;
import com.deelock.wifilock.ui.dialog.NickNameDialog;
import com.deelock.wifilock.ui.dialog.PushDialog;
import com.deelock.wifilock.utils.ToastUtil;
import com.deelock.wifilock.view.IMagnetometerDetailView;

/**
 * Created by forgive for on 2018\5\18 0018.
 */
public class MagnetometerDetailPresenter implements MagnetometerDetailModelImpl {

    private Context context;
    private IMagnetometerDetailView view;
    private MagnetometerDetailModel model;
    private NickNameDialog nickNameDialog;
    private DeleteDialog deleteDialog;
    private PushDialog pushDialog;
    private MagDetail detail;
    private AccessDialog accessDialog;

    private String tempName;

    public MagnetometerDetailPresenter(Context context, IMagnetometerDetailView view, MagDetail detail) {
        this.context = context;
        this.view = view;
        model = new MagnetometerDetailModel(this, detail.getPid());
        nickNameDialog = new NickNameDialog(context, R.style.dialog);
        deleteDialog = new DeleteDialog(context, R.style.dialog);
        deleteDialog.setNoticeTitle("您确定要解绑该设备");
        pushDialog = new PushDialog(context);
        accessDialog = new AccessDialog(context, R.style.dialog);
        view.setName(detail.getNickName());
        view.setWiFi(detail.getSsid());
        refreshPushState(detail.getAlertType());
        switch (detail.getIsCall()) {
            case 0:
                view.setWarning(false);
                break;
            case 1:
                view.setWarning(true);
                break;
        }
        this.detail = detail;
    }

    @Override
    public void onModifyName() {
        detail.setNickName(tempName);
        view.setName(tempName);
    }

    @Override
    public void onDelete() {
        ((Activity) context).finish();
        onDestroy();
    }

    public void warning(boolean isChecked) {
        model.warning(context, isChecked);
    }

    public void onClick(int id) {
        switch (id) {
            case R.id.back_ib:
                ((Activity) context).finish();
                break;
            case R.id.name_rl:
                nickNameDialog.setEvent(new NickNameDialog.Event() {
                    @Override
                    public void ensure(String name) {
                        tempName = name;
                        view.setName(name);
                        model.modifyName(context, name);
                    }
                });
                nickNameDialog.show();
                break;
            case R.id.about_rl:
                Intent intent = new Intent(context, AboutHardwareActivity.class);
                intent.putExtra("id", detail.getPid());
                intent.putExtra("hard", detail.getHardVersion());
                intent.putExtra("soft", detail.getSoftVersion());

                context.startActivity(intent);
                break;
            case R.id.change_wifi_rl:
                Intent intent1 = new Intent(context, ChangeWifiActivity.class);
                intent1.putExtra("ssid", detail.getSsid());
                intent1.putExtra("type", detail.getPid().substring(0, 3));
                context.startActivity(intent1);
                break;
            case R.id.push_rl:
                final String[] items = {"开门推送", "关门推送"};
                boolean[] b = {false, false};
                switch (detail.getAlertType()) {
                    case "00":  //什么都不推送
                        b = new boolean[]{false, false};
                        break;
                    case "01":  //开门不推送，关门推送
                        b = new boolean[]{false, true};
                        break;
                    case "10":  //开门推送，关门不推
                        b = new boolean[]{true, false};
                        break;
                    case "11":   //开关门推送
                        b = new boolean[]{true, true};
                        break;
                }
                final boolean[] checkedItems = b;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int alertType = 0;
                        if (checkedItems[0]) {
                            alertType += 10;
                        }
                        if (checkedItems[1]) {
                            alertType += 1;
                        }
                        String type;
                        if (alertType == 0) {
                            type = "00";
                        } else if (alertType == 1) {
                            type = "01";
                        } else {
                            type = Integer.toString(alertType);
                        }
                        if (!type.equals(detail.getAlertType())) {
                            model.setPush(context, type);
                            refreshPushState(type);
                            detail.setAlertType(type);
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.delete_ll:
                deleteDialog.setEvent(new DeleteDialog.Event() {
                    @Override
                    public void delete() {
                        model.deleteEquipment(context);
                    }

                    @Override
                    public void cancel() {

                    }
                });
                deleteDialog.show();
                break;
            case R.id.magnetometer_access:
                accessDialog.setEvent(new AccessDialog.Event() {
                    @Override
                    public void ensure(String inputContent) {
                        if (inputContent.length() != 8) {
                            ToastUtil.toastShort(context, "请输入8位验证码");
                            return;
                        }
                        model.accessXinJia(context, inputContent);
                    }

                    @Override
                    public void cancel() {

                    }
                });
                accessDialog.show();
                break;
        }
    }

    private void refreshPushState(String type) {
        switch (type) {
            case "00":
                view.setPush("开门不推送", "关门不推送");
                break;
            case "01":
                view.setPush("开门不推送", "关门推送");
                break;
            case "10":
                view.setPush("开门推送", "关门不推送");
                break;
            case "11":
                view.setPush("开门推送", "关门推送");
                break;
        }
    }

    public void onResume() {
    }

    public void onDestroy() {
        view = null;
    }
}
