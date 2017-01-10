package com.ycsoft.weartest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.socket.SocketReceiverService;
import com.ycsoft.weartest.ui.activity.LoginActivity;
import com.ycsoft.weartest.util.SharedPreferenceUtil;

/**
 * Created by zhangjun on 2017/1/5.
 * 开机完成广播
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSharedPreferenceUtil = new SharedPreferenceUtil(context, Constants.SPF_NAME);
        if (!mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
            //// TODO: 需要调用HTTP接口更新服务器上员工信息
            mSharedPreferenceUtil.removeKey("roomNumber");
        }
        //启动监听udp广播的服务
        Intent startReceiver = new Intent(context, SocketReceiverService.class);
        startReceiver.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(startReceiver);
        if (!mSharedPreferenceUtil.getBoolean("isLogin", false)) {
            //跳转到登录
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
        }
    }
}
