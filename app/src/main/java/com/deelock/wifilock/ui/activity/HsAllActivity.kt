package com.deelock.wifilock.ui.activity

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.deelock.wifilock.R
import com.deelock.wifilock.entity.PasswordList
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.utils.SPUtil
import com.google.gson.Gson
import java.util.HashMap

/**
 * Created by forgive for on 2018\6\28 0028.
 */
class HsAllActivity: BaseActivity() {


    //widget
    private lateinit var backIb: ImageButton
    private lateinit var landlordRl: RelativeLayout
    private lateinit var landlordTv: TextView
    private lateinit var customRl: RelativeLayout
    private lateinit var customTv: TextView
    private lateinit var typeIv: ImageView


    private lateinit var sdlId: String

    override fun bindActivity() {
        setContentView(R.layout.activity_hs_all)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        landlordRl = f(R.id.landlordRl)
        landlordTv = f(R.id.landlordTv)
        customRl = f(R.id.customRl)
        customTv = f(R.id.customTv)
        typeIv = f(R.id.typeIv)
    }

    override fun doBusiness() {
        sdlId = intent.getStringExtra("sdlId")

        val params = HashMap<String, Any>()
        params["timestamp"] = TimeUtil.getTime()
        params["uid"] = SPUtil.getUid(this)
        params["sdlId"] = sdlId
        RequestUtils.request(RequestUtils.ALL_PASSWORD, this, params).enqueue(object : ResponseCallback<BaseResponse>(this) {
            override fun onSuccess(code: Int, content: String) {
                val landlord = Gson().fromJson(content, PasswordList::class.java).userPasswords
                val custom = Gson().fromJson(content, PasswordList::class.java).tempPasswords

                if (!landlord.isEmpty()){
                    landlordRl.visibility = View.VISIBLE
                    landlordTv.text = landlord[0].openName
                }

                if (!custom.isEmpty()){
                    customRl.visibility = View.VISIBLE
                    customTv.text = custom[0].openName

                    when(custom[0].type){
                        0 -> typeIv.setImageResource(R.mipmap.password_once)
                        1 -> typeIv.setImageResource(R.mipmap.time_password)
                        2 -> typeIv.setImageResource(R.mipmap.password_loop)
                    }
                }
            }
        })
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }
    }
}