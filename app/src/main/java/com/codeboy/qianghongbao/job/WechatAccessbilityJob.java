package com.codeboy.qianghongbao.job;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.codeboy.qianghongbao.BuildConfig;
import com.codeboy.qianghongbao.QiangHongBaoService;

import java.util.List;

/**
 * <p>Created 16/1/16 上午12:40.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class WechatAccessbilityJob extends BaseAccessbilityJob {

    private static final String TAG = "WechatAccessbilityJob";

    /** 微信的包名*/
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";

    /** 红包消息的关键字*/
    static final String HONGBAO_TEXT_KEY = "[微信红包]";

    /** 不能再使用文字匹配的最小版本号 */
    private static final int USE_ID_MIN_VERSION = 700;// 6.3.8 对应code为680,6.3.9对应code为700

    /** 列表红包资源id */
    private static final String ID_LIST_HONGBAO = "com.tencent.mm:id/cd";

    /** 列表红包资源文字 */
    private static final String TEXT_LIST_HONGBAO = "[微信红包]";

    /** 领取红包资源id */
    private static final String ID_PICK_UP_HONGBAO = "com.tencent.mm:id/dq";

    /** 领取红包资源文字 */
    private static final String TEXT_PICK_UP_HONGBAO = "领取红包";

    /** 点开红包资源id */
    private static final String ID_OPEN_HONGBAO = "com.tencent.mm:id/b2c";

    /** 点开红包资源文字*/
    private static final String TEXT_OPEN_HONGBAO = "拆红包";

    private boolean isFirstChecked ;
    private PackageInfo mWechatPackageInfo = null;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新安装包信息
            updatePackageInfo();
        }
    };

    @Override
    public void onCreateJob(QiangHongBaoService service) {
        super.onCreateJob(service);

        updatePackageInfo();

        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");

        getContext().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onStopJob() {
        try {
            getContext().unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {}
    }

    @Override
    public String getTargetPackageName() {
        return WECHAT_PACKAGENAME;
    }

    @Override
    public void onReceiveJob(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if(text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        }
    }

    private void sendNotifyEvent(){
        AccessibilityManager manager= (AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event=AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if(event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName beforeTop = am.getRunningTasks(1).get(0).topActivity;
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "beforeTop----" + beforeTop.toString());
        }
        //以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;

        isFirstChecked = true;
        try {
            pendingIntent.send();
            ComponentName afterTop = am.getRunningTasks(1).get(0).topActivity;
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "afterTop----" + afterTop.toString());
            }
            if (beforeTop.equals(afterTop)){
                handleChatListHongBao();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //点中了红包，下一步就是去拆红包
            handleLuckyMoneyReceive();
        } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后看详细的纪录界面
            //nonething
        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            handleChatListHongBao();
        }
    }

    /**
     * 点击聊天里的红包后，显示的界面
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLuckyMoneyReceive() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = null;
        if(isEnableUseText()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(TEXT_OPEN_HONGBAO);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            list = nodeInfo.findAccessibilityNodeInfosByViewId(ID_OPEN_HONGBAO);
        }

        if(list != null && !list.isEmpty()) {
            for (AccessibilityNodeInfo n : list) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 收到聊天里的红包
     * */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleChatListHongBao() {
        AccessibilityNodeInfo nodeInfo = getService().getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = null;
        if (isEnableUseText()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(TEXT_PICK_UP_HONGBAO);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            list = nodeInfo.findAccessibilityNodeInfosByViewId(ID_PICK_UP_HONGBAO);
        }

        if(list != null && list.isEmpty()) {
            // 从消息列表查找红包
            if (isEnableUseText()) {
                list = nodeInfo.findAccessibilityNodeInfosByText(TEXT_LIST_HONGBAO);
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(ID_LIST_HONGBAO);
            } else {
                list = null;
            }

            if(list == null || list.isEmpty()) {
                return;
            }

            for(AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else if(list != null) {
            //最新的红包领起
            for(int i = list.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if(parent != null) {
                    if (isFirstChecked){
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        isFirstChecked = false;
                    }
                    break;
                }
            }
        }
    }

    /** 是否可以通过使用文本方式进行抢红包*/
    private boolean isEnableUseText() {
        if(mWechatPackageInfo == null) {
            return true;
        }
        return mWechatPackageInfo.versionCode < USE_ID_MIN_VERSION;
    }

    /** 更新微信包信息*/
    private void updatePackageInfo() {
        try {
            mWechatPackageInfo = getContext().getPackageManager().getPackageInfo(WECHAT_PACKAGENAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
