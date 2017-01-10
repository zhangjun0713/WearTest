package com.ycsoft.weartest.socket;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.common.SocketConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhangjun on 2016/12/28.
 * 连接盒子Socket通信类
 */

public class SendToBoxSocketClient implements Runnable {
    private static final String TAG = "SendToBoxSocketClient";
    private Socket mSocket;
    private ExecutorService mExecutorService;
    private JSONObject message;
    private BufferedReader in;
    private OutputStream out;
    private String responseContent;
    private Handler mHandler;
    private String staffName;

    public SendToBoxSocketClient(String staffName, @Nullable Handler handler) {
        if (handler != null) {
            this.mHandler = handler;
        }
        this.staffName = staffName;
        mExecutorService = Executors.newCachedThreadPool();
        try {
            if (Constants.BOX_IP != null) {
                //启动线程发送和接收消息
                mExecutorService.execute(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给发送方返回信息
     */
    private void sendMessage(String staffName) {
        message = new JSONObject();
        try {
            message.put("name", staffName);
            byte[] bytes = message.toString().getBytes("UTF-8");
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            //建立Socket连接
            mSocket = new Socket(Constants.BOX_IP, SocketConstants.PORT_RESPONSE_SERVICE);
            out = mSocket.getOutputStream();
            sendMessage(staffName);
            in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            //发送消息
            while (!mSocket.isClosed()) {
                if (mSocket.isConnected()) {
                    if (!mSocket.isInputShutdown() && mHandler != null) {
                        if ((responseContent = in.readLine()) != null) {
                            Log.d(TAG, responseContent);
                            mHandler.obtainMessage(1, responseContent).sendToTarget();
                            //处理完了返回消息，关闭连接
                            in.close();
                            out.close();
                            mSocket.close();
                        } else {
                            mHandler.obtainMessage(1, "no").sendToTarget();
                            //处理完了返回消息，关闭连接
                            in.close();
                            out.close();
                            mSocket.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}