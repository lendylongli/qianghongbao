package com.codeboy.qianghongbao;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <p>Created 16/1/15 下午10:59.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class Config {

    public static final String PREFERENCE_NAME = "config";
    public static final String KEY_ENABLE_WECHAT = "KEY_ENABLE_WECHAT";

    SharedPreferences preferences;

    public Config(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /** 是否启动微信抢红包*/
    public boolean isEnableWechat() {
        return preferences.getBoolean(KEY_ENABLE_WECHAT, true);
    }
}
