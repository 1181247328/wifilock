package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.entity.User;
import com.deelock.wifilock.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by forgive for on 2018\3\9 0009.
 */

public class MoveDialog extends Dialog {

    List<User> data;
    Context context;
    int height;
    float textSize;
    LinearLayout ll;
    TextView notice_title_tv;
    ImageView back_iv;
    private boolean isUpdated;

    public MoveDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        this.context = context;
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        height = 90 * DensityUtil.getScreenHeight(context) / 1334;
        textSize = 34 * DensityUtil.getScreenWidth(context) / 750;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER);
        data = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_move);
        initView();
    }

    public void setData(final List<User> data){
        this.data = new ArrayList<>();
        this.data.addAll(data);
        if (ll == null){
            return;
        }

        updateUi();
    }

    private void initView() {
        ll = findViewById(R.id.ll);
        back_iv = findViewById(R.id.back_iv);
        notice_title_tv = findViewById(R.id.notice_title_tv);
        if (data.size() > 0){
            updateUi();
        }

        back_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void updateUi(){
        for (int i = 0; i < data.size(); i++) {
            TextView v = new TextView(context);
            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            v.setGravity(Gravity.CENTER);
            v.setTextColor(0xff999999);
            v.setTextSize(0, textSize);
            v.setText(data.get(i).getName());
            final int finalI = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (event == null){
                        return;
                    }
                    event.select(finalI);
                    dismiss();
                }
            });
            ll.addView(v);
        }
        isUpdated = true;
    }

    public interface Event{
        void select(int p);
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
