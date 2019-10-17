package com.deelock.wifilock.ui.activity

import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageButton
import com.deelock.wifilock.R
import com.deelock.wifilock.adapter.HsRecordAdapter
import com.deelock.wifilock.entity.HsRecord
import com.deelock.wifilock.entity.HsRecordList
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.overwrite.HistoryView
import com.deelock.wifilock.utils.SPUtil
import com.google.gson.Gson
import java.util.HashMap

/**
 * Created by forgive for on 2018\6\29 0029.
 */
class HsRecordActivity: BaseActivity() {


    //widget
    private lateinit var backIb: ImageButton
    private lateinit var rv: HistoryView


    private lateinit var adapter: HsRecordAdapter

    override fun bindActivity() {
        setContentView(R.layout.activity_hs_record)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        rv = f(R.id.rv)
    }

    override fun doBusiness() {

    }

    override fun requestData() {
        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["uid"] = SPUtil.getUid(this)
        params["sdlId"] = intent.getStringExtra("sdlId")
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params).enqueue(object : ResponseCallback<BaseResponse>(this) {
            override fun onSuccess(code: Int, content: String) {
                var receivedData = Gson().fromJson(content, HsRecordList::class.java).list
                var rec = HsRecord()
                var rec1 = HsRecord()
                rec1.openName = "123"
                rec.isTitle = 1
                rec.month = "1234"
                receivedData.add(rec)
                receivedData.add(rec1)
                receivedData.add(rec1)

                if (receivedData.isEmpty()){
                    return
                }

                val data = getData(receivedData)
                adapter = HsRecordAdapter(receivedData)
                rv.setAdapter(adapter)
                rv.setLayoutManager(LinearLayoutManager(this@HsRecordActivity))
            }
        })
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }
    }

    private fun getData(list: List<HsRecord>): List<HsRecord>{
        var data = mutableListOf<HsRecord>()
        var temp = 0L

        list.forEach {

        }

        return data
    }
}