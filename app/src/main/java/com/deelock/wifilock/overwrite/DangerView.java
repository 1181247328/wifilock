package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by forgive for on 2018\5\3 0003.
 */
public class DangerView extends RelativeLayout {

    private Scroller scroller;
    private RelativeLayout.LayoutParams lp;
    private boolean expanded = true;
    private int minHeight;
    private int maxHeight;

    public DangerView(Context context) {
        this(context, null);
    }

    public DangerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DangerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        int totalHeight = DensityUtil.getScreenHeight(context);
        minHeight = 160 * totalHeight / 1334;
        maxHeight = 273 * totalHeight / 1334;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lp = (RelativeLayout.LayoutParams) getLayoutParams();
    }

    /**
     * 伸缩
     */
    public void retract() {
        scroller.forceFinished(true);
        if (expanded){
            scroller.startScroll(0, maxHeight, 0, minHeight - maxHeight, 1000);
        } else {
            scroller.startScroll(0, minHeight, 0, maxHeight - minHeight, 1000);
        }
        expanded = !expanded;
    }

    public boolean isExpanded(){
        return expanded;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            lp.height = scroller.getCurrY();
            setLayoutParams(lp);
        }
    }
}
