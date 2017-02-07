package com.ycsoft.wear.common;

import android.content.Context;
import android.content.Intent;

import com.ycsoft.wear.service.WebSocketService;

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
     * 接受服务api
     */
    public static final String API_ACCEPT_SERVICE = "/api/staff/acceptService";
    /**
     * 完成服务api
     */
    public static final String API_FINISHED_SERVICE = "/api/staff/finishedService";
    /**
     * 登录api
     */
    public static final String API_LOGIN = "/api/staff/login";
    /**
     * 退出登录api
     */
    public static final String API_LOGOUT = "/api/staff/logout";

    /**
     * 当前是否是与服务器建立了WebSocket连接
     */
    public static boolean isConnectedServer;
}
