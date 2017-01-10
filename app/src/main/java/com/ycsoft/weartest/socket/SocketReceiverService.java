package com.ycsoft.weartest.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.common.SocketConstants;
import com.ycsoft.weartest.ui.activity.MainActivity;
import com.ycsoft.weartest.util.SharedPreferenceUtil;
import com.ycsoft.weartest.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zhangjun on 2016/12/28.
 * 接收服务器Socket消息的服务
 */

public class SocketReceiverService extends Service {
    private static final String TAG = "SocketReceiverService";
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    public void onCreate() {
        sharedPreferenceUtil = new SharedPreferenceUtil(this, Constants.SPF_NAME);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        x.task().run(new Runnable() {

            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                //在这里同样使用约定好的端口
                int port = SocketConstants.BC_PORT_CALL_SERVICE_RECEIVE;
                DatagramSocket server;
                try {
                    server = new DatagramSocket(port);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    while (true) {
                        try {
                            //接收服务器广播消息
                            server.receive(packet);
                            String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                            Log.d(TAG, "address : " + packet.getAddress()
                                    + "\nport : " + packet.getPort() + "\ncontent : " + s);
                            //设置接收到的盒子的ip
                            Constants.BOX_IP = packet.getAddress();
                            JSONObject jsonObject = new JSONObject(s);
                            String roomNumber = jsonObject.getString("roomNumber");
                            sharedPreferenceUtil.setValue("roomNumber", roomNumber);
                            mHandler.sendEmptyMessage(OPEN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
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

    private static final int OPEN = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN:
                    ToastUtil.showToast(SocketReceiverService.this,
                            sharedPreferenceUtil.getString("roomNumber", "")
                                    + " 客户正在呼叫服务！", true);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
            }
        }
    };
}
