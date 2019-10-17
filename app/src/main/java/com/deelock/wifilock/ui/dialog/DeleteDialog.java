package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;

/**
 * Created by Administrator on 2017\10\28 0028.
 */

public class DeleteDialog extends Dialog {

    TextView notice_title_tv;
    TextView notice_tv;
    TextView ensure_tv;
    TextView cancel_tv;
    ImageView icon_iv;

    String title;
    String content;
    int id;

    String left;
    String right;

    public DeleteDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        assert window != null;
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete);
        initView();
    }

    public void setNoticeTitle(String str){
        title = str;
        if (notice_title_tv != null){
            notice_title_tv.setText(title);
        }
    }

    public void setNotice(String str){
        content = str;
        if (ensure_tv != null){
            notice_tv.setText(content);
        }
    }

    public void setButton(String str){
        right = str;
        if (ensure_tv != null){
            ensure_tv.setText(right);
        }
    }

    public void setLeft(String str){
        left = str;
        if (cancel_tv != null){
            cancel_tv.setText(left);
        }
    }

    private void initView() {
        notice_tv = findViewById(R.id.notice_tv);
        notice_title_tv = findViewById(R.id.notice_title_tv);
        ensure_tv = findViewById(R.id.ensure_tv);
        cancel_tv = findViewById(R.id.cancel_tv);
        notice_title_tv.setText(title);
        if (!TextUtils.isEmpty(content)){
            notice_tv.setText(content);
        }
        if (!TextUtils.isEmpty(right)){
            ensure_tv.setText(right);
        }
        if (!TextUtils.isEmpty(left)){
            cancel_tv.setText(left);
        }

        findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (event != null){
                    event.cancel();
                }
            }
        });

        findViewById(R.id.ensure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (event != null){
                    event.delete();
                }
            }
        });
    }

    public interface Event{
        void delete();
        void cancel();
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
