package com.deelock.wifilock.model

import android.app.Activity
import android.content.Context
import com.deelock.wifilock.contract.HsCreateContract
import com.deelock.wifilock.entity.Key
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.utils.SPUtil
import com.google.gson.Gson
import java.util.HashMap

/**
 * Created by forgive for on 2018\6\20 0020.
 */
class HsHsCreateModel(private var model: HsCreateContract.HsCreateModelImpl):
        HsCreateContract.IHsCreateModel{

    var key = 0

    override fun getKey(context: Context, pid: String) {
        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["uid"] = SPUtil.getUid(context)
        params["pid"] = pid
        RequestUtils.request(RequestUtils.GET_KEY, context, params).enqueue(object : ResponseCallback<BaseResponse>(context as Activity) {
            override fun onSuccess(code: Int, content: String) {
                var k = Gson().fromJson(content, Key::class.java)!!
                key = Integer.parseInt(k.devKey, 16)
            }
        })
    }

    override fun addHomeStayPwd(context: Context, sdlId: String, type: Int, pwd: String, authId: String, timeBegin: Long) {

    }
}