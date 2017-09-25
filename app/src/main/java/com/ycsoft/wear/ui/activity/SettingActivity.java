package com.ycsoft.wear.ui.activity;

import android.widget.EditText;
import android.widget.Toast;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToolUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhang on 2017-09-25.
 * 设置界面
 */

public class SettingActivity extends BaseActivity {
    @BindView(R.id.et_server_ip)
    EditText etServerIp;
    SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    protected void initActivity() {
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
    }

    @Override
    protected void initView() {
        String serverIp = mSharedPreferenceUtil.getString(SpfConstants.KEY_SERVER_IP, "");
        etServerIp.setText(serverIp);
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.btn_ok)
    void onClick() {
        String serverIp = etServerIp.getText().toString();
        if (ToolUtil.isIPFormat(serverIp)) {
            mSharedPreferenceUtil.setValue(SpfConstants.KEY_SERVER_IP, etServerIp.getText().toString().trim());
            Constants.SERVER_IP = etServerIp.getText().toString();
            this.finish();
        } else {
            Toast.makeText(getApplicationContext(), "输入的IP地址有误！",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
