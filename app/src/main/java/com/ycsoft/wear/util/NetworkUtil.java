package com.ycsoft.wear.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Jeremy on 2016/11/26.
 * 网络工具类
 */

public class NetworkUtil {
    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            isConnected = true;
        }
        return isConnected;
    }
}
