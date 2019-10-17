package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\3\12 0012.
 */

public class PrintDialog extends Dialog {

    Context context;
    private TextView notice_tv;
    private TextView cancel_tv;
    private TextView ensure_tv;
    private String notice;

    public PrintDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        this.context = context;
        Window window = getWindow();
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
        setContentView(R.layout.dialog_print);
        initView();
    }

    private void initView() {
        notice_tv = findViewById(R.id.notice_tv);
        cancel_tv = findViewById(R.id.cancel_tv);
        ensure_tv = findViewById(R.id.ensure_tv);

        cancel_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ensure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.select();
                }
                dismiss();
            }
        });
    }

    public void setNotice(String notice) {
        this.notice = notice;
        if (notice_tv != null){
            notice_tv.setText(notice);
        }
    }

    public interface Event {
        void select();
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
