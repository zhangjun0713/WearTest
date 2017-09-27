package com.ycsoft.wear.service;

import android.annotation.SuppressLint;
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
import com.ycsoft.wear.port.IMessageCallback;
import com.ycsoft.wear.socket.MyWebSocketClient;
import com.ycsoft.wear.ui.activity.MainActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhangjun on 2017-1-14.
 * 接收呼叫服务WebSocket服务
 */

public class WebSocketService extends Service implements IMessageCallback {
    private static final String TAG = "WebSocketService";
    private static final String URI_PREFIX = "ws://";
    private static final String URI_SUFFIX = ":80/api/waiter?";
    public static String URI_TOKEN = "";
    private static WebSocketClient webSocketClient;
    private ExecutorService executorService;
    private SharedPreferenceUtil mSharedPreferenceUtil;
    /**
     * 是否成功接受了服务
     */
    private boolean acceptSuccess;

    /**
     * 获取WebSocketClient
     *
     * @return
     */
    public static WebSocketClient getWebSocketClient() {
        return webSocketClient == null ? null : webSocketClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(5);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executorService.submit(new WebSocketConnect());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 重新连接服务器
     */
    public static final int RE_CONNECT_SERVER = 1;
    /**
     * 进入登录界面
     */
    public static final int GO_TO_LOGIN = 2;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RE_CONNECT_SERVER:
                    //与服务器连接异常或关闭了需要重新连接
                    ToolUtil.getToken(getApplicationContext(), mCallback);
                    break;
                case GO_TO_LOGIN:
                    //震动5s提醒服务员
                    ToolUtil.startVibrate(getApplicationContext(), 5);
                    if (getTopActivity(getApplicationContext()).equals(MainActivity.class.getName())) {
                        Intent intent = new Intent(Constants.BC_GO_LOGIN);
                        sendBroadcast(intent);
                    } else {
                        Constants.NEED_RE_LOGIN = true;
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

    @Override
    public void onMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            switch (jsonObject.getString("action")) {
                case SocketConstants.ACTION_CALL_SERVICE:
                    //---收到请求呼叫服务命令
                    handleCallService(jsonObject);
                    break;
                case SocketConstants.ACTION_ACCEPT_SERVICE:
                    //收到接受呼叫服务返回结果
                    acceptSuccess = jsonObject.getBoolean("result");
                    Intent acceptResultIntent = new Intent();
                    acceptResultIntent.putExtra("result", jsonObject.getBoolean("result"));
                    acceptResultIntent.setAction(Constants.BC_ACCEPT_SERVICE_SUCCEED);
                    sendBroadcast(acceptResultIntent);
                    break;
                case SocketConstants.ACTION_FINISHED_SERVICE:
                    //收到确认完成服务返回结果
                    acceptSuccess = false;
                    if (jsonObject.getBoolean("result")) {
                        Intent intent2 = new Intent(Constants.BC_FINISHED_SERVICE_SUCCEED);
                        sendBroadcast(intent2);
                    }
                    break;
                case SocketConstants.ACTION_CANCEL_SERVICE:
                    //---收到取消呼叫服务命令
                    if (!acceptSuccess) {
                        String clientName = jsonObject.getString("clientName");
                        String savedClientName = mSharedPreferenceUtil
                                .getString(SpfConstants.KEY_ROOM_NUMBER, "");
                        if (clientName.equals(savedClientName)) {
                            Intent cancelCallIntent = new Intent(Constants.BC_CANCEL_SERVICE_DIALOG);
                            cancelCallIntent.putExtra(SpfConstants.KEY_ROOM_NUMBER, clientName);
                            sendBroadcast(cancelCallIntent);
                        }
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, boolean remote) {
        if (remote) {
            //服务器端主动断开了连接
            //根据code判断服务器断开连接的原因
            if (code == 1006) {
                //客户端断网了服务器端主动关闭连接
                mHandler.obtainMessage(RE_CONNECT_SERVER).sendToTarget();
            } else {
                //已经在其它地方登录了
                mHandler.obtainMessage(GO_TO_LOGIN).sendToTarget();
            }
        }
    }

    @Override
    public void onError() {

    }

    /**
     * 处理呼叫服务请求
     *
     * @param jsonObject
     * @throws JSONException
     */
    private void handleCallService(JSONObject jsonObject) throws JSONException {
        String roomNumber = jsonObject.getString(SpfConstants.KEY_ROOM_NUMBER);
        String floor = jsonObject.getString(SpfConstants.KEY_AREA_NAME);
        if (floor.equals(mSharedPreferenceUtil.getString(SpfConstants.KEY_AREA_NAME, ""))
                && mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
            //如果楼层和登录楼层相同，且没有正在服务的房间号则提醒服务员
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_ROOM_NUMBER, roomNumber);
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_NEED_VIBRATE, true);
            if (!ToolUtil.isScreenOn(this)) {
                ToolUtil.wakeUpScreen(this);
            }
        }
        if (getTopActivity(getApplicationContext()).equals(MainActivity.class.getName())) {
            Intent intent = new Intent(Constants.BC_SHOW_CALL_SERVICE_DIALOG);
            sendBroadcast(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * 建立WebSocket连接线程类
     */
    private class WebSocketConnect implements Runnable {
        @Override
        public void run() {
            try {
                //建立WebSocket连接
                if (webSocketClient != null) {
                    webSocketClient.close();
                    webSocketClient = null;
                }
                webSocketClient = new MyWebSocketClient(getApplication(),
                        new URI(URI_PREFIX + Constants.SERVER_IP + URI_SUFFIX + URI_TOKEN), WebSocketService.this);
                boolean b = webSocketClient.connectBlocking();
                if (b) {
                    Constants.isConnectedServer = true;
                    Log.d(TAG, "run: 与服务器连接成功！");
                } else {
                    Constants.isConnectedServer = false;
                    Log.d(TAG, "run: 与服务器连接失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在此回调对象中处理获取Token结果
     */
    private Callback.CommonCallback<String> mCallback = new Callback.CommonCallback<String>() {
        @Override
        public void onSuccess(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getBoolean("result")) {
                    if (jsonObject.getBoolean("result")) {
                        //1.登录成功，获取Token
                        String token = jsonObject.getString("token");
                        URI_TOKEN = "token=" + token;
                        //2.清除之前连接的对象
                        webSocketClient.close();
                        webSocketClient = null;
                        //3.重新连接服务器
                        executorService.submit(new WebSocketConnect());
                    } else {
                        //自动重新登录失败
                        ToastUtil.showToast(getApplicationContext(), "自动重新登录失败！", true);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            ex.printStackTrace();
            ToastUtil.showToast(getApplicationContext(), "访问服务器失败！", true);
        }

        @Override
        public void onCancelled(CancelledException cex) {
            cex.printStackTrace();
        }

        @Override
        public void onFinished() {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        webSocketClient.close();
        webSocketClient = null;
        URI_TOKEN = "";
        if (new SharedPreferenceUtil(getApplicationContext(), SpfConstants.SPF_NAME)
                .getBoolean(SpfConstants.KEY_IS_LOGIN, false)) {
            //当前是登录状态的情况下证明服务是被系统杀掉的，立即重启服务连接服务器
            ToastUtil.showToast(this, "服务异常销毁了，即将重启服务！", true);
            Intent intent = new Intent(getApplication(), WebSocketService.class);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            getApplication().startService(intent);
        }
    }
}
