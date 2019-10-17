package com.deelock.wifilock.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * Created by forgive for on 2018\7\3 0003.
 */
class TabScrip: LinearLayout {

    var scroller: Scroller
    var curX = 0

    constructor(context: Context): this(context, null){

    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        scroller = Scroller(context)
    }

    fun scroll(last: Int, x: Int, time: Int){
        scroller.forceFinished(true)
        scroller.startScroll(last, 0, x, 0, time)
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            curX = scroller.currX
            scrollTo(curX, 0)
            invalidate()
        }
    }
}