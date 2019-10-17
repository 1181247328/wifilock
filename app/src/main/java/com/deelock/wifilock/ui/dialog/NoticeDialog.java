package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2017\12\21 0021.
 */

public class NoticeDialog extends Dialog {

    TextView notice_tv;
    ImageView icon_iv;

    String str;
    int id;

    public NoticeDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
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
        setContentView(R.layout.dialog_notice);
        initView();
    }

    public void setNotice(String str){
        this.str = str;
    }

    private void initView() {
        notice_tv = findViewById(R.id.notice_tv);
        notice_tv.setText(str);

        findViewById(R.id.ensure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.delete();
                }
                dismiss();
            }
        });
    }

    public interface Event{
        void delete();
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
