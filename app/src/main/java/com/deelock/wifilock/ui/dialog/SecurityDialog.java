package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.widget.JustifyTextView;

/**
 * Created by forgive for on 2018\6\6 0006.
 */
public class SecurityDialog extends Dialog {

    private String title = "";
    private String content = "";

    private TextView phoneTv;
    private TextView contentTv;
    private TextView cancelTv;
    private TextView ensureTv;

    public SecurityDialog(@NonNull Context context) {
        super(context, R.style.dialog);
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
        setContentView(R.layout.dialog_security);
        initView();
    }

    public void setContent(String title, String content){
        this.title = title;
        this.content = content;
    }

    private void initView() {
        phoneTv = findViewById(R.id.phoneTv);
        contentTv = findViewById(R.id.contentTv);
        cancelTv = findViewById(R.id.cancelTv);
        ensureTv = findViewById(R.id.ensureTv);

        if (!TextUtils.isEmpty(title)){
            phoneTv.setText(title);
        }
        if (!TextUtils.isEmpty(content)){
            contentTv.setText(content);
        }

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ensureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.ensure();
                }
                dismiss();
            }
        });
    }

    public interface Event{
        void ensure();
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
