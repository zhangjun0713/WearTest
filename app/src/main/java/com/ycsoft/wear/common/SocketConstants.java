package com.ycsoft.wear.common;

/**
 * Created by zhangjun on 2017/1/5.
 * Socket通信相关常量类
 */
public class SocketConstants {
    /**
     * 获取服务器IP地址广播端口
     */
    public static final int PORT_UDP_GET_SERVER_IP = 9001;
    /**
     * 接收服务器IP端口
     */
    public static final int PORT_UDP_RECEIVE_SERVER_IP = 9002;
    /**
     * 请求呼叫服务Socket接收端口
     */
    public static final int PORT_TCP_CALL_SERVICE_RECEIVE = 9003;
    /**
     * 取消呼叫服务广播接收端口
     */
    public static final int PORT_UDP_CANCEL_CALL_SERVICE_RECEIVE = 9004;
}
