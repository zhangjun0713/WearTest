package com.ycsoft.wear.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.ycsoft.wear.service.UdpReceiveServerIpService;
import com.ycsoft.wear.util.ToolUtil;

/**
 * Created by zhangjun on 2016/12/28.
 * 连接到网络广播接收器
 */

public class NetworkConnectedReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkConnectedReceive";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiState = connectivityManager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).getState();
        if (NetworkInfo.State.CONNECTED == wifiState) {
            //启动接收服务器IP的服务
            Log.d(TAG, "onReceive: 启动接收服务器IP的服务");
            if (!ToolUtil.isServiceLive(context, UdpReceiveServerIpService.class.getName())) {
                Intent getServerIntent = new Intent(context, UdpReceiveServerIpService.class);
                context.startService(getServerIntent);
            }
        }
    }
}
