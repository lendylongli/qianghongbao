package com.codeboy.qianghongbao.job;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;

import com.codeboy.qianghongbao.Config;

/**
 * <p>Created 16/1/16 上午12:38.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public abstract class BaseAccessbilityJob implements AccessbilityJob {

    protected final AccessibilityService mService;
    protected final Config mConfig;

    public BaseAccessbilityJob(AccessibilityService service, Config config) {
        this.mService = service;
        this.mConfig = config;
    }

    public Context getContext() {
        return mService.getApplicationContext();
    }
}
