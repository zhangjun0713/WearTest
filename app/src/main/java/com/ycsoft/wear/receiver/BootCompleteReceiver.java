package com.ycsoft.wear.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.service.SocketReceiveCallService;
import com.ycsoft.wear.service.SocketReceiveServerIpService;
import com.ycsoft.wear.service.UdpReceiveCancelCallService;
import com.ycsoft.wear.socket.UdpSendBroadcast;
import com.ycsoft.wear.ui.activity.LoginActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zhangjun on 2017/1/5.
 * 开机完成广播
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    public void onReceive(Context context, Intent intent) {
        mSharedPreferenceUtil = new SharedPreferenceUtil(context, Constants.SPF_NAME);
        //启动监听udp广播的服务
        Intent startReceiverIntent = new Intent(context, UdpReceiveCancelCallService.class);
        startReceiverIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(startReceiverIntent);
        //启动监听呼叫服务Socket的服务
        Intent callReceiverIntent = new Intent(context, SocketReceiveCallService.class);
        callReceiverIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(callReceiverIntent);
        //启动获取服务器IP的Socket的服务
        Intent getServerIpIntent = new Intent(context, SocketReceiveServerIpService.class);
        getServerIpIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.startService(getServerIpIntent);
        //如果之前已经登录了，则此处只需要注册
        if (!mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
            mSharedPreferenceUtil.removeKey("roomNumber");
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "REGIST");
                jsonObject.put("name", mSharedPreferenceUtil.getString("name", ""));
                jsonObject.put("id", mSharedPreferenceUtil.getString("id", ""));
                jsonObject.put("floor", mSharedPreferenceUtil.getString("floor", ""));
                UdpSendBroadcast.sendBroadCastToCenter(context,
                        jsonObject.toString(), SocketConstants.BC_PORT_GET_SERVER_IP);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (!mSharedPreferenceUtil.getBoolean("isLogin", false)) {
            //跳转到登录
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(loginIntent);
        }
    }
}
