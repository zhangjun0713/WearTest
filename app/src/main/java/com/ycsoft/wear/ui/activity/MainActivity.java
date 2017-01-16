package com.ycsoft.wear.ui.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.service.SocketReceiveCallService;
import com.ycsoft.wear.service.UdpReceiveCancelCallService;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.ui.dialog.ResponseServiceDialog;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;

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
    private BroadcastReceiver mReceiver;
    private ResponseServiceDialog dialog;

    @Override
    protected void initActivity() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, Constants.SPF_NAME);
        if (!isWorked(UdpReceiveCancelCallService.class.getName())) {
            Intent startReceiver = new Intent(this, UdpReceiveCancelCallService.class);
            startReceiver.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            startService(startReceiver);
        }
        if (!isWorked(SocketReceiveCallService.class.getName())) {
            Intent startReceiver = new Intent(this, SocketReceiveCallService.class);
            startReceiver.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            startService(startReceiver);
        }
        initReceiver();
    }

    public boolean isWorked(String serviceName) {
        ActivityManager myManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService =
                (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化广播接收器
     */
    private void initReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Constants.BC_SHOW_CALL_SERVICE_DIALOG:
                        showServiceDialog(mSharedPreferenceUtil.getString("roomNumber", ""));
                        break;
                    case Constants.BC_SHOW_CANCEL_SERVICE_DIALOG:
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BC_SHOW_CALL_SERVICE_DIALOG);
        intentFilter.addAction(Constants.BC_SHOW_CANCEL_SERVICE_DIALOG);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void initView() {
        if (mSharedPreferenceUtil.getBoolean("isLogin", false)) {
            tvStaffName.setText(mSharedPreferenceUtil.getString("name", ""));
            if (!mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
                btnFinishedService.setVisibility(View.VISIBLE);
            }
        } else {
            goLoginPage();
            finish();
        }
        if (!mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
            if (mSharedPreferenceUtil.getBoolean("needVibrate", false)) {
                //震动，并显示对话框！
                showServiceDialog(mSharedPreferenceUtil.getString("roomNumber", ""));
            } else {
                tvInfo.setText("请尽快到客户房间\n" + mSharedPreferenceUtil.getString("roomNumber", ""));
            }
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
                //TODO:调用HTTP接口来通知服务器服务员完成了服务
                RequestParams params = new RequestParams(Constants.SERVER_IP + Constants.API_FINISHED_SERVICE);
                params.addBodyParameter("id", mSharedPreferenceUtil.getString("id", ""));
                params.addBodyParameter("name", mSharedPreferenceUtil.getString("name", ""));
                params.addBodyParameter("roomNumber", mSharedPreferenceUtil.getString("roomNumber", ""));
                params.setCharset("UTF-8");
                x.http().get(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            //完成了确认完成服务操作
                            mHandler.obtainMessage(FINISHED_SERVICE, jsonObject.getBoolean("result"))
                                    .sendToTarget();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {
                    }

                    @Override
                    public void onFinished() {
                    }
                });
                break;
            case R.id.btn_logout:
                if (mSharedPreferenceUtil.getString("roomNumber", "").equals("")) {
                    logoutDialog("退出提示", "真的要退出登录吗？");
                } else {
                    Toast.makeText(getApplicationContext(), "您当前还没有确认完成服务，不能退出登录！",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 显示服务提醒对话框
     */
    private void showServiceDialog(String roomNumber) {
        ToastUtil.showToast(this,
                mSharedPreferenceUtil.getString("roomNumber", "")
                        + " 客户正在呼叫服务！", true);
        dialog = new ResponseServiceDialog(this, roomNumber);
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
                mHandler.obtainMessage(LOGOUT).sendToTarget();
                dialog.dismiss();
                //跳转到登录界面
                goLoginPage();
                finish();
            }
        });
        builder.show();
    }

    private static final int FINISHED_SERVICE = 1;
    private static final int LOGOUT = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISHED_SERVICE:
                    Toast.makeText(getApplicationContext(), "\"" +
                                    mSharedPreferenceUtil.getString("name", "") + "\"" + "您已完成了当前服务！",
                            Toast.LENGTH_SHORT).show();
                    btnFinishedService.setVisibility(View.GONE);
                    break;
                case LOGOUT:
                    goLogout();
                    break;
            }
        }
    };

    /**
     * 退出登录
     */
    private void goLogout() {
        RequestParams params = new RequestParams(Constants.SERVER_IP + Constants.API_LOGOUT);
        params.addBodyParameter("id", mSharedPreferenceUtil.getString("id", ""));
        params.addBodyParameter("name", mSharedPreferenceUtil.getString("name", ""));
        params.setCharset("UTF-8");
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getBoolean("result")) {
                        //退出成功
                        mSharedPreferenceUtil.setValue("isLogin", false);
                        mSharedPreferenceUtil.removeKey("name");
                        mSharedPreferenceUtil.removeKey("roomNumber");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 跳入登录界面
     */
    private void goLoginPage() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
