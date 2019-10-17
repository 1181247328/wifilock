package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by forgive for on 2017\12\1 0001.
 */

public class HistorySwipeRefreshLayout extends SwipeRefreshLayout {

    private float startY;
    private float startX;
    // 记录viewPager是否拖拽的标记
    private boolean mIsVpDragger;
    private final int mTouchSlop;

    int moveState = 0;
    final int STATE_START = 0;
    final int STATE_WAIT = 1;
    final int STATE_HORIZONTAL = 2;
    final int STATE_VERTICAL = 3;

    public HistorySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY = ev.getY();
                startX = ev.getX();
                // 初始化标记
                mIsVpDragger = false;
                return super.onInterceptTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                // 如果viewpager正在拖拽中，那么不拦截它的事件，直接return false；
                if(mIsVpDragger) {
                    return false;
                }

                // 获取当前手指位置
                float endY = ev.getY();
                float endX = ev.getX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);

                if (distanceX < 20 && distanceY < 20){
                    moveState = STATE_WAIT;
                } else if (distanceX > distanceY){
                    moveState = STATE_HORIZONTAL;
                    return false;
                } else {
                    moveState = STATE_VERTICAL;
                }

                // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
//                if(distanceX > mTouchSlop && distanceX > distanceY) {
//                    mIsVpDragger = true;
//                    return false;
//                }

                if (moveState == STATE_VERTICAL){
                    return super.onInterceptTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
                moveState = STATE_START;
                return false;
            case MotionEvent.ACTION_CANCEL:
                // 初始化标记
                mIsVpDragger = false;
                break;
        }
        // 如果是Y轴位移大于X轴，事件交给swipeRefreshLayout处理。
        return super.onInterceptTouchEvent(ev);
    }
}
