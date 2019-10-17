package com.deelock.wifilock.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.widget.*
import com.deelock.wifilock.R
import com.deelock.wifilock.entity.User
import com.deelock.wifilock.network.BaseResponse
import com.deelock.wifilock.network.RequestUtils
import com.deelock.wifilock.network.ResponseCallback
import com.deelock.wifilock.network.TimeUtil
import com.deelock.wifilock.utils.SPUtil
import com.deelock.wifilock.utils.StatusBarUtil
import com.deelock.wifilock.utils.ToastUtil
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by forgive for on 2018\6\21 0021.
 */
class AddUserActivity : BaseActivity() {


    //widget
    lateinit var backIb: ImageButton
    lateinit var phoneNumberEt: EditText
    lateinit var contactsRl: RelativeLayout
    lateinit var nickNameEt: EditText
    lateinit var confirmBtn: Button
    private var userList: List<User> = ArrayList()

    private var sdlId = ""


    override fun bindActivity() {
        setContentView(R.layout.activity_add_user)
        StatusBarUtil.StatusBarLightMode(this)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        phoneNumberEt = f(R.id.phoneNumberEt)
        contactsRl = f(R.id.contactsRl)
        nickNameEt = f(R.id.nickNameEt)
        confirmBtn = f(R.id.confirmBtn)
    }

    override fun doBusiness() {
        sdlId = intent.getStringExtra("sdlId")
        userList = intent.getParcelableArrayListExtra("userList")
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }

        contactsRl.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                //判断是否具有权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS),
                            30)
                } else {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.data = ContactsContract.Contacts.CONTENT_URI
                    startActivityForResult(intent, 30)
                }
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.data = ContactsContract.Contacts.CONTENT_URI
                startActivityForResult(intent, 30)
            }
        }

        confirmBtn.setOnClickListener {
            val number = phoneNumberEt.text.trim().toString()
            if (!TextUtils.isEmpty(number)) {
                for (item in userList) {
                    if (number == item.phoneNumber) {
                        Toast.makeText(this@AddUserActivity, "该手机号已被" + item.name + "使用", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }
            val name = nickNameEt.text.toString()
            if (!TextUtils.isEmpty(name)) {
                for (item in userList) {
                    if (name == item.name) {
                        Toast.makeText(this@AddUserActivity, "该用户名已存在", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            } else {
                Toast.makeText(this@AddUserActivity, "请输入用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val params = HashMap<String, Any>()
            params["timestamp"] = TimeUtil.getTime()
            params["uid"] = SPUtil.getUid(this)
            params["sdlId"] = sdlId
            params["name"] = nickNameEt.text.toString()
            params["isPush"] = 1
            if (!TextUtils.isEmpty(number)) {
                params["phoneNumber"] = number
            }
            val url: String
//            if ("A003" == sdlId.substring(0, 4)) {
//                url = RequestUtils.ADD_AUTH_USER_1
//            } else {
//                url = RequestUtils.ADD_AUTH_USER
//            }
            url = RequestUtils.ADD_AUTH_USER
            RequestUtils.request(url, this, params).enqueue(
                    object : ResponseCallback<BaseResponse>(this) {
                        override fun onSuccess(code: Int, content: String) {
                            if (code == 1) {
                                ToastUtil.toastShort(this@AddUserActivity, "创建成功")
                                finish()
                            }
                        }

                        override fun onFailure(code: Int, message: String?) {
                            super.onFailure(code, message)
                            ToastUtil.toastShort(this@AddUserActivity, message)
                        }
                    })
        }
    }

    /**
     * 显示选择的电话号
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }

        val contactData = data.data
        val c = managedQuery(contactData, null, null, null, null)
        if (c.moveToFirst()) {
            val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            var hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
            val contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
            var phoneNumber: String? =
                    null
            hasPhone = if (hasPhone.equals("1", ignoreCase = true)) {
                "true"
            } else {
                "false"
            }
            if (java.lang.Boolean.parseBoolean(hasPhone)) {
                val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                + contactId, null, null)
                while (phones!!.moveToNext()) {
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
                phones.close()
                val sb = StringBuilder("")
                for (i in 0 until phoneNumber!!.length) {
                    val cc = phoneNumber[i]
                    if (cc.toInt() in 48..57) {
                        if (sb.isEmpty() && cc.toInt() != 49) {
                            continue
                        }
                        sb.append(cc)
                    }
                }
                phoneNumberEt.text = SpannableStringBuilder(sb)
            }
        }
    }
}