/*
 * Project: Laiwang
 * 
 * File Created at 2016-01-15
 * 
 * Copyright 2013 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
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
    public static final String KEY_ENABLE_QIANG_HONG_BAO = "KEY_ENABLE_QIANG_HONG_BAO";

    SharedPreferences preferences;

    public Config(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isEnableQiangHongBao() {
        return preferences.getBoolean(KEY_ENABLE_QIANG_HONG_BAO, true);
    }
}
