package com.ycsoft.weartest.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ycsoft.weartest.R;
import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.socket.SocketReceiverService;
import com.ycsoft.weartest.ui.BaseActivity;
import com.ycsoft.weartest.ui.dialog.ResponseServiceDialog;
import com.ycsoft.weartest.util.SharedPreferenceUtil;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.tv_staff_name)
    TextView tvStaffName;
    @BindView(R.id.tv_info)
    TextView tvInfo;
    @BindView(R.id.btn_finished_service)
    Button btnFinishedService;
    @BindString(R.string.dialog_ok)
    String POSITIVE_BUTTON;
    @BindString(R.string.dialog_no)
    String NEGATIVE_BUTTON;
    private SharedPreferenceUtil mSharedPreferenceUtil;

    @Override
    protected void initActivity() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, Constants.SPF_NAME);

        Intent startReceiver = new Intent(this, SocketReceiverService.class);
        startReceiver.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        startService(startReceiver);
    }

    @Override
    protected void initView() {
        if (mSharedPreferenceUtil.getBoolean("isLogin", false)) {
            tvStaffName.setText(mSharedPreferenceUtil.getString("name", ""));
        } else {
            goLoginPage();
            finish();
        }
        if (!mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
            //震动，并显示对话框！
            showServiceDialog("客户呼叫提醒", mSharedPreferenceUtil.getString("roomNumber", ""));
        }
    }

    @Override
    protected void initData() {
    }

    @OnClick({R.id.btn_finished_service, R.id.btn_logout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finished_service:
                mSharedPreferenceUtil.removeKey("roomNumber");
                tvInfo.setText("没有客户呼叫服务");
                //TODO:调用HTTP接口来更新服务器上当前员工的状态
                btnFinishedService.setVisibility(View.GONE);
                break;
            case R.id.btn_logout:
                logoutDialog("退出提示", "真的要退出登录吗？");
                break;
        }
    }

    /**
     * 显示服务提醒对话框
     */
    private void showServiceDialog(String title, String roomNumber) {
        ResponseServiceDialog dialog = new ResponseServiceDialog(this, roomNumber);
        dialog.show(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //更新界面显示
                String roomNumber = mSharedPreferenceUtil.getString("roomNumber", "");
                if (roomNumber.equals("")) {
                    tvInfo.setText("没有客户呼叫服务");
                    btnFinishedService.setVisibility(View.GONE);
                } else {
                    tvInfo.setText("请尽快到客户房间\n" + roomNumber);
                    btnFinishedService.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 退出登录
     */
    private void logoutDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(NEGATIVE_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(POSITIVE_BUTTON, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "\"" +
                                mSharedPreferenceUtil.getString("name", "") + "\"" + "已退出！",
                        Toast.LENGTH_SHORT).show();
                mSharedPreferenceUtil.setValue("isLogin", false);
                mSharedPreferenceUtil.removeKey("name");
                dialog.dismiss();
                //跳转到登录界面
                goLoginPage();
                //TODO:调用HTTP接口来更新服务器上当前员工的状态
            }
        });
        builder.show();
    }

    /**
     * 跳入登录界面
     */
    private void goLoginPage() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
