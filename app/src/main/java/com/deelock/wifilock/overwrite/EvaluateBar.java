package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.deelock.wifilock.R;

/**
 * Created by forgive for on 2018\1\30 0030.
 */

public class EvaluateBar extends LinearLayout {

    private int viewWidth;
    private int starCount = 0;
    private int lastCount = 0;

    public EvaluateBar(Context context) {
        this(context, null);
    }

    public EvaluateBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EvaluateBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            starCount = (int) (event.getX() * 4 / viewWidth + 0.5f);
            if (starCount != lastCount){
                lastCount = starCount;
                if (starEvent != null){
                    starEvent.selectedNum(lastCount);
                }
                for (int i = 0; i < 4; i++){
                    ImageView v = (ImageView) getChildAt(i);
                    if (i < lastCount){
                        v.setImageResource(R.mipmap.solid_star);
                    } else {
                        v.setImageResource(R.mipmap.empty_star);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public interface StarEvent{
        void selectedNum(int num);
    }

    private StarEvent starEvent;

    public void setStarEvent(StarEvent starEvent){
        this.starEvent = starEvent;
    }
}
