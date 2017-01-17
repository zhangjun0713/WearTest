package com.ycsoft.wear.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.ui.activity.MainActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhang on 2017-1-14.
 * 接收呼叫服务Socket服务
 */

public class TcpReceiveCallService extends Service implements Runnable {
    private static final String TAG = "SocketReceiveService";
    ExecutorService executorService;
    private SharedPreferenceUtil sharedPreferenceUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
        executorService.execute(this);
        sharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(SocketConstants.PORT_TCP_CALL_SERVICE_RECEIVE);
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
                    JSONObject jsonObject = new JSONObject(message);
                    String roomNumber = jsonObject.getString(SpfConstants.KEY_ROOM_NUMBER);
                    String floor = jsonObject.getString(SpfConstants.KEY_FLOOR);
                    if (floor.equals(sharedPreferenceUtil.getString(SpfConstants.KEY_FLOOR, ""))
                            && sharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
                        sharedPreferenceUtil.setValue(SpfConstants.KEY_ROOM_NUMBER, roomNumber);
                        sharedPreferenceUtil.setValue(SpfConstants.KEY_NEED_VIBRATE, true);
                        mHandler.sendEmptyMessage(OPEN);
                    }
                    out.write("ok".getBytes());
                    mHandler.sendEmptyMessage(OPEN);
                }
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final int OPEN = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN:
                    if (getTopActivity(getApplicationContext()).equals(MainActivity.class.getName())) {
                        Intent intent = new Intent(Constants.BC_SHOW_CALL_SERVICE_DIALOG);
                        sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
            }
        }
    };

    /**
     * 获取当前显示的Activity
     *
     * @param context
     * @return
     */
    private String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList = manager.getRunningTasks(1);
        if (runningTaskInfoList != null)
            return runningTaskInfoList.get(0).topActivity.getClassName();
        else
            return "";
    }
}
