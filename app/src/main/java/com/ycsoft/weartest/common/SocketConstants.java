package com.ycsoft.weartest.common;

/**
 * Created by zhangjun on 2017/1/5.
 * Socket通信相关常量类
 */
public class SocketConstants {
    /**
     * 请求服务器IP地址的广播发送端口
     */
    public static final int BC_PORT_REQUEST_SERVER_IP_SEND = 9001;
    /**
     * 接收服务器IP地址的通信端口
     */
    public static final int PORT_REQUEST_SERVER_IP_RECEIVE = 9002;
    /**
     * 请求呼叫服务广播接收端口（机顶盒的发送呼叫服务广播端口）
     */
    public static final int BC_PORT_CALL_SERVICE_RECEIVE = 9003;
    /**
     * 取消呼叫服务广播接收端口（机顶盒的发送取消呼叫服务广播端口）
     */
    public static final int BC_PORT_CANCEL_CALL_SERVICE_RECEIVE = 9004;
    /**
     * 确认服务通信端口
     */
    public static final int PORT_RESPONSE_SERVICE = 9005;
}
