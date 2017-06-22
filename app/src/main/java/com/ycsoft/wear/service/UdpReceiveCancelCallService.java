package com.ycsoft.wear.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zhangjun on 2016/12/28.
 * 接收盒子广播呼叫服务消息服务类
 */

public class UdpReceiveCancelCallService extends Service {
    private static final String TAG = "UdpReceiveCancelService";
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    public void onCreate() {
        sharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        x.task().run(new Runnable() {

            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                //在这里同样使用约定好的端口
                int port = SocketConstants.PORT_UDP_CANCEL_CALL_SERVICE_RECEIVE;
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
                            JSONObject jsonObject = new JSONObject(receivedMessage);
                            if (jsonObject.getString(SpfConstants.KEY_AREA_NAME).equals(sharedPreferenceUtil
                                    .getString(SpfConstants.KEY_AREA_NAME, ""))) {
                                String name = jsonObject.getString("name");
                                //3.通知提示已有服务员某某先确认了服务和取消震动并关闭提示的呼叫服务对话框
                                mHandler.obtainMessage(CANCEL_CALL, name).sendToTarget();
                                Log.d(TAG, "address : " + packet.getAddress() +
                                        "\ncontent : " + receivedMessage);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 取消呼叫
     */
    private static final int CANCEL_CALL = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CANCEL_CALL:
                    sharedPreferenceUtil.removeKey(SpfConstants.KEY_ROOM_NUMBER);
                    sharedPreferenceUtil.removeKey(SpfConstants.KEY_NEED_VIBRATE);
                    Intent intent = new Intent(Constants.BC_CANCEL_SERVICE_DIALOG);
                    intent.putExtra("info", (String) msg.obj);
                    sendBroadcast(intent);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
