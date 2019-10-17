package com.deelock.wifilock.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.view.ViewPager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.deelock.wifilock.R
import com.deelock.wifilock.adapter.ViewPagerFragmentAdapter
import com.deelock.wifilock.presenter.HsCreatePresenter
import com.deelock.wifilock.ui.fragment.LoopFragment
import com.deelock.wifilock.ui.fragment.OnceFragment
import com.deelock.wifilock.ui.fragment.PeriodFragment
import com.deelock.wifilock.utils.DensityUtil
import com.deelock.wifilock.widget.TabScrip

/**
 * Created by forgive for on 2018\6\19 0019.
 */
class HsCreateActivity: AppActivity() {


    //widget
    private lateinit var backIb: ImageButton
    private lateinit var bannerRl: RelativeLayout
    private lateinit var periodTv: TextView
    private lateinit var loopTv: TextView
    private lateinit var onceTv: TextView
    private lateinit var tabStrip: TabScrip
    private lateinit var vp: ViewPager


    private lateinit var adapter: ViewPagerFragmentAdapter
    private lateinit var animator: ValueAnimator
    private lateinit var presenter: HsCreatePresenter
    private var minHeight = 0
    private var maxHeight = 0
    private var bannerWidth = 0
    private var lastPosition = 0
    private var sdlId = "" //锁ID


    private lateinit var period: PeriodFragment
    private lateinit var loop: LoopFragment
    private lateinit var once: OnceFragment

    override fun bindActivity() {
        setContentView(R.layout.activity_create_password)
    }

    override fun findView() {
        backIb = f(R.id.backIb)
        bannerRl = f(R.id.bannerRl)
        periodTv = f(R.id.periodTv)
        loopTv = f(R.id.loopTv)
        onceTv = f(R.id.onceTv)
        tabStrip = f(R.id.tabStrip)
        vp = f(R.id.vp)
    }

    override fun doBusiness() {
        sdlId = intent.getStringExtra("sdlId")
        presenter = HsCreatePresenter(this, sdlId)

        period = PeriodFragment(sdlId)  //时段密码页面
        loop = LoopFragment(sdlId)
        once = OnceFragment(sdlId)

        bannerWidth = (187f * DensityUtil.getScreenWidth(this) / 750).toInt()
        adapter = ViewPagerFragmentAdapter(supportFragmentManager, listOf(period, loop, once))
        vp.adapter = adapter

        minHeight = 124 * DensityUtil.getScreenHeight(this) / 1334
        maxHeight = 247 * DensityUtil.getScreenHeight(this) / 1334
    }

    override fun setEvent() {
        backIb.setOnClickListener { finish() }

        periodTv.setOnClickListener {
            processEvent(0)
        }

        loopTv.setOnClickListener {
            processEvent(1)
        }

        onceTv.setOnClickListener {
            processEvent(2)
        }

        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val pos = ((position + positionOffset) * bannerWidth).toInt()
                tabStrip.scrollTo(-pos, 0)
                tabStrip.postInvalidate()

                val value = Math.abs(position + positionOffset - 1)
                if (value <= 1){
                    val height = ((1 - value) * (maxHeight - minHeight) + minHeight).toInt()
                    bannerRl.layoutParams.height = height
                    bannerRl.requestLayout()
                    loop.setAlpha(1 - value)
                }
            }

            override fun onPageSelected(position: Int) {
                changeBannerState(position)
            }
        })

        period.setListener { hour, period, timeBegin, timeEnd ->
            presenter.createResult(hour, period, 0, 1, timeBegin, timeEnd, "")
        }

        loop.setListener { hour, period, loop, timeBegin, timeEnd, week ->
            presenter.createResult(hour, period, loop, 2, timeBegin, timeEnd, week)
        }

        once.setListener { hour, period, timeBegin, timeEnd ->
            presenter.createResult(hour, period, 0, 0, timeBegin, timeEnd, "")
        }
    }

    /**
     * 处理进度条
     * @param item 页面
     */
    @SuppressLint("ResourceAsColor")
    private fun processEvent(item: Int){
        if (lastPosition == item){
            return
        }

        tabStrip.scroll(-bannerWidth * lastPosition, -bannerWidth * (item - lastPosition), 240)

        if (item == 1){
            animator = ValueAnimator.ofInt(bannerRl.layoutParams.height, maxHeight)
            animator.duration = 240
            animator.addUpdateListener {
                bannerRl.layoutParams.height = it.animatedValue as Int
                bannerRl.requestLayout()
            }
            animator.start()
        }

        if (lastPosition == 1){
            animator = ValueAnimator.ofInt(bannerRl.layoutParams.height, minHeight)
            animator.duration = 240
            animator.addUpdateListener {
                bannerRl.layoutParams.height = it.animatedValue as Int
                bannerRl.requestLayout()
            }
            animator.start()
        }

        vp.setCurrentItem(item, false)
    }

    /**
     * 改变字体颜色
     * @param pos 页面
     */
    private fun changeBannerState(pos: Int){
        if (lastPosition == pos){
            return
        }
        when(lastPosition){
            0 -> periodTv.setTextColor(Color.parseColor("#999999"))
            1 -> loopTv.setTextColor(Color.parseColor("#999999"))
            2 -> onceTv.setTextColor(Color.parseColor("#999999"))
        }
        when(pos){
            0 -> periodTv.setTextColor(Color.parseColor("#12C6E5"))
            1 -> loopTv.setTextColor(Color.parseColor("#12C6E5"))
            2 -> onceTv.setTextColor(Color.parseColor("#12C6E5"))
        }
        lastPosition = pos
    }
}