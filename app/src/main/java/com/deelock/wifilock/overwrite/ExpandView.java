package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by binChuan on 2017\9\20 0020.
 */

public class ExpandView extends RelativeLayout {

    private Scroller scroller;
    private int maxHeight;
    private int minHeight;
    private int dy;
    private LinearLayout.LayoutParams lp;
    private boolean expanded = true;
    private boolean first = true;
    private int currentY;
    private ImageButton ib;
    private boolean canScroll;

    public ExpandView(Context context) {
        this(context, null);
    }

    public ExpandView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildAt(2).getMeasuredHeight() != 0 && dy == 0 && !canScroll){
            ib = (ImageButton) getChildAt(1);
            dy = -getChildAt(2).getMeasuredHeight();
            maxHeight = getMeasuredHeight();
            minHeight = maxHeight + dy;
            lp = (LinearLayout.LayoutParams) getLayoutParams();
            canScroll = true;
        }
    }

    /**
     * 伸缩
     */
    public void retract() {
        if (first) {
            scroller.startScroll(0, maxHeight, 0, dy, 600);
            expanded = false;
            first = false;
            invalidate();
        } else if (expanded) {
            scroller.forceFinished(true);
            scroller.startScroll(0, currentY, 0, minHeight - currentY, 600);
            expanded = false;
            postInvalidate();
        } else {
            scroller.forceFinished(true);
            scroller.startScroll(0, currentY, 0, maxHeight - currentY, 600);
            expanded = true;
            postInvalidate();
        }
    }

    public boolean isExpanded(){
        return expanded;
    }

    @Override
    public void computeScroll() {
        if (canScroll){
            if (scroller.computeScrollOffset()) {
                lp.height = scroller.getCurrY();
                currentY = lp.height;
                ib.setRotation(180 - (currentY - minHeight) * 180 / (maxHeight - minHeight));
                setLayoutParams(lp);
            }
        }
    }
}
