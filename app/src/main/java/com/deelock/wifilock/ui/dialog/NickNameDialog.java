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

public class NickNameDialog extends Dialog {

    EditText nick_name_et;

    public NickNameDialog(@NonNull Context context, @StyleRes int themeResId) {
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
        setContentView(R.layout.dialog_nick_name);
        initView();
    }

    private void initView() {
        nick_name_et = findViewById(R.id.nick_name_et);
        nick_name_et.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) &&
                                    !Character.toString(source.charAt(i)).equals("_")) {
                                return "";
                            }
                        }
                        return null;
                    }
                }, new InputFilter.LengthFilter(16)
        });

        findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        findViewById(R.id.ensure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nick_name_et.getText().toString();
                if (TextUtils.isEmpty(name)){
                    return;
                }
                if (event != null){
                    event.ensure(name);
                }
                dismiss();
            }
        });
    }

    public interface Event{
        void ensure(String name);
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
