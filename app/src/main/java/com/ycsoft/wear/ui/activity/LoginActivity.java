package com.ycsoft.wear.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangjun on 2017/1/5.
 * 登录界面
 */

public class LoginActivity extends BaseActivity {
    /**
     * 广播通知登录结果
     */
    public static final String BC_LOGIN_RESULT = "bc_login_result";
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_floor)
    EditText etFloor;
    @BindView(R.id.et_password)
    EditText etPassword;
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    protected void initActivity() {
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
        initReceiver();
    }

    /**
     * 初始化广播接收器
     */
    private void initReceiver() {
        IntentFilter filter = new IntentFilter(BC_LOGIN_RESULT);
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("result", false)) {
                    //登录成功，提示
                    Toast.makeText(getApplication(), "登录成功！", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    //登录失败，提示
                    Toast.makeText(getApplication(), "登录失败！请重试！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
    }

    @OnClick(R.id.btn_login)
    public void onClick(View v) {
        if (TextUtils.isEmpty(etName.getText().toString())) {
            Toast.makeText(this, "请输入服务员姓名或编号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etFloor.getText().toString())) {
            Toast.makeText(this, "请输入您服务楼层", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String name, floor, password;
        name = etName.getText().toString().trim();
        floor = etFloor.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        login(name, floor, password);
    }

    private void login(final String name, final String floor, String password) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SpfConstants.KEY_ID, name);
            jsonObject.put(SpfConstants.KEY_FLOOR, floor);
            jsonObject.put(SpfConstants.KEY_PWD, password);
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_PWD, password);
            WebSocketClient mWebSocketClient = WebSocketService.getWebSocketClient();
            if (mWebSocketClient != null) {
                mWebSocketClient.send(jsonObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
