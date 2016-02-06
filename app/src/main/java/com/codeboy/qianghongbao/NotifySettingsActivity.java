package com.codeboy.qianghongbao;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;

/**
 * <p>Created 16/2/5 下午9:25.</p>
 * <p><a href="mailto:codeboy2013@gmail.com">Email:codeboy2013@gmail.com</a></p>
 * <p><a href="http://www.happycodeboy.com">LeonLee Blog</a></p>
 *
 * @author LeonLee
 */
public class NotifySettingsActivity extends BaseSettingsActivity {
    @Override
    public Fragment getSettingsFragment() {
        return new NotifySettingsFragment();
    }

    public static class NotifySettingsFragment extends BaseSettingsFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.notify_settings);

            findPreference(Config.KEY_NOTIFY_SOUND).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_sound", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_VIBRATE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_vibrate", String.valueOf(newValue));
                    return true;
                }
            });

            findPreference(Config.KEY_NOTIFY_NIGHT_ENABLE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    QHBApplication.eventStatistics(getActivity(), "notify_night", String.valueOf(newValue));
                    return true;
                }
            });
        }
    }
}
