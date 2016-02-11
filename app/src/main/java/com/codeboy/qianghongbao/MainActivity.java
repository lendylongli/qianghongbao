package com.codeboy.qianghongbao;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codeboy.qianghongbao.job.WechatAccessbilityJob;
import com.codeboy.qianghongbao.util.BitmapUtils;

import java.io.File;

/**
 * <p>Created by LeonLee on 15/2/17 下午10:11.</p>
 * <p><a href="mailto:codeboy2013@163.com">Email:codeboy2013@163.com</a></p>
 *
 * 抢红包主界面
 */
public class MainActivity extends BaseSettingsActivity {

    private Dialog mTipsDialog;
    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String version = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = " v" + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setTitle(getString(R.string.app_name) + version);

        QHBApplication.activityStartMain(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        registerReceiver(qhbConnectReceiver, filter);
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    public Fragment getSettingsFragment() {
        mMainFragment = new MainFragment();
        return mMainFragment;
    }

    private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isFinishing()) {
                return;
            }
            String action = intent.getAction();
            Log.d("MainActivity", "receive-->" + action);
            if(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT.equals(action)) {
                if (mTipsDialog != null) {
                    mTipsDialog.dismiss();
                }
            } else if(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT.equals(action)) {
                showOpenAccessibilityServiceDialog();
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT.equals(action)) {
                if(mMainFragment != null) {
                    mMainFragment.updateNotifyPreference();
                }
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT.equals(action)) {
                if(mMainFragment != null) {
                    mMainFragment.updateNotifyPreference();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(QiangHongBaoService.isRunning()) {
            if(mTipsDialog != null) {
                mTipsDialog.dismiss();
            }
        } else {
            showOpenAccessibilityServiceDialog();
        }

        boolean isAgreement = Config.getConfig(this).isAgreement();
        if(!isAgreement) {
            showAgreementDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(qhbConnectReceiver);
        } catch (Exception e) {}
        mTipsDialog = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item = menu.add(0, 0, 1, R.string.open_service_button);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem notifyitem = menu.add(0, 3, 2, R.string.open_notify_service);
        notifyitem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem about = menu.add(0, 4, 4, R.string.about_title);
        about.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                openAccessibilityServiceSettings();
                QHBApplication.eventStatistics(this, "menu_service");
                return true;
            case 3:
                openNotificationServiceSettings();
                QHBApplication.eventStatistics(this, "menu_notify");
                break;
            case 4:
                startActivity(new Intent(this, AboutMeActivity.class));
                QHBApplication.eventStatistics(this, "menu_about");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** 显示免责声明的对话框*/
    private void showAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.agreement_title);
        builder.setMessage(getString(R.string.agreement_message, getString(R.string.app_name)));
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(true);
                QHBApplication.eventStatistics(MainActivity.this, "agreement", "true");
            }
        });
        builder.setNegativeButton("不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getConfig(getApplicationContext()).setAgreement(false);
                QHBApplication.eventStatistics(MainActivity.this, "agreement", "false");
                finish();
            }
        });
        builder.show();
    }

    /** 分享*/
    private void showShareDialog() {
        QHBApplication.showShare(this);
    }

    /** 二维码*/
    private void showQrDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        View view = getLayoutInflater().inflate(R.layout.qr_dialog_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = getString(R.string.qr_wx_id);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", id);
                clipboardManager.setPrimaryClip(clip);

                //跳到微信
                Intent wxIntent = getPackageManager().getLaunchIntentForPackage(
                        WechatAccessbilityJob.WECHAT_PACKAGENAME);
                if(wxIntent != null) {
                    try {
                        startActivity(wxIntent);
                    } catch (Exception e){}
                }

                Toast.makeText(getApplicationContext(), "已复制到粘贴板", Toast.LENGTH_LONG).show();
                QHBApplication.eventStatistics(MainActivity.this, "copy_qr");
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    /** 显示捐赠的对话框*/
    private void showDonateDialog() {
        final Dialog dialog = new Dialog(this, R.style.QR_Dialog_Theme);
        View view = getLayoutInflater().inflate(R.layout.donate_dialog_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                File output = new File(android.os.Environment.getExternalStorageDirectory(), "codeboy_wechatpay_qr.jpg");
                if(!output.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wechatpay_qr);
                    BitmapUtils.saveBitmap(MainActivity.this, output, bitmap);
                }
                Toast.makeText(MainActivity.this, "已保存到:" + output.getAbsolutePath(), Toast.LENGTH_LONG).show();
                return true;
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    /** 显示未开启辅助服务的对话框*/
    private void showOpenAccessibilityServiceDialog() {
        if(mTipsDialog != null && mTipsDialog.isShowing()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_tips_layout, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilityServiceSettings();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.open_service_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.open_service_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openAccessibilityServiceSettings();
            }
        });
        mTipsDialog = builder.show();
    }

    /** 打开辅助服务的设置*/
    private void openAccessibilityServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 打开通知栏设置*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void openNotificationServiceSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, R.string.tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MainFragment extends BaseSettingsFragment {

        private SwitchPreference notificationPref;
        private boolean notificationChangeByUser = true;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.main);

            //微信红包开关
            Preference wechatPref = findPreference(Config.KEY_ENABLE_WECHAT);
            wechatPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue && !QiangHongBaoService.isRunning()) {
                        ((MainActivity)getActivity()).showOpenAccessibilityServiceDialog();
                    }
                    return true;
                }
            });

            notificationPref = (SwitchPreference) findPreference("KEY_NOTIFICATION_SERVICE_TEMP_ENABLE");
            notificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Toast.makeText(getActivity(), "该功能只支持安卓4.3以上的系统", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if(!notificationChangeByUser) {
                        notificationChangeByUser = true;
                        return true;
                    }

                    boolean enalbe = (boolean) newValue;

                    Config.getConfig(getActivity()).setNotificationServiceEnable(enalbe);

                    if(enalbe && !QiangHongBaoService.isNotificationServiceRunning()) {
                        ((MainActivity)getActivity()).openNotificationServiceSettings();
                        return false;
                    }
                    QHBApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(newValue));
                    return true;
                }
            });

            Preference preference = findPreference("KEY_FOLLOW_ME");
            if(preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((MainActivity) getActivity()).showQrDialog();
                        QHBApplication.eventStatistics(getActivity(), "about_author");
                        return true;
                    }
                });
            }

            preference = findPreference("KEY_DONATE_ME");
            if(preference != null) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((MainActivity) getActivity()).showDonateDialog();
                        QHBApplication.eventStatistics(getActivity(), "donate");
                        return true;
                    }
                });
            }

            findPreference("WECHAT_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), WechatSettingsActivity.class));
                    return true;
                }
            });

            findPreference("NOTIFY_SETTINGS").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), NotifySettingsActivity.class));
                    return true;
                }
            });

        }

        /** 更新快速读取通知的设置*/
        public void updateNotifyPreference() {
            if(notificationPref == null) {
                return;
            }
            boolean running = QiangHongBaoService.isNotificationServiceRunning();
            boolean enable = Config.getConfig(getActivity()).isEnableNotificationService();
            if( enable && running && !notificationPref.isChecked()) {
                QHBApplication.eventStatistics(getActivity(), "notify_service", String.valueOf(true));
                notificationChangeByUser = false;
                notificationPref.setChecked(true);
            } else if((!enable || !running) && notificationPref.isChecked()) {
                notificationChangeByUser = false;
                notificationPref.setChecked(false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateNotifyPreference();
        }
    }
}
