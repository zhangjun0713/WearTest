package com.ycsoft.wear.common;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.xutils.x;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Jeremy on 2017/1/5.
 * Application
 */

public class YCApplication extends Application {
    private static final String TAG = "YCApplication";
    private static YCApplication mYcApplication;
    private static ExecutorService executorService;
    /**
     * 装Activity的集合
     */
    private List<BaseActivity> activities = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        executorService = Executors.newFixedThreadPool(5);
        //清除之前服务器IP地址
        new SharedPreferenceUtil(this, SpfConstants.SPF_NAME).removeKey(SpfConstants.KEY_SERVER_IP);
    }

    /**
     * 拿到线程池管理操作对象
     *
     * @return
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 非Activity中可以通过该方法拿到Application单例对象
     *
     * @return
     */
    public static YCApplication getApplicationInstance() {
        return mYcApplication;
    }

    /**
     * 添加activity到集合中
     *
     * @param baseActivity
     */
    public void addActivity(BaseActivity baseActivity) {
        activities.add(baseActivity);
    }

    /**
     * 移除一个Activity
     *
     * @param activity
     */
    public void removeActivity(BaseActivity activity) {
        activities.remove(activity);
    }

    /**
     * 退出app，结束当前app进程
     */
    public void exitApp() {
        for (BaseActivity baseActivity : activities) {
            if (baseActivity != null) {
                baseActivity.finish();
            }
        }
        Process.killProcess(Process.myPid());
    }

    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = null;
            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }
            String mac = null;
            FileReader fstream = null;
            try {
                fstream = new FileReader("/sys/class/net/wlan0/address");
            } catch (FileNotFoundException e) {
                fstream = new FileReader("/sys/class/net/eth0/address");
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(fstream, 1024);
                mac = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            json.put("mac", mac);
            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }
            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
