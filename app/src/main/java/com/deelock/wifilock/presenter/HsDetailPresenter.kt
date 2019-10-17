package com.deelock.wifilock.presenter

import android.content.Context
import android.content.Intent
import com.deelock.wifilock.R
import com.deelock.wifilock.entity.LockDetail
import com.deelock.wifilock.model.HsDetailModel
import com.deelock.wifilock.ui.activity.AboutHardwareActivity
import com.deelock.wifilock.ui.activity.ChangeWifiActivity
import com.deelock.wifilock.ui.activity.HsAllActivity
import com.deelock.wifilock.ui.activity.HsRecordActivity
import com.deelock.wifilock.ui.dialog.NickNameDialog

/**
 * Created by forgive for on 2018\6\29 0029.
 */
class HsDetailPresenter(private var context: Context, private var sdlId: String) {

    private var model: HsDetailModel = HsDetailModel(sdlId)
    private lateinit var nameListener: (String) -> Unit
    private lateinit var wifiListener: (String) -> Unit

    private var nickNameDialog: NickNameDialog
    private lateinit var detail: LockDetail

    init {
        model.onLoadDetail {
            detail = it
            nameListener(it.nickName)
            wifiListener(it.ssid)
        }

        model.onUpdateName { nameListener(it) }

        nickNameDialog = NickNameDialog(context, R.style.dialog)
        nickNameDialog.setEvent {
            model.updateName(context, it)
        }
    }

    fun showNameDialog() {
        nickNameDialog.show()
    }

    fun toHardwarePage() {
        val intent = Intent(context, AboutHardwareActivity::class.java)
        intent.putExtra("id", detail.pid)
        intent.putExtra("hard", detail.hardVersion)
        intent.putExtra("soft", detail.softVersion)
        context.startActivity(intent)
    }

    fun toUpdateWifiPage() {
        val intent = Intent(context, ChangeWifiActivity::class.java)
        intent.putExtra("ssid", detail.ssid)
        intent.putExtra("type", "A00")
        context.startActivity(intent)
    }

    fun toAllPage() {
        val intent = Intent(context, HsAllActivity::class.java)
        intent.putExtra("sdlId", sdlId)
        context.startActivity(intent)
    }

    fun toRecordPage() {
        val intent = Intent(context, HsRecordActivity::class.java)
        intent.putExtra("sdlId", sdlId)
        context.startActivity(intent)
    }

    fun getDetail() {
        model.getDetail(context)
    }

    fun deleteDevice() {
        model.deleteDevice(context, sdlId)
    }

    fun setNameListener(nameListener: (String) -> Unit) {
        this.nameListener = nameListener
    }

    fun setWiFiListener(wifiListener: (String) -> Unit) {
        this.wifiListener = wifiListener
    }
}