package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.deelock.wifilock.R;

/**
 * Created by binChuan on 2017\10\23 0023.
 */

public class InputPhoneDialog extends Dialog {

    EditText inputPhone;

    public InputPhoneDialog(@NonNull Context context, @StyleRes int themeResId) {
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
        setContentView(R.layout.dialog_input_phone);
        initView();
    }

    private void initView() {
        inputPhone = findViewById(R.id.input_phone_et);


        findViewById(R.id.input_phone_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.input_phone_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = inputPhone.getText().toString();
                if (TextUtils.isEmpty(phone)){
                    return;
                }
                if (event != null){
                    event.ensure(phone);
                }
                dismiss();
            }
        });
    }

    public interface Event{
        void ensure(String phone);
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
