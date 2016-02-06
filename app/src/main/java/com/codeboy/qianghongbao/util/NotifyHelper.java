package com.codeboy.qianghongbao.util;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;

import com.codeboy.qianghongbao.Config;

import java.util.Calendar;

/**
 * <p>Created 16/2/5 下午9:48.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class NotifyHelper {

    private static Vibrator sVibrator;
    private static KeyguardManager sKeyguardManager;
    private static PowerManager sPowerManager;

    /** 播放声音*/
    public static void playSound(Context context) {
        try {
            MediaPlayer player = MediaPlayer.create(context,
                    Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 振动*/
    public static void vibrator(Context context) {
        if(sVibrator == null) {
            sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        sVibrator.vibrate(new long[]{100, 10, 100, 1000}, -1);
    }

    /** 是否为夜间*/
    public static  boolean isNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour >= 23 || hour < 7) {
            return true;
        }
        return false;
    }

    public static KeyguardManager getKeyguardManager(Context context) {
        if(sKeyguardManager == null) {
            sKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
        return sKeyguardManager;
    }

    public static PowerManager getPowerManager(Context context) {
        if(sPowerManager == null) {
            sPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return sPowerManager;
    }

    /** 是否为锁屏或黑屏状态*/
    public static boolean isLockScreen(Context context) {
        KeyguardManager km = getKeyguardManager(context);

        return km.inKeyguardRestrictedInputMode() || !isScreenOn(context);
    }

    public static boolean isScreenOn(Context context) {
        PowerManager pm = getPowerManager(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return pm.isScreenOn();
        }
    }

    /** 检查并播放通知*/
    public static void checkAndPlayNotify(Context context, Config config, PendingIntent pendingIntent, boolean fromWechat) {
        //夜间模式，不处理
        if(NotifyHelper.isNight() && config.isNotifyNight()) {
            return;
        }

        boolean lock = isLockScreen(context);
        if(!lock && fromWechat && config.getWechatMode() == Config.WX_MODE_0) {
            return;
        } else if(!lock && !fromWechat) { //非锁屏下
            return;
        }

        if(lock) {
            showNotify(context, pendingIntent);
        }

        if(config.isNotifySound()) {
            playSound(context);
        }
        if(config.isNotifyVibrate()) {
            vibrator(context);
        }
    }

    /** 显示通知*/
    public static void showNotify(Context context, PendingIntent pendingIntent) {

    }
}
