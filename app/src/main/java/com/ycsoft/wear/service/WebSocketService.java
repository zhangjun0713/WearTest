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

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";
    private static final String URI_PREFIX = "ws://";
    private static final String URI_SUFFIX = ":80/api/waiter?";
    public static String URI_TOKEN = "";
    private ExecutorService executorService;
    private static WebSocketClient webSocketClient;

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
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executorService.execute(new WebSocketConnect());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 打开主界面
     */
    public static final int CALL_SERVICE = 1;
    /**
     * 取消呼叫服务
     */
    public static final int CANCEL_SERVICE = 2;
    /**
     * 重新连接服务器
     */
    public static final int RE_CONNECT_SERVER = 3;
    /**
     * 进入登录界面
     */
    public static final int GO_TO_LOGIN = 4;
    /**
     * 接受呼叫服务结果
     */
    public static final int ACCEPT_SERVICE_RESULT = 5;
    /**
     * 点击完成服务结果
     */
    public static final int FINISHED_SERVICE_RESULT = 6;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CALL_SERVICE:
                    //呼叫服务
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
                case CANCEL_SERVICE:
                    //取消呼叫服务
                    Intent cancelCallIntent = new Intent(Constants.BC_CANCEL_SERVICE_DIALOG);
                    sendBroadcast(cancelCallIntent);
                    break;
                case RE_CONNECT_SERVER:
                    //与服务器连接异常或关闭了需要重新连接
                    ToolUtil.getToken(getApplicationContext(), mCallback);
                    break;
                case ACCEPT_SERVICE_RESULT:
                    Intent acceptResultIntent = new Intent();
                    acceptResultIntent.putExtra("result", (boolean) msg.obj);
                    acceptResultIntent.setAction(Constants.BC_ACCEPT_SERVICE_SUCCEED);
                    sendBroadcast(acceptResultIntent);
                    break;
                case FINISHED_SERVICE_RESULT:
                    if ((boolean) msg.obj) {
                        //成功确认完成服务，广播通知主界面更新显示
                        Intent intent2 = new Intent(Constants.BC_FINISHED_SERVICE_SUCCEED);
                        sendBroadcast(intent2);
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
                webSocketClient = new MyWebSocketClient(getApplication(), mHandler,
                        new URI(URI_PREFIX + Constants.SERVER_IP + URI_SUFFIX + URI_TOKEN));
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
                if (jsonObject.getBoolean("Result")) {
                    if (jsonObject.getBoolean("Result")) {
                        //1.登录成功，获取Token
                        String token = jsonObject.getString("Token");
                        URI_TOKEN = "token=" + token;
                        //2.清除之前连接的对象
                        webSocketClient.close();
                        webSocketClient = null;
                        URI_TOKEN = "";
                        //3.重新连接服务器
                        executorService.execute(new WebSocketConnect());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {

        }

        @Override
        public void onCancelled(CancelledException cex) {

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
        ToastUtil.showToast(this, "WebSocketService停止了！", true);
        //如果是异常停止则需要重启服务
        if (new SharedPreferenceUtil(getApplicationContext(), SpfConstants.SPF_NAME).getBoolean(SpfConstants.KEY_IS_LOGIN, false)) {
            Intent intent = new Intent(getApplication(), WebSocketService.class);
            getApplication().startService(intent);
        }
    }
}