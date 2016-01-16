package com.codeboy.qianghongbao;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * <p>Created 16/1/16 上午1:15.</p>
 * <p><a href="mailto:730395591@qq.com">Email:730395591@qq.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author 李文龙(LeonLee)
 */
public class QHBApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /** 首个activity启动调用*/
    public static void activityStartMain(Activity activity) {

    }

    /** 每个activity生命周期里的onCreate*/
    public static void activityCreateStatistics(Activity activity) {

    }

    /** 每个activity生命周期里的onResume*/
    public static void activityResumeStatistics(Activity activity) {

    }

    /** 每个activity生命周期里的onPause*/
    public static void activityPauseStatistics(Activity activity) {

    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event) {

    }

    /** 事件统计*/
    public static void eventStatistics(Context context, String event, String tag) {

    }
}
