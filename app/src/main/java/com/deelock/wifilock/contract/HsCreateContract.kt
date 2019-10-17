package com.deelock.wifilock.contract

import android.content.Context

/**
 * Created by forgive for on 2018\6\20 0020.
 */
interface HsCreateContract {

    interface HsCreateModelImpl{

    }

    interface IHsCreateModel{
        fun getKey(context: Context, pid: String)

        fun addHomeStayPwd(context: Context, sdlId: String, type: Int, pwd: String, authId: String, timeBegin: Long)
    }
}