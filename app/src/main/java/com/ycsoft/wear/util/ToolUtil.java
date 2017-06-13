package com.ycsoft.wear.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SpfConstants;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangjun on 2016/10/20.
 * 工具类
 */

public class ToolUtil {
    private static long lastTimeMillis = 0;

    /**
     * 防止点击过快的检测
     *
     * @return true表示是点击过快了，反之正常点击
     */
    public static boolean isFastClick() {
        long curTime = System.currentTimeMillis();
        long duringTime = curTime - lastTimeMillis;
        lastTimeMillis = curTime;
        return duringTime < 300;
    }

    /**
     * 判断是ip地址格式是否合法
     *
     * @param source
     * @return
     */
    public static boolean isIPFormat(String source) {
        boolean flag;
        Pattern pattern = Pattern
                .compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(source);
        flag = m.matches();
        return flag;
    }

    /**
     * 判断服务是否已经在运行
     *
     * @param context
     * @param serviceName
     * @return
     */
    public static boolean isServiceLive(Context context, String serviceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取认证Token
     *
     * @param context
     * @param callback
     */
    public static void getToken(Context context, Callback.CommonCallback<String> callback) {
        SharedPreferenceUtil mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
        RequestParams params = new RequestParams("http://" + Constants.SERVER_IP + "/api/waiter");
        params.setCharset("UTF-8");
        params.addParameter(SpfConstants.KEY_ID, mSharedPreferenceUtil.getString(SpfConstants.KEY_ID, ""));
        params.addParameter(SpfConstants.KEY_PWD, mSharedPreferenceUtil.getString(SpfConstants.KEY_PWD, ""));
        x.http().get(params, callback);
    }

    /**
     * 唤醒屏幕
     */
    public static void wakeUpScreen(Context context) {
        PowerManager.WakeLock mWakelock;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wake_tag");
        mWakelock.acquire();
    }

    /**
     * 开始震动
     *
     * @param context
     * @param second  秒
     */
    public static void startVibrate(Context context, int second) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            long[] mPattern = new long[second * 2];
            for (int i = 0; i < second * 2; i++) {
                mPattern[i] = 500;
            }
            vibrator.vibrate(mPattern, -1);
        }
    }

    /**
     * 停止震动
     */
    public static void stopVibrator(Context context) {
        Vibrator mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null)
            mVibrator.cancel();
    }
}
