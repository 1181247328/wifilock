package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by binChuan on 2017\10\11 0011.
 */

public class MessageItem extends RelativeLayout {

    Scroller scroller;
    int screenWidth;
    int maxWidth;

    public MessageItem(Context context) {
        this(context, null);
    }

    public MessageItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        screenWidth = DensityUtil.getScreenWidth(context);
        maxWidth = screenWidth - 60 * screenWidth / 750;
    }

    public void scrollSmoothly(int time){
        scroller.startScroll(0, 0, maxWidth, 0, time);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), 0);
        }
    }
}
