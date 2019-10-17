package com.deelock.wifilock.model

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.deelock.wifilock.entity.LockDetail
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.ui.activity.MainActivity
import com.deelock.wifilock.utils.SPUtil
import com.google.gson.Gson
import java.util.HashMap

/**
 * Created by forgive for on 2018\6\29 0029.
 */
class HsDetailModel(private var sdlId: String) {

    private lateinit var detailListener: (LockDetail) -> Unit
    private lateinit var nameListener: (String) -> Unit

    fun getDetail(context: Context) {
        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["uid"] = SPUtil.getUid(context)
        params["pid"] = sdlId
        RequestUtils.request(RequestUtils.LOCK_DETAIL_INFO, context, params).enqueue(object : ResponseCallback<BaseResponse>(context as Activity) {
            override fun onSuccess(code: Int, content: String) {
                var lockDetail = Gson().fromJson(content, LockDetail::class.java)
                detailListener(lockDetail)
            }
        })
    }

    fun deleteDevice(context: Context, deviceId: String) {
        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["token"] = SPUtil.getToken(context)
        params["uid"] = SPUtil.getUid(context)
        params["pid"] = deviceId
        params["type"] = "A00"
        RequestUtils.request(RequestUtils.UNBIND, context, params).enqueue(object : ResponseCallback<BaseResponse>(context as Activity) {
            override fun onSuccess(code: Int, content: String) {
                if (code == 1) {
                    Toast.makeText(context, "设备已解除绑定", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context,
                            MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                }
            }

            override fun onFailure(code: Int, message: String?) {
                super.onFailure(code, message)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun updateName(context: Context, name: String) {
        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["uid"] = SPUtil.getUid(context)
        params["pid"] = sdlId
        params["nickName"] = name
        RequestUtils.request(RequestUtils.UPDATE_LOCK, context, params).enqueue(object : ResponseCallback<BaseResponse>(context as Activity) {
            override fun onSuccess(code: Int, content: String) {
                if (code > 0) {
                    nameListener(name)
                }
            }
        })
    }

    fun onLoadDetail(detailListener: (LockDetail) -> Unit) {
        this.detailListener = detailListener
    }

    fun onUpdateName(nameListener: (String) -> Unit) {
        this.nameListener = nameListener
    }
}