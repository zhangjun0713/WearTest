package com.ycsoft.wear.port;

/**
 * Created by zhang on 2017-09-23.
 * WebSocketClient接收到消息回调接口类
 */

public interface IMessageCallback {
    /**
     * 消息回调
     *
     * @param message
     */
    void onMessage(String message);

    /**
     * 连接中断了
     */
    void onClose(boolean remote);

    /**
     * 连接错误
     */
    void onError();
}
