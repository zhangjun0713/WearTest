package com.ycsoft.wear.socket;

import android.content.Context;
import android.util.Log;

import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.port.IMessageCallback;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjun on 2017/1/21.
 * WebSocketClient
 */

public class MyWebSocketClient extends WebSocketClient {
    private static final String TAG = "MyWebSocketClient";
    private Context mContext;

    private IMessageCallback messageCallback;

    public MyWebSocketClient(Context context, URI serverURI, IMessageCallback callback) {
        this(context, serverURI, new Draft_17(), callback);
    }

    public MyWebSocketClient(Context context, URI serverUri, Draft draft, IMessageCallback callback) {
        this(context, serverUri, draft, new HashMap<String, String>(), 1000, callback);
    }

    public MyWebSocketClient(Context context, URI serverUri, Draft draft,
                             Map<String, String> headers, int connectTimeout, IMessageCallback callback) {
        super(serverUri, draft, headers, connectTimeout);
        this.mContext = context;
        this.messageCallback = callback;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.d(TAG, "onOpen: WebSocket连接打开了！");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "onMessage: " + message);
        if (messageCallback != null) {
            messageCallback.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Constants.isConnectedServer = false;
        Log.d(TAG, "onClose: \ncode=" + code + "\nreason=" + reason + "\nremote=" + remote);
        if (remote) {
            //服务器端主动断开了连接
            //根据code判断服务器断开连接的原因
            if (code == 1006) {
                //客户端断网了服务器端主动关闭连接
            } else {
                //已经在其它地方登录了
            }
        }
        if (messageCallback != null) {
            messageCallback.onClose(remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        Constants.isConnectedServer = false;
        ex.printStackTrace();
        Log.d(TAG, "onError: " + ex.getMessage());
        if (messageCallback != null) {
            messageCallback.onError();
        }
    }
}
