package com.ycsoft.wear.socket;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.ui.activity.LoginActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * Created by zhangjun on 2017/1/21.
 * WebSocketClient
 */

public class MyWebSocketClient extends WebSocketClient {
    private static final String TAG = "MyWebSocketClient";
    private SharedPreferenceUtil mSharedPreferenceUtil;
    private Handler mHandler;
    private Context mContext;
    private int mReConnectedCount;

    public MyWebSocketClient(Context context, Handler handler, URI serverURI) {
        this(context, handler, serverURI, new Draft_17());
    }

    public MyWebSocketClient(Context context, Handler handler, URI serverUri, Draft draft) {
        this(context, handler, serverUri, draft, new HashMap<String, String>(), 1000);
    }

    public MyWebSocketClient(Context context, Handler handler, URI serverUri, Draft draft,
                             Map<String, String> headers, int connecttimeout) {
        super(serverUri, draft, headers, connecttimeout);
        this.mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
        this.mHandler = handler;
        this.mContext = context;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "onOpen: WebSocket连接打开了！");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            switch (jsonObject.getString("action")) {
                case SocketConstants.ACTION_LOGIN:
                    //登录，连接服务器
                    handleLoginResult(jsonObject);
                    break;
                case SocketConstants.ACTION_LOGOUT:
                    //跳入登录界面
                    mHandler.obtainMessage(WebSocketService.GO_TO_LOGIN).sendToTarget();
                    break;
                case SocketConstants.ACTION_CALL_SERVICE:
                    //收到呼叫服务请求
                    callService(jsonObject);
                    break;
                case SocketConstants.ACTION_ACCEPT_SERVICE:
                    //接受呼叫服务
                    mHandler.obtainMessage(WebSocketService.ACCEPT_SERVICE_RESULT,
                            jsonObject.getBoolean("result")).sendToTarget();
                    break;
                case SocketConstants.ACTION_FINISHED_SERVICE:
                    //确认完成服务
                    mHandler.obtainMessage(WebSocketService.FINISHED_SERVICE_RESULT,
                            jsonObject.getBoolean("result")).sendToTarget();
                    break;
                case SocketConstants.ACTION_CANCEL_SERVICE:
                    //取消呼叫服务
                    mHandler.obtainMessage().sendToTarget();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理登录结果
     *
     * @param jsonObject
     * @throws JSONException
     */
    private void handleLoginResult(JSONObject jsonObject) throws JSONException {
        if (jsonObject.getBoolean("result")) {
            //登录成功
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_IS_LOGIN, true);
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_ID,
                    jsonObject.getString(SpfConstants.KEY_ID));
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_NAME,
                    jsonObject.getString(SpfConstants.KEY_NAME));
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_FLOOR,
                    jsonObject.getString(SpfConstants.KEY_FLOOR));
        } else {
            //登录失败，删除存入配置文件的密码
            mSharedPreferenceUtil.removeKey(SpfConstants.KEY_PWD);
        }
        //广播通知登录结果
        Intent intent = new Intent(LoginActivity.BC_LOGIN_RESULT);
        intent.putExtra("result", jsonObject.getBoolean("result"));
        mContext.sendBroadcast(intent);
    }

    /**
     * 呼叫服务
     *
     * @param jsonObject
     * @throws JSONException
     */
    private void callService(JSONObject jsonObject) throws JSONException {
        String roomNumber = jsonObject.getString(SpfConstants.KEY_ROOM_NUMBER);
        String floor = jsonObject.getString(SpfConstants.KEY_FLOOR);
        if (floor.equals(mSharedPreferenceUtil.getString(SpfConstants.KEY_FLOOR, ""))
                && mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_ROOM_NUMBER, roomNumber);
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_NEED_VIBRATE, true);
            if (!isScreenOn()) {
                wakeUpScreen();
            }
            mHandler.obtainMessage(WebSocketService.CALL_SERVICE).sendToTarget();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose: \ncode=" + code + "\nreason=" + reason + "\nremote=" + remote);
        if (remote) {
            //服务器端主动断开了连接
            //根据code判断服务器断开连接的原因
            switch (code) {
                case 100:
                    //已经在其它地方登录了
                    mHandler.obtainMessage(WebSocketService.GO_TO_LOGIN).sendToTarget();
                    break;
            }
        } else {
            //本客户端主动断开了连接
        }
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        Log.d(TAG, "onError: " + ex.getMessage());
        mReConnectedCount++;
        if (mReConnectedCount <= 3) {
            mHandler.obtainMessage(WebSocketService.RE_CONNECT_SERVER).sendToTarget();
        }
    }

    /**
     * 判断屏幕是否处于息屏状态
     *
     * @return
     */
    public boolean isScreenOn() {
        PowerManager pm = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        if (pm.isScreenOn()) {
            return true;
        }
        return false;
    }

    /**
     * 唤醒屏幕
     */
    private void wakeUpScreen() {
        PowerManager.WakeLock mWakelock;
        PowerManager pm = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | FLAG_KEEP_SCREEN_ON, "SimpleTimer");
        mWakelock.acquire();
        if (isLockedScreen()) {
            showNotification();
        }
    }

    /**
     * 判断屏幕是否锁屏状态
     *
     * @return
     */
    private boolean isLockedScreen() {
        KeyguardManager mKeyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);
        return !mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 显示提醒
     */
    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "")
                        + " 客户正在呼叫服务！")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("呼叫服务")
                .setContentText(mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "")
                        + " 客户正在呼叫服务！")
                .getNotification();
        notificationManager.notify(1, notification);
    }
}
