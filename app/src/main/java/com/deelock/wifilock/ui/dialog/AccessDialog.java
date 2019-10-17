package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.overwrite.InputCodeView;

/**
 * Created by forgive for on 2018\7\27 0027.
 */
public class AccessDialog extends Dialog {

    private InputCodeView inputCode;
    private TextView ensure;
    private TextView cancel;
    private Context context;

    public AccessDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        assert window != null;
        this.context = context;
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
        setContentView(R.layout.dialog_access_xinjia);
        initView();
    }

    private void initView() {
        inputCode = findViewById(R.id.access_et);
        ensure = findViewById(R.id.access_ensure);
        cancel = findViewById(R.id.access_cancel);

        ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (event != null) {
                    event.ensure(inputCode.getInputCode());
                    inputCode.clearInputCode();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (event != null) {
                    event.cancel();
                    inputCode.clearInputCode();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        View view = getCurrentFocus();
        InputMethodManager inputManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }catch (Exception e){

        }
        super.dismiss();
    }

    public interface Event {
        void ensure(String inputContent);

        void cancel();
    }

    private Event event;

    public void setEvent(Event event) {
        this.event = event;
    }
}
