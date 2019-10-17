package com.deelock.wifilock.application;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.deelock.wifilock.R;
import com.deelock.wifilock.network.ServerHost;
import com.mob.MobSDK;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by mingjun on 16/8/25.
 */
public class InitializeService extends IntentService {

    private static final String ACTION_INIT_WHEN_APP_CREATE = "com.deelock.wifilock.application.action.INIT";

    public InitializeService() {
        super("InitializeService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                performInit();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void performInit() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        MobSDK.init(getApplicationContext(), "22242da69e853", "faa549bb71c29146a4ceec2f0340ed23");
        JPushInterface.init(getApplicationContext());
    }

    public boolean isConnByHttp(){
        boolean isConn = false;
        URL url;
        HttpURLConnection conn = null;
        try {
            url = new URL(ServerHost.INTERNAL_SERVER_HOST);//你的服务器IP
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(1000*5);
            if(conn.getResponseCode()==200){
                isConn = true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            conn.disconnect();
        }
        return isConn;
    }

    private static final String NOTIFICATION_TITLE = "notification_title";
    public static final int INVALID_COLOR = -1; // 无效颜色
    private static int notificationTitleColor = INVALID_COLOR; // 获取到的颜色缓存


    /**
     * 获取系统通知栏主标题颜色，根据Activity继承自AppCompatActivity或FragmentActivity采取不同策略。
     *
     * @param context 上下文环境
     * @return 系统主标题颜色
     */
    public static int getNotificationColor(Context context) {
        try {
            if (notificationTitleColor == INVALID_COLOR) {
                if (context instanceof AppCompatActivity) {
                    notificationTitleColor = getNotificationColorCompat(context);
                } else {
                    notificationTitleColor = getNotificationColorInternal(context);
                }
            }
        } catch (Exception ignored) {

        }
        return notificationTitleColor;
    }



//     * 通过一个空的Notification拿到Notification.contentView，通过{@link RemoteViews#apply(Context, ViewGroup)}方法返回通知栏消息根布局实例。
    /**
     *
     * @param context 上下文
     * @return 系统主标题颜色
     */
    private static int getNotificationColorInternal(Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(NOTIFICATION_TITLE);
        Notification notification = builder.build();
        try {
            ViewGroup root = (ViewGroup) notification.contentView.apply(context, new FrameLayout(context));
            TextView titleView = (TextView) root.findViewById(android.R.id.title);
            if (null == titleView) {
                iteratorView(root, new Filter() {
                    @Override
                    public void filter(View view) {
                        if (view instanceof TextView) {
                            TextView textView = (TextView) view;
                            if (NOTIFICATION_TITLE.equals(textView.getText().toString())) {
                                notificationTitleColor = textView.getCurrentTextColor();
                            }
                        }
                    }
                });
                return notificationTitleColor;
            } else {
                return titleView.getCurrentTextColor();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getNotificationColorCompat(context);
        }
    }
    /**
     * 使用getNotificationColorInternal()方法，Activity不能继承自AppCompatActivity（实测5.0以下机型可以，5.0及以上机型不行），
     * 大致的原因是默认通知布局文件中的ImageView（largeIcon和smallIcon）被替换成了AppCompatImageView，
     * 而在5.0及以上系统中，AppCompatImageView的setBackgroundResource(int)未被标记为RemotableViewMethod，导致apply时抛异常。
     *
     * @param context 上下文
     * @return 系统主标题颜色
     */
    private static int getNotificationColorCompat(Context context) {
        try {
            Notification.Builder builder = new Notification.Builder(context);
            Notification notification = builder.build();
            int layoutId = notification.contentView.getLayoutId();
            ViewGroup root = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
            TextView titleView = (TextView) root.findViewById(android.R.id.title);
            if (null == titleView) {
                return getTitleColorIteratorCompat(root);
            } else {
                return titleView.getCurrentTextColor();
            }
        } catch (Exception e) {
        }
        return INVALID_COLOR;
    }
    private static void iteratorView(View view, Filter filter) {
        if (view == null || filter == null) {
            return;
        }
        filter.filter(view);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                iteratorView(child, filter);
            }
        }
    }
    private static int getTitleColorIteratorCompat(View view) {
        if (view == null) {
            return INVALID_COLOR;
        }
        List<TextView> textViews = getAllTextViews(view);
        int maxTextSizeIndex = findMaxTextSizeIndex(textViews);
        if (maxTextSizeIndex != Integer.MIN_VALUE) {
            return textViews.get(maxTextSizeIndex).getCurrentTextColor();
        }
        return INVALID_COLOR;
    }
    private static int findMaxTextSizeIndex(List<TextView> textViews) {
        float max = Integer.MIN_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        int index = 0;
        for (TextView textView : textViews) {
            if (max < textView.getTextSize()) {
                // 找到字号最大的字体，默认把它设置为主标题字号大小
                max = textView.getTextSize();
                maxIndex = index;
            }
            index++;
        }
        return maxIndex;
    }
    /**
     * 实现遍历View树中的TextView，返回包含TextView的集合。
     *
     * @param root 根节点
     * @return 包含TextView的集合
     */
    private static List<TextView> getAllTextViews(View root) {
        final List<TextView> textViews = new ArrayList<>();
        iteratorView(root, new Filter() {
            @Override
            public void filter(View view) {
                if (view instanceof TextView) {
                    textViews.add((TextView) view);
                }
            }
        });
        return textViews;
    }

    private interface Filter {
        void filter(View view);
    }
}
