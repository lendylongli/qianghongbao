package com.codeboy.qianghongbao.job;

import android.view.accessibility.AccessibilityEvent;

/**
 * <p>Created 16/1/16 上午12:32.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public interface AccessbilityJob {
    String getTargetPackageName();
    void onReceiveJob(AccessibilityEvent event);
    void onStopJob();
}
