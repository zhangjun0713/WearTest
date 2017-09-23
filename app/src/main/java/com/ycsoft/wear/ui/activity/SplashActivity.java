package com.ycsoft.wear.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.UdpReceiveServerIpService;
import com.ycsoft.wear.socket.UdpSendBroadcast;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.util.NetworkUtil;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhang on 2017-1-17.
 * 启动界面
 */

public class SplashActivity extends BaseActivity {
    @BindView(R.id.iv_register)
    ImageView ivRegister;
    @BindView(R.id.btn_connect_server)
    Button btnConnectServer;
    @BindView(R.id.et_server_ip)
    EditText etServerIp;
    private SharedPreferenceUtil mSharedPreferenceUtil;
    private BroadcastReceiver mReceiver;
    public static final String BC_CONNECTED_SERVER = "bc_connected_server";

    @Override
    protected void initActivity() {
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
        initReceiver();
        startFindServer();
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter(BC_CONNECTED_SERVER);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ivRegister.setBackgroundResource(R.drawable.icon_connect_server);
                btnConnectServer.setVisibility(View.GONE);
                ToastUtil.showToast(getApplicationContext(), "已发现开启的服务器！", true);
                goMainActivity();
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 进入主页
     */
    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void initView() {
        etServerIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnConnectServer.setText("连接服务器");
                } else {
                    btnConnectServer.setText("扫描连接服务器");
                }
            }
        });
        if (mSharedPreferenceUtil.getString(SpfConstants.KEY_SERVER_IP, "").equals("")) {
            ivRegister.setBackgroundResource(R.drawable.icon_disconnect_server);
            btnConnectServer.setVisibility(View.VISIBLE);
            etServerIp.setVisibility(View.VISIBLE);
        } else {
            ivRegister.setBackgroundResource(R.drawable.icon_connect_server);
            btnConnectServer.setVisibility(View.GONE);
            etServerIp.setVisibility(View.GONE);
            goMainActivity();
        }
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.btn_connect_server)
    void onClick() {
        if (TextUtils.isEmpty(etServerIp.getText())) {
            startFindServer();
        } else {
            String serverIp = etServerIp.getText().toString();
            if (ToolUtil.isIPFormat(serverIp)) {
                mSharedPreferenceUtil.setValue(SpfConstants.KEY_SERVER_IP, serverIp);
                Constants.SERVER_IP = serverIp;
                goMainActivity();
            } else {
                ToastUtil.showToast(this, "IP地址有误，请重新输入！", true);
            }
        }
    }

    /**
     * 开始在局域网中寻找服务器
     */
    private void startFindServer() {
        try {
            if (NetworkUtil.isConnected(this)) {//已连接wifi
                if (ToolUtil.isServiceLive(this, UdpReceiveServerIpService.class.getName())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action", "GET_SERVER_IP");
                    UdpSendBroadcast.sendBroadCastToCenter(this, jsonObject.toString(),
                            SocketConstants.PORT_UDP_GET_SERVER_IP);
                } else {
                    Intent getServerIntent = new Intent(this, UdpReceiveServerIpService.class);
                    this.startService(getServerIntent);
                }
            } else {
                Toast.makeText(getApplicationContext(), "请先连接服务器所在局域网！",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
