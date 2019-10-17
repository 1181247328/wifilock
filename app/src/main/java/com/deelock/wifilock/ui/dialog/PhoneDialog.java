package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.deelock.wifilock.R;

public class PhoneDialog extends Dialog {

    private PhoneClick phoneClick;

    public PhoneDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_callphone);

        Button click = findViewById(R.id.callphone_click);
        final Button cancel = findViewById(R.id.callphone_cancel);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneClick.callPhone();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setPhoneClick(PhoneClick click) {
        this.phoneClick = click;
    }

    public interface PhoneClick {
        void callPhone();
    }
}
