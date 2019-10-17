package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by forgive for on 2017\12\22 0022.
 */

public class PasswordDialog extends Dialog {

    TextView password_tv;

    String password;

    public PasswordDialog(@NonNull Context context, @StyleRes int themeResId) {
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
        setContentView(R.layout.dialog_password);
        initView();
    }

    public void setPassword(String password){
        this.password = password;
    }

    private void initView() {
        password_tv = (TextView) findViewById(R.id.password_tv);
        int screenWidth = DensityUtil.getScreenWidth(getContext());
        int size1 = 50 * screenWidth / 750;
        AbsoluteSizeSpan as = new AbsoluteSizeSpan(size1);
        SpannableStringBuilder sb = new SpannableStringBuilder("#"+password+"#");
        sb.setSpan(as, 1, 7, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(0xff999999), 1, 7, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        password_tv.setText(sb);

        findViewById(R.id.ensure_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event != null){
                    event.ensure();
                }
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
