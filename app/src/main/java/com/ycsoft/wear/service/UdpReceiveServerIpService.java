package com.ycsoft.wear.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.socket.UdpSendBroadcast;
import com.ycsoft.wear.ui.activity.SplashActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zhangjun on 2016/12/28.
 * 接收服务器IP地址UDP服务类
 */

public class UdpReceiveServerIpService extends Service implements Runnable {
    private static final String TAG = "UdpReceiveServerIp";
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    public void onCreate() {
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
        x.task().run(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "GET_SERVER_IP");
            UdpSendBroadcast.sendBroadCastToCenter(this, jsonObject.toString(),
                    SocketConstants.PORT_UDP_GET_SERVER_IP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        //在这里同样使用约定好的端口
        int port = SocketConstants.PORT_UDP_RECEIVE_SERVER_IP;
        DatagramSocket server;
        try {
            server = new DatagramSocket(port);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                try {
                    //1.接收服务器广播消息
                    server.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0,
                            packet.getLength(), "UTF-8");
                    //2.解析并处理消息
                    if (!ToolUtil.isIPFormat(receivedMessage)) {
                        receivedMessage = packet.getAddress().getHostAddress();
                    }
                    mSharedPreferenceUtil.setValue(SpfConstants.KEY_SERVER_IP, receivedMessage);
                    Constants.SERVER_IP = receivedMessage;
                    Log.d(TAG, "address : " + packet.getAddress() + "\ncontent : " + receivedMessage);
                    mHandler.sendEmptyMessage(STOP_SERVICE);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止当前获取服务器IP的服务
     */
    private static final int STOP_SERVICE = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_SERVICE:
                    Log.d(TAG, "handleMessage: 获取服务器IP成功！");
                    //广播通知连接成功
                    Intent intent = new Intent(SplashActivity.BC_CONNECTED_SERVER);
                    sendBroadcast(intent);
                    stopSelf();
                    break;
            }
        }
    };
}
