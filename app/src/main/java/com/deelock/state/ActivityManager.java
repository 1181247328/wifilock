package com.deelock.state;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *存放你打开过的所有的Activity
 * Created by admin on 2016/4/19.
 */
public class ActivityManager {
    //输出日志
    private String TAG = ActivityManager.class.getSimpleName();

    //环境上下文
    private Context context;

    //实例化
    private static ActivityManager activityManager;

    //保存所有的activity,SoftReference是什么呢可以去网上查看一下,这个东西还是挺有用的
    private final HashMap<String,SoftReference<Activity>> taskMap = new HashMap<String,SoftReference<Activity>>();

    //确定是哪个Activity
    public ActivityManager(Context context) {
        this.context = context;
    }


    //ActivityManager管理器实例化
    public static ActivityManager getActivityManager(Context context){
            if(activityManager == null){
                //实例化
                activityManager = new ActivityManager(context);
            }
        return activityManager;
    }


    /**
     * 保存activity
     */
    public final void putActivity(Activity atv){
        Log.e(TAG,"---"+atv.toString());
        taskMap.put(atv.toString(),new SoftReference<Activity>(atv));
    }

    /**
     * 删除Activtiy
     * @param atv
     */
    public final void removeActivity(Activity atv){
        taskMap.remove(atv.toString());
    }

    public final void removeActivity(String atv){
        taskMap.remove(atv);
    }

    /**
     * 退出所有的栈中的ativity,什么是栈也可以去网上看一下,这个就不在这里多说了,android的知识面太广了
     */
    public final void exit(){
        //遍历MAP
        for(Iterator<Map.Entry<String,SoftReference<Activity>>> iterator = taskMap.entrySet().iterator();iterator.hasNext();){
            //获得每个activity
            SoftReference<Activity> activityReference = iterator.next().getValue();
            //获得activity
            Activity activity = activityReference.get();
            //不为空的情况下才做
            if(activity != null){
                //销毁
                activity.finish();
            }
        }
        //删除Map中所有的内容
        taskMap.clear();
    }
}
