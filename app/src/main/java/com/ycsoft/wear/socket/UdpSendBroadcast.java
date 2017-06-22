package com.ycsoft.wear.socket;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.xutils.x;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by zhangjun on 2016/12/28.
 * 向局域网发送广播的UDP类
 */

public class UdpSendBroadcast {
    /**
     * 发送广播
     *
     * @param context
     * @param message 要发送的消息
     */
    public static void sendBroadCastToCenter(final Context context, final String message, final int port) {
        x.task().run(new Runnable() {
            @Override
            public void run() {
                WifiManager wifiMgr = (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo;
                //这里获取了IP地址，获取到的IP地址还是int类型的
                wifiInfo = wifiMgr.getConnectionInfo();
                if (wifiInfo != null) {//连上了无线网
                    DatagramSocket theSocket = null;
                    try {
                        InetAddress server = InetAddress.getByName("255.255.255.255");
                        theSocket = new DatagramSocket();
                        DatagramPacket theOutput = new DatagramPacket(message.getBytes(), message.length(), server,
                                port);
                        theSocket.send(theOutput);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (theSocket != null)
                            theSocket.close();
                    }
                }
            }
        });
    }
}
