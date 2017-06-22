package com.ycsoft.wear.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.util.SharedPreferenceUtil;

/**
 * Created by zhangjun on 2017/1/5.
 * 开机完成广播
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //清除掉配置文件中的服务器IP地址信息，在启动APP时自动重新去获取
        SharedPreferenceUtil mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_SERVER_IP);
    }
}
