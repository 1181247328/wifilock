package com.deelock.wifilock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2018\5\18 0018.
 */
public class PushDialog extends Dialog {

    private CheckBox open;
    private CheckBox close;
    private TextView cancel_tv;
    private TextView ensure_tv;

    public PushDialog(@NonNull Context context) {
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
        super.onCreate(savedInstanceState);setContentView(R.layout.dialog_push);
        initView();
    }

    private void initView() {
        open = findViewById(R.id.open_cb);
        close = findViewById(R.id.close_cb);
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
                dismiss();
                if (event != null){
                    event.ensure(open.isChecked(), close.isChecked());
                }
            }
        });
    }

    public interface Event{
        void ensure(boolean open, boolean close);
    }

    private Event event;

    public void setEvent(Event event){
        this.event = event;
    }
}
