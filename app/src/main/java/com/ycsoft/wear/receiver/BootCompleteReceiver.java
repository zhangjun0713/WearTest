package com.ycsoft.wear.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.TcpReceiveCallService;
import com.ycsoft.wear.service.UdpReceiveCancelCallService;
import com.ycsoft.wear.ui.activity.LoginActivity;
import com.ycsoft.wear.ui.activity.MainActivity;
import com.ycsoft.wear.ui.activity.SplashActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

/**
 * Created by zhangjun on 2017/1/5.
 * 开机完成广播
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_SERVER_IP);
        //启动监听udp广播的服务
        Intent startReceiverIntent = new Intent(context, UdpReceiveCancelCallService.class);
        startReceiverIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(startReceiverIntent);
        //启动监听呼叫服务tcp的服务
        Intent callReceiverIntent = new Intent(context, TcpReceiveCallService.class);
        callReceiverIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(callReceiverIntent);
//        if (!mSharedPreferenceUtil.getBoolean(SpfConstants.KEY_IS_LOGIN, false)) {
//            //跳转到登录
//            Intent loginIntent = new Intent(context, LoginActivity.class);
//            loginIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(loginIntent);
//        } else {
//            //跳转到启动界面
//            Intent loginIntent = new Intent(context, SplashActivity.class);
//            loginIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(loginIntent);
//        }
    }
}
