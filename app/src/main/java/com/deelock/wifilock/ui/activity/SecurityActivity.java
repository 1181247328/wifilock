package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.HelpInfo;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.ui.dialog.SecurityDialog;
import com.deelock.wifilock.utils.InputUtil;
import com.deelock.wifilock.utils.SPUtil;

import com.deelock.wifilock.utils.ToastUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by forgive for on 2018\6\6 0006.
 */
public class SecurityActivity extends BaseActivity {

    private ImageButton back_ib;
    private EditText phone_et;
    private EditText notice_name_et;
    private EditText help_name_et;
    private TextView closeTv;
    private TextView openTv;
    private View bgV;
    private View guideIv;
    private View guideEnsureV;

    private String sdlId;
    private String pid;
    private String authId;
    private int isSecurityHelp = -1;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_security);

        back_ib = f(R.id.back_ib);
        phone_et = f(R.id.phone_et);
        notice_name_et = f(R.id.notice_name_et);
        help_name_et = f(R.id.help_name_et);
        closeTv = f(R.id.closeTv);
        openTv = f(R.id.openTv);
        bgV = f(R.id.bgV);
        guideIv = f(R.id.guideIv);
        guideEnsureV = f(R.id.guideEnsureV);
    }

    @Override
    protected void doBusiness() {
        if (SPUtil.isFirstTimeIntoSecurityPage(this)){
            bgV.setVisibility(View.VISIBLE);
            guideIv.setVisibility(View.VISIBLE);
            guideEnsureV.setVisibility(View.VISIBLE);
            SPUtil.setFirstTimeIntoSecurityPage(this);
        }

        setNameInputFilter(notice_name_et);
        setNameInputFilter(help_name_et);
        HelpInfo info = SPUtil.getHelpInfo(this);
        phone_et.setText(info.getHelpNumber());
        help_name_et.setText(info.getHelpName());
        notice_name_et.setText(info.getNoticeName());

        sdlId = getIntent().getStringExtra("sdlId");
        pid = getIntent().getStringExtra("pid");
        authId = getIntent().getStringExtra("authId");
    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("isSecurityHelp", isSecurityHelp);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        closeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestClose();
            }
        });

        guideEnsureV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bgV.setVisibility(View.GONE);
                guideIv.setVisibility(View.GONE);
                guideEnsureV.setVisibility(View.GONE);
            }
        });

        openTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone_et.getText().toString().length() < 11){
                    ToastUtil.toastLong(SecurityActivity.this, "请输入手机号");
                    return;
                }

                if (!InputUtil.checkPhoneNumber(SecurityActivity.this, phone_et.getText().toString())){
                    return;
                }

                if (notice_name_et.getText().toString().isEmpty()){
                    ToastUtil.toastLong(SecurityActivity.this, "请输入通知人姓名");
                    return;
                }
                if (help_name_et.getText().toString().isEmpty()){
                    ToastUtil.toastLong(SecurityActivity.this, "请输入求助人姓名");
                    return;
                }

                SecurityDialog dialog = new SecurityDialog(SecurityActivity.this);
                StringBuilder noticeName = new StringBuilder(notice_name_et.getText());
                StringBuilder helpName = new StringBuilder(help_name_et.getText());
                if (noticeName.length() > 1){
                    noticeName.replace(noticeName.length() - 2, noticeName.length() - 1, "x");
                }

                if (helpName.length() > 1){
                    helpName.replace(helpName.length() - 2, helpName.length() - 1, "x");
                }

                dialog.setContent("求助电话："+ phone_et.getText().toString().substring(0, 3)
                        + "****" + phone_et.getText().toString().substring(7),
                        "通知内容：您好，"+ noticeName.toString() +"，Deelock\n" +
                                "检测到"+ helpName.toString() +"于xx时xx分可能存在安\n" +
                                "全风险，正向您发起求助！");
                dialog.setEvent(new SecurityDialog.Event() {
                    @Override
                    public void ensure() {
                        HelpInfo info = new HelpInfo();
                        info.setHelpNumber(phone_et.getText().toString());
                        info.setNoticeName(notice_name_et.getText().toString());
                        info.setHelpName(help_name_et.getText().toString());
                        SPUtil.setHelpInfo(SecurityActivity.this, info);
                        requestOpen();
                    }
                });
                dialog.show();
            }
        });
    }

    private void requestOpen(){
        String url;
        if (getIntent().getIntExtra("flag", 1) == 1){
            url = RequestUtils.UPDATE_USER_PRINT_NAME;
        } else {
            url = RequestUtils.UPDATE_USER_PASSWORD_NAME;
        }
        Intent intent = new Intent(this, SendMessageActivity.class);
        intent.putExtra("sdlId", sdlId);
        intent.putExtra("pid", pid);
        intent.putExtra("authId", authId);
        intent.putExtra("calledName", notice_name_et.getText().toString());
        intent.putExtra("helpedName", help_name_et.getText().toString());
        intent.putExtra("calledNumber", phone_et.getText().toString());
        intent.putExtra("url", url);
        startActivityForResult(intent, 0);
    }

    private void requestClose(){
        String url;
        if (getIntent().getIntExtra("flag", 1) == 1){
            url = RequestUtils.UPDATE_USER_PRINT_NAME;
        } else {
            url = RequestUtils.UPDATE_USER_PASSWORD_NAME;
        }
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("uid", SPUtil.getUid(this));
        params.put("sdlId", sdlId);
        params.put("pid", pid);
        params.put("authId", authId);
        params.put("isSecurityHelp", 0);
        RequestUtils.request(url, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        ToastUtil.toastLong(SecurityActivity.this, "安全求助已关闭");
                        Intent intent = new Intent();
                        intent.putExtra("isSecurityHelp", 0);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0){
            if (data == null){
                return;
            }
            isSecurityHelp = data.getIntExtra("isSecurityHelp", 0);
            Intent intent = new Intent();
            intent.putExtra("isSecurityHelp", isSecurityHelp);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
//            Intent intent = new Intent();
//            intent.putExtra("isSecurityHelp", isSecurityHelp);
//            setResult(RESULT_OK, intent);
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
