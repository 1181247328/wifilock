package com.deelock.wifilock.ui.activity

import android.content.ClipData
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.deelock.wifilock.R
import android.content.ClipboardManager
import android.content.Context
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.utils.SPUtil
import com.deelock.wifilock.utils.ToastUtil
import java.util.HashMap


/**
 * Created by forgive for on 2018\6\28 0028.
 */
class HsPwdResultActivity: BaseActivity() {


    //widget
    private lateinit var backIb: ImageButton
    private lateinit var pwdTv: TextView
    private lateinit var copyPwdIv: ImageButton
    private lateinit var verifyTv: TextView
    private lateinit var copyVerifyIv: ImageButton
    private lateinit var succeedBtn: Button

    private var pwd = ""
    private var verify = ""
    private var sdlId = ""
    private var type = 0
    private var timeBegin = 0L
    private var timeEnd = 0L
    private var week = ""

    override fun bindActivity() {
        setContentView(R.layout.activity_hs_pwd_result)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        pwdTv = f(R.id.pwdTv)
        copyPwdIv = f(R.id.copyPwdIv)
        verifyTv = f(R.id.verifyTv)
        copyVerifyIv = f(R.id.copyVerifyIv)
        succeedBtn = f(R.id.succeedBtn)
    }

    override fun doBusiness() {
        pwd = intent.getStringExtra("pwd")
        verify = intent.getStringExtra("verify")
        sdlId = intent.getStringExtra("sdlId")
        type = intent.getIntExtra("type", 0)
        timeBegin = intent.getLongExtra("timeBegin", 0)
        timeEnd = intent.getLongExtra("timeEnd", 0)

        pwdTv.text = pwd
        verifyTv.text = verify
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }

        copyPwdIv.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText("Label", pwd)
            // 将ClipData内容放到系统剪贴板里。
            cm.primaryClip = mClipData
            ToastUtil.toastLong(this, "复制成功")
        }

        copyVerifyIv.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData = ClipData.newPlainText("Label", verify)
            // 将ClipData内容放到系统剪贴板里。
            cm.primaryClip = mClipData
            ToastUtil.toastLong(this, "复制成功")
        }

        succeedBtn.setOnClickListener {
            val params = HashMap<String, Any>()
            params["timestamp"] = TimeUtil.getTime()
            params["uid"] = SPUtil.getUid(this)
            params["sdlId"] = sdlId
            params["type"] = type
            params["pwd"] = pwd
            params["authId"] = "00000000000000000000000000000000"
            params["timeBegin"] = timeBegin
            params["timeEnd"] = timeEnd
            if (!week.isEmpty()){
                params["week"] = week
            }
            RequestUtils.request(RequestUtils.ADD_HS_PWD, this, params).enqueue(object : ResponseCallback<BaseResponse>(this) {
                override fun onSuccess(code: Int, content: String) {
                    if (code > 0){
                        ToastUtil.toastLong(this@HsPwdResultActivity, "下发成功")
                    }
                    finish()
                }
            })
        }
    }
}