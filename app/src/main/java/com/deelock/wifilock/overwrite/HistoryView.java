package com.deelock.wifilock.overwrite;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * Created by Administrator on 2017\11\2 0002.
 */

public class HistoryView extends PullToRefreshBase<HistoryRecyclerView> {

    HistoryRecyclerView recyclerView;

    public HistoryView(Context context) {
        super(context);
    }

    public HistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HistoryView(Context context, Mode mode) {
        super(context, mode);
    }

    public HistoryView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }

    public RecyclerView.ItemAnimator getItemAnimator(){
        return recyclerView.getItemAnimator();
    }

    /**
     * 重置item
     */
    @Override
    public void resetItem() {
        recyclerView.resetItem();
    }

    @Override
    public final Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected HistoryRecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        recyclerView = new HistoryRecyclerView(context, attrs);
//        recyclerView.setId(com.handmark.pulltorefresh.library.R.id.recycleview);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullEnd() {
        return isLastItemVisible();
    }

    /**
     * @return boolean:
     * @Description: 判断第一个条目是否完全可见
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private boolean isFirstItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以下拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
//            if (DEBUG) {
//                Log.d(LOG_TAG, "isFirstItemVisible. Empty View.");
//            }
            return true;

        } else {
            // 第一个条目完全展示,可以刷新
            if (getFirstVisiblePosition() == 0) {
                return getRefreshableView().getChildAt(0).getTop() >= getRefreshableView()
                        .getTop();
            }
        }

        return false;
    }

    /**
     * @return int: 位置
     * @Description: 获取第一个可见子View的位置下标
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private int getFirstVisiblePosition() {
        View firstVisibleChild = getRefreshableView().getChildAt(0);
        return firstVisibleChild != null ? getRefreshableView()
                .getChildAdapterPosition(firstVisibleChild) : -1;
    }

    /**
     * @return boolean:
     * @Description: 判断最后一个条目是否完全可见
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private boolean isLastItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        // 如果未设置Adapter或者Adapter没有数据可以上拉刷新
        if (null == adapter || adapter.getItemCount() == 0) {
//            if (DEBUG) {
//                Log.d(LOG_TAG, "isLastItemVisible. Empty View.");
//            }
            return true;

        } else {
            // 最后一个条目View完全展示,可以刷新
            int lastVisiblePosition = getLastVisiblePosition();
            if (lastVisiblePosition >= getRefreshableView().getAdapter().getItemCount() - 1) {
                return getRefreshableView().getChildAt(
                        getRefreshableView().getChildCount() - 1).getBottom() <= getRefreshableView()
                        .getBottom();
            }
        }
        return false;
    }

    /**
     * @return int: 位置
     * @Description: 获取最后一个可见子View的位置下标
     * @version 1.0
     * @date 2015-9-23
     * @Author zhou.wenkai
     */
    private int getLastVisiblePosition() {
        View lastVisibleChild = getRefreshableView().getChildAt(getRefreshableView()
                .getChildCount() - 1);
        return lastVisibleChild != null ? getRefreshableView()
                .getChildAdapterPosition(lastVisibleChild) : -1;
    }


    public void setAdapter(RecyclerView.Adapter adapter) {
        getRefreshableView().setAdapter(adapter);
    }

    public View findChildViewUnder(float x, float y){
        return getRefreshableView().findChildViewUnder(x, y);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        getRefreshableView().setLayoutManager(layoutManager);
    }

//    mRecyclerView.setHasFixedSize(true)

    public void setHasFixedSize(boolean hasFixedSize) {
        getRefreshableView().setHasFixedSize(hasFixedSize);
    }
}
