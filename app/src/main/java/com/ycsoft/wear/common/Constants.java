package com.ycsoft.wear.common;

/**
 * Created by Jeremy on 2017/1/5.
 * 常量类
 */
public class Constants {
    public static String SERVER_IP;
    /**
     * 广播通知显示呼叫服务对话框
     */
    public static final String BC_SHOW_CALL_SERVICE_DIALOG = "bc_show_call_service_dialog";
    /**
     * 广播通知关闭呼叫服务对话框
     */
    public static final String BC_CANCEL_SERVICE_DIALOG = "bc_cancel_service_dialog";
    /**
     * 广播通知点击完成服务后返回结果
     */
    public static final String BC_FINISHED_SERVICE_SUCCEED = "bc_finished_service_succeed";
    /**
     * 广播通知接受服务成功
     */
    public static final String BC_ACCEPT_SERVICE_SUCCEED = "bc_accept_service_succeed";
    /**
     * 广播通知需要重新登录
     */
    public static final String BC_GO_LOGIN = "bc_go_login";
    /**
     * 当前是否是与服务器建立了WebSocket连接
     */
    public static boolean isConnectedServer;
    /**
     * 需要重新登录
     */
    public static boolean NEED_RE_LOGIN;
}
