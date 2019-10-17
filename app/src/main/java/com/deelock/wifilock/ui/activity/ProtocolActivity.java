package com.deelock.wifilock.ui.activity;

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2018\1\3 0003.
 */

public class ProtocolActivity extends BaseActivity {

    private WebView web;
    private ImageButton back_ib;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_protocol);
    }

    @Override
    protected void doBusiness() {
        web = findViewById(R.id.web);
        back_ib = findViewById(R.id.back_ib);
        back_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        web.loadUrl("file:///android_asset/protocol.html");
    }

    @Override
    protected void requestData() {

    }

    @Override
    protected void setEvent() {

    }
}
