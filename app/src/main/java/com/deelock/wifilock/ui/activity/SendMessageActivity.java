package com.deelock.wifilock.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.network.BaseResponse;
import com.deelock.wifilock.network.RequestUtils;
import com.deelock.wifilock.network.ResponseCallback;
import com.deelock.wifilock.network.TimeUtil;
import com.deelock.wifilock.utils.SPUtil;
import com.deelock.wifilock.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by forgive for on 2018\6\7 0007.
 */
public class SendMessageActivity extends BaseActivity {


    //widget
    private ImageButton back_ib;
    private TextView phone_tv;
    private EditText message_et;
    private Button send_btn;
    private Button commit_btn;

    private String sdlId;
    private String pid;
    private String authId;
    private String calledName;
    private String helpedName;
    private String calledNumber;
    private String url;

    private boolean isSend;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_send_message);

        back_ib = f(R.id.back_ib);
        phone_tv = f(R.id.phone_tv);
        message_et = f(R.id.message_et);
        send_btn = f(R.id.send_btn);
        commit_btn = f(R.id.commit_btn);
    }

    @Override
    protected void doBusiness() {
        sdlId = getIntent().getStringExtra("sdlId");
        pid = getIntent().getStringExtra("pid");
        authId = getIntent().getStringExtra("authId");
        calledName = getIntent().getStringExtra("calledName");
        helpedName = getIntent().getStringExtra("helpedName");
        calledNumber = getIntent().getStringExtra("calledNumber");
        url = getIntent().getStringExtra("url");

        phone_tv.setText(calledNumber);
    }

    @Override
    protected void setEvent() {
        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSend){
                    return;
                }
                isSend = true;
                requestSend();
            }
        });

        commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message_et.getText().length() < 6){
                    ToastUtil.toastLong(SendMessageActivity.this, "请输入正确验证码");
                    return;
                }
                Map params = new HashMap();
                params.put("timestamp", TimeUtil.getTime());
                params.put("uid", SPUtil.getUid(SendMessageActivity.this));
                params.put("sdlId", sdlId);
                params.put("pid", pid);
                params.put("authId", authId);
                params.put("isSecurityHelp", 1);
                params.put("calledName", calledName);
                params.put("helpedName", helpedName);
                params.put("calledNumber", calledNumber);
                params.put("msgCode", message_et.getText().toString());
                RequestUtils.request(url, SendMessageActivity.this, params).enqueue(
                        new ResponseCallback<BaseResponse>(SendMessageActivity.this) {
                            @Override
                            protected void onSuccess(int code, String content) {
                                ToastUtil.toastLong(SendMessageActivity.this, "安全求助已开启");
                                Intent intent = new Intent();
                                intent.putExtra("isSecurityHelp", 1);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                );
            }
        });
    }

    private void requestSend(){
        Map params = new HashMap();
        params.put("timestamp", TimeUtil.getTime());
        params.put("type", 21);
        params.put("phoneNumber", calledNumber);
        params.put("helpedName", helpedName);
        RequestUtils.request(RequestUtils.GET_MESSAGE, this, params).enqueue(
                new ResponseCallback<BaseResponse>(this) {
                    @Override
                    protected void onSuccess(int code, String content) {
                        ToastUtil.toastLong(SendMessageActivity.this, "短信已发出");
                        countDown();
                    }
                }
        );
    }
    
    private void countDown(){
        final int count = 90;

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(@NonNull Long aLong) throws Exception {
                        return count - aLong;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                })
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        send_btn.setText(aLong + "秒后重发");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        send_btn.setText("发送验证码");
                    }
                });
    }
}
