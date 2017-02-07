package com.ycsoft.wear.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;

import org.json.JSONException;
import org.json.JSONObject;
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
        boolean flag = false;
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
}
