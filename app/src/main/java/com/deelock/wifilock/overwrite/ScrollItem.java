package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by binChuan on 2017\9\29 0029.
 */

public class ScrollItem extends RelativeLayout {

    Scroller scroller;
    Context context;
    int currX;
    int type;

    final int scrollTime = 400;

    public ScrollItem(Context context) {
        this(context, null);
    }

    public ScrollItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        scroller = new Scroller(context);
        this.context = context;
    }

    public void scrollSmoothly(int startX, int dx){
        scroller.forceFinished(true);
        scroller.startScroll(startX, 0, dx, 0, scrollTime);
        invalidate();
    }

    public void scroll(int x){
        scroller.forceFinished(true);
        currX = x;
        scrollTo(x, 0);
        postInvalidate();
    }

    public int getCurrX() {
        return currX;
    }

    public int getType() {
        if (getChildCount() < 3){
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            currX = scroller.getCurrX();
            scrollTo(currX, 0);
            invalidate();
        }
    }
}
