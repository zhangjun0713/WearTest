package com.ycsoft.wear.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.ui.activity.LoginActivity;
import com.ycsoft.wear.ui.activity.MainActivity;
import com.ycsoft.wear.socket.MyWebSocketClient;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String URI_SUFFIX = ":80/api/test";
    private ExecutorService executorService;
    private static WebSocketClient webSocketClient;
    private SharedPreferenceUtil mSharedPreferenceUtil;

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
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
        executorService = Executors.newCachedThreadPool();
        executorService.execute(new WebSocketConnect());
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
                    try {
                        JSONObject object = new JSONObject();
                        object.put("action", SocketConstants.ACTION_ACCEPT_SERVICE);
                        object.put("result", true);
                        webSocketClient.send(object.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case CANCEL_SERVICE:
                    //取消呼叫服务
                    Intent cancelCallIntent = new Intent(Constants.BC_SHOW_CANCEL_SERVICE_DIALOG);
                    sendBroadcast(cancelCallIntent);
                    break;
                case RE_CONNECT_SERVER:
                    //与服务器连接异常或关闭了需要重新连接
                    executorService.execute(new WebSocketConnect());
                    break;
                case GO_TO_LOGIN:
                    //1.已经在其它地方登录了或者退出登录了，需要重新登录
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                    //2.如果是锁屏状态下需要发送通知信息提示用户
                    showCancelNotification(intent);
                    //3.清除本机登录信息
                    clearLoginInfo();
                    break;
                case ACCEPT_SERVICE_RESULT:
                    if ((boolean) msg.obj) {
                        ToastUtil.showToast(getApplicationContext(), "请尽快去\n" + mSharedPreferenceUtil
                                .getString(SpfConstants.KEY_ROOM_NUMBER, "") + "\n服务！", true);
                    } else {
                        ToastUtil.showToast(getApplicationContext(), "已经有其他服务业先接受了服务请求！", true);
                    }
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
     * 清除登录信息
     */
    private void clearLoginInfo() {
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_ID);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_NAME);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_FLOOR);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_IS_LOGIN);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_ROOM_NUMBER);
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_NEED_VIBRATE);
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
                        new URI(URI_PREFIX + Constants.SERVER_IP + URI_SUFFIX));
                boolean b = webSocketClient.connectBlocking();
                if (b) {
                    Log.d(TAG, "run: 与服务器连接成功！");
                } else {
                    Log.d(TAG, "run: 与服务器连接失败！");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示提醒
     */
    private void showCancelNotification(Intent intent) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("已经在其它地方登录了或者退出登录了！")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("重新登录提醒！")
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, intent, 0))
                .setContentText(mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "")
                        + "已经在其它地方登录了或者退出登录了！")
                .getNotification();
        notificationManager.notify(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webSocketClient.close();
        webSocketClient = null;
    }
}
