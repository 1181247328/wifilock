package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.LinearLayout;

import com.deelock.wifilock.utils.DensityUtil;

/**
 * Created by binChuan on 2017\9\29 0029.
 */

public class HistoryRecyclerView extends RecyclerView {

    VelocityTracker mvt;

    int itemWidth;
    int dateWidth;
    int deleteIconWidth;

    float scrollSpeed;

    int downX;
    int downY;

    int nowX;
    int nowY;

    int diff;
    int scroll;

    ScrollItem currentItem;
    ScrollItem lastItem;
    
    int moveState = 0;
    final int STATE_START = 0;
    final int STATE_WAIT = 1;
    final int STATE_HORIZONTAL = 2;
    final int STATE_VERTICAL = 3;
    final int STATE_STRETCHED = 4;
    float speed = 0f;

    public HistoryRecyclerView(Context context) {
        this(context, null);
    }

    public HistoryRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HistoryRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int screenWidth = DensityUtil.getScreenWidth(context);
        itemWidth = screenWidth * 140 / 750;
        dateWidth = screenWidth * 88 / 750;
        scrollSpeed = screenWidth / 3.6f;
        init();
    }

    private void init() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mvt == null){
            mvt = VelocityTracker.obtain();
        }
        mvt.addMovement(e);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return performActionDown(e);
            case MotionEvent.ACTION_MOVE:
                return performActionMove(e);
            case MotionEvent.ACTION_UP:
                return performActionUp(e);
            case MotionEvent.ACTION_CANCEL:
                releaseVelocityTracker();
                break;
        }
        return super.onTouchEvent(e);
    }

    private boolean performActionDown(MotionEvent e) {
        downX = (int) e.getX();
        downY = (int) e.getY();
        LinearLayout l = (LinearLayout) findChildViewUnder(downX, downY);
        if (l == null){
            if (lastItem != null){
                lastItem.scroll(0);
            }
            currentItem = null;
            return super.onTouchEvent(e);
        }
        try {
            currentItem = (ScrollItem) l.getChildAt(0);
        } catch (Exception ee){
            return false;
        }


        if (currentItem == null){
            return true;
        }

        loadParam();

        if (!isLastChild()){
            lastItem.scroll(0);
            lastItem.postInvalidate();
        }
        return true;
    }

    /**
     *
     * 执行滑动处理
     * @param e
     * @return
     */
    private boolean performActionMove(MotionEvent e) {
        nowX = (int) e.getX();
        nowY = (int) e.getY();

        if (moveState == STATE_START || moveState == STATE_WAIT){
            int dx = downX - nowX;
            int dy = downY - nowY;

            if (diff > 0){
                moveState = STATE_STRETCHED;
            } else if (Math.abs(dx) < 20 && Math.abs(dy) < 20) {
                moveState = STATE_WAIT;
            } else if (Math.abs(dx) > Math.abs(dy)){
                moveState = STATE_HORIZONTAL;
                return true;
            } else {
                if (lastItem != null){
                    lastItem.scroll(0);
                }
                if (currentItem != null){
                    currentItem.scroll(0);
                }
                moveState = STATE_VERTICAL;
            }
        }

        if (moveState == STATE_VERTICAL){
            return super.onTouchEvent(e);
        }

        scroll = downX - nowX + diff;

        if (scroll > deleteIconWidth){
            scroll = deleteIconWidth;
            downX = nowX + deleteIconWidth;
            diff = 0;
        } else if (scroll < 0){
            scroll = 0;
            downX = nowX;
            diff = 0;
        }
        if (currentItem != null){
            currentItem.scroll(scroll);
        }
        return true;
    }

    /**
     *
     * 执行手指抬起处理
     * @param e
     * @return
     */
    private boolean performActionUp(MotionEvent e) {
        final VelocityTracker vt = mvt;
        vt.computeCurrentVelocity(1000);
        speed = vt.getXVelocity();
        if (moveState == STATE_VERTICAL){
            moveState = STATE_START;
            return super.onTouchEvent(e);
        }

        moveState = STATE_START;
        lastItem = currentItem;
//        速度达到临界值、未达到右边界或滑动距离过半     往左滑动
        if (lastItem == null){
            return true;
        }
        if (speed > scrollSpeed && scroll < deleteIconWidth){
            lastItem.scrollSmoothly(scroll, -scroll);
            return true;
        }

//        速度达到临界值、未达到左边界或滑动距离未过半
        if (speed < -scrollSpeed && scroll > 0){
            lastItem.scrollSmoothly(scroll, deleteIconWidth - scroll);
            return true;
        }

        if (scroll < deleteIconWidth / 2){
            lastItem.scrollSmoothly(scroll, -scroll);
        } else {
            lastItem.scrollSmoothly(scroll, deleteIconWidth - scroll);
        }
        return true;
    }

    private void releaseVelocityTracker() {
        if (mvt != null) {
            mvt.recycle();
            mvt = null;
        }
    }

    /**
     * 读取参数
     */
    private void loadParam(){
        if (currentItem.getType() == 1){
            deleteIconWidth = itemWidth;
        } else {
            deleteIconWidth = dateWidth;
        }
        diff = currentItem.getCurrX();
    }

    public void resetItem(){
        if (lastItem != null){
            lastItem.scroll(0);
        }
        if (currentItem != null){
            currentItem.scroll(0);
        }
    }

    /**
     *
     * 判断是否为上次的item
     * @return
     */
    private boolean isLastChild(){
        if (lastItem != null && currentItem != lastItem){
            return false;
        }
        return true;
    }
}
