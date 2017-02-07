package com.ycsoft.wear.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangjun on 2017/1/5.
 * 登录界面
 */

public class LoginActivity extends BaseActivity {
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
    }

    /**
     * 跳到主页
     */
    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
        mSharedPreferenceUtil.setValue(SpfConstants.KEY_ID, name);
        mSharedPreferenceUtil.setValue(SpfConstants.KEY_PWD, password);
        mSharedPreferenceUtil.setValue(SpfConstants.KEY_FLOOR, floor);
        login();
    }

    private void login() {
        //调HTTP接口登录
        Callback.CommonCallback<String> callback = new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("Result")) {
                        //1.存储帐号和密码
                        mSharedPreferenceUtil.setValue(SpfConstants.KEY_IS_LOGIN, true);
                        mSharedPreferenceUtil.setValue(SpfConstants.KEY_NAME, jsonObject.getString("Name"));
                        //2.登录成功，获取Token
                        String token = jsonObject.getString("Token");
                        WebSocketService.URI_TOKEN = "token=" + token;
                        //3.启动WebSocketService，启动后自动去连接上服务器
                        if (!ToolUtil.isServiceLive(getApplicationContext(), WebSocketService.class.getName())) {
                            Intent intent = new Intent(getApplicationContext(), WebSocketService.class);
                            startService(intent);
                        }
                        //4.跳转到主界面
                        finish();
                        goMainActivity();
                    } else {
                        //登录失败
                        ToastUtil.showToast(getApplicationContext(), "登录失败，请重试！", true);
                        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_ID);
                        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_PWD);
                        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_FLOOR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                cex.printStackTrace();
            }

            @Override
            public void onFinished() {
            }
        };
        ToolUtil.getToken(this, callback);
    }
}