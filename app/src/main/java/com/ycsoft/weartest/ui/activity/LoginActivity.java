package com.ycsoft.weartest.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ycsoft.weartest.R;
import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.ui.BaseActivity;
import com.ycsoft.weartest.util.SharedPreferenceUtil;

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
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, Constants.SPF_NAME);
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
        if (name.equals("admin") && password.equals("admin")) {
            Toast.makeText(this, "登录成功！欢迎admin！", Toast.LENGTH_SHORT).show();
            mSharedPreferenceUtil.setValue("isLogin", true);
            mSharedPreferenceUtil.setValue("name", "admin");
            mSharedPreferenceUtil.setValue("floor", floor);
        } else if (name.equals("guest") && password.equals("guest")) {
            Toast.makeText(this, "登录成功！欢迎guest！", Toast.LENGTH_SHORT).show();
            mSharedPreferenceUtil.setValue("isLogin", true);
            mSharedPreferenceUtil.setValue("name", "guest");
            mSharedPreferenceUtil.setValue("floor", floor);
        } else {
            mSharedPreferenceUtil.setValue("isLogin", false);
            mSharedPreferenceUtil.removeKey("name");
            return;
        }
        //登录成功跳转到主页
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
