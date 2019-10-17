package com.deelock.wifilock.ui.activity

import android.widget.*
import com.deelock.wifilock.R
import com.deelock.wifilock.presenter.HsDetailPresenter

/**
 * Created by forgive for on 2018\6\19 0019.
 */
class HsDetailActivity : BaseActivity() {


    //widget
    private lateinit var backIb: ImageButton
    private lateinit var nameRl: RelativeLayout
    private lateinit var nameTv: TextView
    private lateinit var aboutRl: RelativeLayout
    private lateinit var changeWifiRl: RelativeLayout
    private lateinit var wifiTv: TextView
    private lateinit var allRl: RelativeLayout
    private lateinit var recordRl: RelativeLayout
    private lateinit var deleteLl: LinearLayout

    private lateinit var presenter: HsDetailPresenter

    override fun bindActivity() {
        setContentView(R.layout.activity_homestay_detail)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        nameRl = f(R.id.nameRl)
        nameTv = f(R.id.nameTv)
        aboutRl = f(R.id.aboutRl)
        changeWifiRl = f(R.id.changeWifiRl)
        wifiTv = f(R.id.wifiTv)
        allRl = f(R.id.allRl)
        recordRl = f(R.id.recordRl)
        deleteLl = f(R.id.deleteLl)
    }

    override fun doBusiness() {
        presenter = HsDetailPresenter(this, intent.getStringExtra("sdlId"))
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }

        nameRl.setOnClickListener { presenter.showNameDialog() }

        aboutRl.setOnClickListener { presenter.toHardwarePage() }

        changeWifiRl.setOnClickListener { presenter.toUpdateWifiPage() }

        allRl.setOnClickListener { presenter.toAllPage() }

        recordRl.setOnClickListener { presenter.toRecordPage() }

        deleteLl.setOnClickListener { presenter.deleteDevice() }
    }

    override fun setCallBack() {
        presenter.setNameListener { nameTv.text = it }
        presenter.setWiFiListener { wifiTv.text = it }
    }

    override fun onResume() {
        super.onResume()
        presenter.getDetail()
    }
}