package com.ycsoft.wear.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhang on 2017-1-14.
 * 接收服务器IP地址的Socket服务
 */

public class SocketReceiveServerIpService extends Service implements Runnable {
    private static final String TAG = "SocketReceiveService";
    ExecutorService executorService;
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
        executorService.execute(this);
        sharedPreferenceUtil = new SharedPreferenceUtil(this, Constants.SPF_NAME);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(SocketConstants.PORT_RECEIVE_SERVER_IP);
            while (true) {
                executorService.execute(new SocketHandlerThread(serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SocketHandlerThread implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private OutputStream out;

        private SocketHandlerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = socket.getOutputStream();
                String message;
                while ((message = in.readLine()) != null) {
                    socket.shutdownInput();
                    Log.d(TAG, "run: " + message);
                    //存储服务器ip地址
                    sharedPreferenceUtil.setValue("serverIp", message);
                    Constants.SERVER_IP = message;
                    out.write("ok".getBytes());
                    mHandler.sendEmptyMessage(STOP_SERVICE);
                }
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final int STOP_SERVICE = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_SERVICE:
                    stopSelf();
                    break;
            }
        }
    };
}
