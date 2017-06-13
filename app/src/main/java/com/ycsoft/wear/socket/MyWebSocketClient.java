package com.ycsoft.wear.socket;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjun on 2017/1/21.
 * WebSocketClient
 */

public class MyWebSocketClient extends WebSocketClient {
    private static final String TAG = "MyWebSocketClient";
    private SharedPreferenceUtil mSharedPreferenceUtil;
    private Handler mHandler;
    private Context mContext;

    public MyWebSocketClient(Context context, Handler handler, URI serverURI) {
        this(context, handler, serverURI, new Draft_17());
    }

    public MyWebSocketClient(Context context, Handler handler, URI serverUri, Draft draft) {
        this(context, handler, serverUri, draft, new HashMap<String, String>(), 1000);
    }

    public MyWebSocketClient(Context context, Handler handler, URI serverUri, Draft draft,
                             Map<String, String> headers, int connectTimeout) {
        super(serverUri, draft, headers, connectTimeout);
        this.mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
        this.mHandler = handler;
        this.mContext = context;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.d(TAG, "onOpen: WebSocket连接打开了！");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            switch (jsonObject.getString("action")) {
                case SocketConstants.ACTION_CALL_SERVICE:
                    //---收到请求呼叫服务命令
                    callService(jsonObject);
                    break;
                case SocketConstants.ACTION_ACCEPT_SERVICE:
                    //收到接受呼叫服务返回结果
                    mHandler.obtainMessage(WebSocketService.ACCEPT_SERVICE_RESULT,
                            jsonObject.getBoolean("result")).sendToTarget();
                    break;
                case SocketConstants.ACTION_FINISHED_SERVICE:
                    //收到确认完成服务返回结果
                    mHandler.obtainMessage(WebSocketService.FINISHED_SERVICE_RESULT,
                            jsonObject.getBoolean("result")).sendToTarget();
                    break;
                case SocketConstants.ACTION_CANCEL_SERVICE:
                    //---收到取消呼叫服务命令
                    mHandler.obtainMessage().sendToTarget();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 呼叫服务
     *
     * @param jsonObject
     * @throws JSONException
     */
    private void callService(JSONObject jsonObject) throws JSONException {
        String roomNumber = jsonObject.getString(SpfConstants.KEY_ROOM_NUMBER);
        String floor = jsonObject.getString(SpfConstants.KEY_AREA_NAME);
        if (floor.equals(mSharedPreferenceUtil.getString(SpfConstants.KEY_AREA_NAME, ""))
                && mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
            //如果楼层和登录楼层相同，切没有正在服务的房间号则提醒服务员
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_ROOM_NUMBER, roomNumber);
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_NEED_VIBRATE, true);
            if (!isScreenOn()) {
                ToolUtil.wakeUpScreen(mContext);
            }
            mHandler.obtainMessage(WebSocketService.CALL_SERVICE).sendToTarget();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Constants.isConnectedServer = false;
        ToastUtil.showToast(mContext, "与服务器连接关闭了！", true);
        Log.d(TAG, "onClose: \ncode=" + code + "\nreason=" + reason + "\nremote=" + remote);
        if (remote) {
            //服务器端主动断开了连接
            //根据code判断服务器断开连接的原因
            if(code == 1006){
                //客户端断网了服务器端主动关闭连接
            } else {
                //已经在其它地方登录了
                mHandler.obtainMessage(WebSocketService.GO_TO_LOGIN).sendToTarget();
            }
        }
//        else {
//            //本客户端主动断开了连接
//            if (mReConnectedCount > 0) {
//                mReConnectedCount = 0;
//            }
//            mReConnectedCount++;
//            if (mReConnectedCount <= 3) {
//                mHandler.obtainMessage(WebSocketService.RE_CONNECT_SERVER).sendToTarget();
//            }
//        }
    }

    @Override
    public void onError(Exception ex) {
        Constants.isConnectedServer = false;
        ToastUtil.showToast(mContext, "与服务器连接错误！", true);
        ex.printStackTrace();
        Log.d(TAG, "onError: " + ex.getMessage());
    }

    /**
     * 判断屏幕是否处于息屏状态
     *
     * @return
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }
}
