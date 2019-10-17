package com.deelock.wifilock.ui.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.deelock.wifilock.R;

public class ADActivity extends BaseActivity {

    private ImageView back;
    private WebView webView;
    private ProgressBar progressBar;
    private Uri uri;

    @Override
    protected void bindActivity() {
        setContentView(R.layout.activity_ad);
    }

    @Override
    protected void doBusiness() {
        back = f(R.id.ad_back);
        webView = f(R.id.ad_view);
        progressBar = f(R.id.ad_progress);
        uri = getIntent().getData();
        if (uri != null) {
            webView.loadUrl(uri.toString());
        }
        webView.setWebViewClient(viewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected void setEvent() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(webView.canGoBack()){
                    webView.goBack();
                }else {
                    setResult(10);
                    finish();
                }
            }
        });
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }
    };

    private WebViewClient viewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }
}
