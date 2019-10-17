package com.deelock.wifilock.ui.dialog

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.WindowManager
import com.deelock.wifilock.R

/**
 * Created by forgive for on 2018\6\20 0020.
 */
class SelectDateDialog: AppCompatDialog {

    constructor(context: Context) : super(context, R.style.dialog) {
        val window = window
        window!!.decorView.setPadding(0, 0, 0, 0)
        val lp = window.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp
        window.setGravity(Gravity.CENTER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_select_date)
        initView()
    }

    private fun initView(){

    }
}