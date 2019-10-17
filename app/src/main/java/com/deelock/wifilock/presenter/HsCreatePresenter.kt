package com.deelock.wifilock.presenter

import android.content.Context
import android.content.Intent
import android.util.Log
import com.deelock.wifilock.contract.HsCreateContract
import com.deelock.wifilock.model.HsHsCreateModel
import com.deelock.wifilock.ui.activity.HsPwdResultActivity
import com.deelock.wifilock.utils.XxUtil
import java.util.*

/**
 * Created by forgive for on 2018\6\22 0022.
 */
class HsCreatePresenter: HsCreateContract.HsCreateModelImpl {

    private var model: HsHsCreateModel = HsHsCreateModel(this)
    private var sdlId = ""
    private var context: Context

    constructor(context: Context, sdlId: String){
        this.context = context
        this.sdlId = sdlId
        model.getKey(context, sdlId)
    }

    /**
     * 生成验证码和密码
     * @param hour 起始时间hour
     * @param period 时长hour
     * @param loop 周期数
     */
    fun createResult(hour: Int, period: Int, loop: Int, type: Int, timeBegin: Long, timeEnd: Long, week: String) {
        var key = createKey()
        var verifyData = createVerifyData(hour, loop)
        var pwdData = createPwdData(period, loop)
        val eVerify = XxUtil.encrypt(verifyData, key)
        val ePwd = XxUtil.encrypt(pwdData, key)
        var verify = getResult(eVerify)
        var pwd = getResult(ePwd)

        var intent = Intent(context, HsPwdResultActivity::class.java)
        intent.putExtra("pwd", pwd)
        intent.putExtra("verify", verify)
        intent.putExtra("sdlId", sdlId)
        intent.putExtra("type", type)
        intent.putExtra("pwd", pwd)
        intent.putExtra("timeBegin", timeBegin)
        intent.putExtra("timeEnd", timeEnd)
        intent.putExtra("week", week)
        context.startActivity(intent)
    }

    /**
     * 生成秘钥
     */
    private fun createKey(): IntArray{
        val cal = Calendar.getInstance()
        var year = cal.get(GregorianCalendar.YEAR)
        var month = cal.get(GregorianCalendar.MONTH) + 1
        var day = cal.get(GregorianCalendar.DATE)

        year -= 2018
        val first = (year and 0xf) shl 4 + month
        val second = day
        val third = Integer.parseInt(sdlId.substring(12, 14), 16)
        val end = model.key

        return intArrayOf(first, second, third, end)
    }

    /**
     * 生成验证码
     * @param hour
     * @param loop
     */
    private fun createVerifyData(hour: Int, loop: Int): IntArray{
        val first = Integer.parseInt("DC", 16)
        val second = hour
        val end = if (loop == 0){
            Integer.parseInt("5A", 16)
        } else {
            128 + loop
        }
        return intArrayOf(first, second, end)
    }

    /**
     * 生成密码
     * @param period
     */
    private fun createPwdData(period: Int, type: Int): IntArray{
        val first = if (type == 0){
            0xba
        } else {
            0xca
        }
        val second = 32 + (period shl 8)
        val end = period and 0xff
        return intArrayOf(first, second, end)
    }

    private fun getResult(arr: IntArray): String{
        var r = ((arr[0] shl 16) + (arr[1] shl 8) + arr[2]).toString()
        var result = r
        for (i in 1..(8 - r.length)){
            result = "0$result"
        }
        return result
    }
}