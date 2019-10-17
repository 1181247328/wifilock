package com.deelock.wifilock.ui.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.deelock.wifilock.R
import com.deelock.wifilock.utils.ToastUtil
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by forgive for on 2018\6\19 0019.
 */
@SuppressLint("ValidFragment")
class LoopFragment(private var sdlId: String) : BaseFragment() {

    private lateinit var rootView: View
    private lateinit var pickerView: TimePickerView

    //widget
    private lateinit var dayRl: RelativeLayout
    private lateinit var settingTv: TextView
    private lateinit var startTv: TextView
    private lateinit var endTv: TextView
    private lateinit var startBtn: Button
    private lateinit var endBtn: Button
    private lateinit var createBtn: Button


    private lateinit var startCalendar: Calendar
    private lateinit var endCalendar: Calendar
    private var startDataSelected: Date? = null
    private var endDataSelected: Date? = null
    private lateinit var showf: SimpleDateFormat
    private lateinit var mf: SimpleDateFormat


    private lateinit var listener: (Int, Int, Int, Long, Long, String) -> Unit  //创建密码回调
    private var date = booleanArrayOf(false, false, false, false, false, false, false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_loop, container, false)
        findView()
        doBusiness()
        setEvent()
        return rootView
    }

    private fun findView(){
        dayRl = rootView.findViewById(R.id.dayRl)
        settingTv = rootView.findViewById(R.id.settingTv)
        startTv = rootView.findViewById(R.id.startTv)
        endTv = rootView.findViewById(R.id.endTv)
        startBtn = rootView.findViewById(R.id.startBtn)
        endBtn = rootView.findViewById(R.id.endBtn)
        createBtn = rootView.findViewById(R.id.createBtn)
    }

    @SuppressLint("SimpleDateFormat")
    private fun doBusiness(){
        startCalendar = Calendar.getInstance()
        endCalendar = Calendar.getInstance()
        showf = SimpleDateFormat("yyyy/MM/dd HH:00")
        mf = SimpleDateFormat("yyyy/MM/ddHH:mm")
    }

    private fun setEvent(){
        settingTv.setOnClickListener {
            val range = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天")
            var builder = AlertDialog.Builder(context!!)
            builder.setMultiChoiceItems(range, date) { dialog, which, isChecked ->
                date[which] = isChecked
            }
            builder.setPositiveButton("确定", { dialog, which ->
                var value = "星期"
                for (i in 0 until 7){
                    if (date[i]){
                        when(i){
                            0 -> value += "一，"
                            1 -> value += "二，"
                            2 -> value += "三，"
                            3 -> value += "四，"
                            4 -> value += "五，"
                            5 -> value += "六，"
                            6 -> value += "天，"
                        }
                    }
                }
                if (value.length > 2){
                    settingTv.text = value.substring(0, value.length - 1)
                }
            })
            builder.create().show()
        }

        startBtn.setOnClickListener {
            startBtn.setBackgroundResource(R.drawable.bg_input_time_left_solid)
            startBtn.setTextColor(-0x1)
            endBtn.setBackgroundResource(R.drawable.bg_input_time_right_stroke)
            endBtn.setTextColor(-0xfe400e)
            setStart()
        }

        endBtn.setOnClickListener {
            startBtn.setBackgroundResource(R.drawable.bg_input_time_left_stroke)
            startBtn.setTextColor(-0xfe400e)
            endBtn.setBackgroundResource(R.drawable.bg_input_time_right_solid)
            endBtn.setTextColor(-0x1)
            setEnd()
        }

        createBtn.setOnClickListener {
            clickCreate()
        }
    }

    private fun setStart(){
        startCalendar.time = Date()
        endCalendar.set(GregorianCalendar.YEAR, startCalendar.get(GregorianCalendar.YEAR) + 1)

        pickerView = TimePickerBuilder(context, OnTimeSelectListener { date2, v ->
            startDataSelected = date2
            startTv.text = showf.format(date2)
        })
                .setType(booleanArrayOf(true, true, true, true, false, false))//默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//滚轮文字大小
                .setTitleSize(18)//标题文字大小
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setTitleBgColor(Color.LTGRAY)
                .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                .setCancelColor(Color.BLUE)//取消按钮文字颜色
                .setRangDate(startCalendar, endCalendar)//默认是1900-2100年
                .setLabel("", "", "", ":00", "", "")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(false)//是否显示为对话框样式
                .build()

        pickerView.show()
    }

    private fun setEnd(){
        startCalendar.time = Date()
        endCalendar.set(GregorianCalendar.YEAR, startCalendar.get(GregorianCalendar.YEAR) + 1)
        if (startDataSelected != null){
            startCalendar.time = Date(startDataSelected!!.time + 3600000)
        }

        pickerView = TimePickerBuilder(context, OnTimeSelectListener { date2, v ->
            endDataSelected = date2
            endTv.text = showf.format(date2)
        })
                .setType(booleanArrayOf(true, true, true, true, false, false))//默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//滚轮文字大小
                .setTitleSize(18)//标题文字大小
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
                .setTitleBgColor(Color.LTGRAY)
                .setTextColorCenter(Color.BLACK)//设置选中项的颜色
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLUE)//确定按钮文字颜色
                .setCancelColor(Color.BLUE)//取消按钮文字颜色
                .setRangDate(startCalendar, endCalendar)//默认是1900-2100年
                .setLabel("", "", "", ":00", "", "")
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(false)//是否显示为对话框样式
                .build()

        pickerView.show()
    }

    private fun clickCreate(){
        if (settingTv.text.isEmpty() || settingTv.text == "点击设置"){
            ToastUtil.toastLong(context, "请选择周期")
            return
        }

        if (startDataSelected == null){
            ToastUtil.toastLong(context, "请选择起始时间")
            return
        }
        if (endDataSelected == null){
            ToastUtil.toastLong(context, "请选择结束时间")
            return
        }

        val p = endDataSelected!!.time - startDataSelected!!.time

        if (p < 0){
            ToastUtil.toastLong(context, "开始时间不能大于结束时间")
            return
        }

        var loop = 0
        var w = 0
        val period: Int = (p / 3600000).toInt()
        val format = SimpleDateFormat("HH")
        val hour = Integer.parseInt(format.format(startDataSelected))
        val timeBegin = startDataSelected!!.time / 1000
        val timeEnd = endDataSelected!!.time / 1000

        for (i in 6 downTo 0){
            loop = loop shl 1
            if (date[i]){
                loop += 1
            }
        }

        date.forEach {
            w = w shl 1
            if (it){
                w += 1
            }
        }

        val week = w.toString()

        listener(hour, period, loop, timeBegin, timeEnd, week)
    }

    fun setAlpha(alpha: Float){
        dayRl.alpha = alpha
    }

    fun setListener(listener: (Int, Int, Int, Long, Long, String) -> Unit){
        this.listener = listener
    }
}