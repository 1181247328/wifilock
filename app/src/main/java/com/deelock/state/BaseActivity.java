package com.deelock.state;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * 继承FragmentActivity
 * Created by admin on 2016/4/19.
 */
public class BaseActivity extends FragmentActivity {
    //实例化activity管理器
    private ActivityManager manager = ActivityManager.getActivityManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //将哪个activity保存进管理器
        manager.putActivity(this);
    }

    /**
     * 销毁操作时删除掉所有的activity直接退出程序
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //删除操作
        manager.removeActivity(this);
    }

    /**
     * 通过它来触发删除所有的actvity
     */
    public void exit(){
        manager.exit();
    }

    public void remove(Activity activity){
        manager.removeActivity(activity);
    }

    public void remove(String activity){
        manager.removeActivity(activity);
    }
}
