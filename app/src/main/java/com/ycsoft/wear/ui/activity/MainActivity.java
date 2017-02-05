package com.ycsoft.wear.ui.activity;

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
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.ui.BaseActivity;
import com.ycsoft.wear.ui.dialog.ResponseServiceDialog;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

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
        mSharedPreferenceUtil = new SharedPreferenceUtil(this, SpfConstants.SPF_NAME);
        initReceiver();
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
                        //显示呼叫服务对话框
                        showServiceDialog(mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, ""));
                        break;
                    case Constants.BC_SHOW_CANCEL_SERVICE_DIALOG:
                        //取消呼叫服务对话框
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        break;
                    case Constants.BC_FINISHED_SERVICE_SUCCEED:
                        //确认完成服务成功
                        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_ROOM_NUMBER);
                        tvInfo.setText("");
                        btnFinishedService.setVisibility(View.GONE);
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BC_SHOW_CALL_SERVICE_DIALOG);
        intentFilter.addAction(Constants.BC_SHOW_CANCEL_SERVICE_DIALOG);
        intentFilter.addAction(Constants.BC_FINISHED_SERVICE_SUCCEED);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void initView() {
        if (mSharedPreferenceUtil.getBoolean(SpfConstants.KEY_IS_LOGIN, false)) {
            tvStaffName.setText(mSharedPreferenceUtil.getString(SpfConstants.KEY_NAME, ""));
            if (!mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
                btnFinishedService.setVisibility(View.VISIBLE);
            }
        } else {
            goLoginPage();
            finish();
        }
        if (!mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
            if (mSharedPreferenceUtil.getBoolean(SpfConstants.KEY_NEED_VIBRATE, false)) {
                //震动，并显示对话框！
                showServiceDialog(mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, ""));
            } else {
                tvInfo.setText("请尽快到客户房间\n" + mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, ""));
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
                //完成服务
                if (WebSocketService.getWebSocketClient() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("action", SocketConstants.ACTION_FINISHED_SERVICE);
                        jsonObject.put(SpfConstants.KEY_ID, mSharedPreferenceUtil
                                .getString(SpfConstants.KEY_ID, ""));
                        jsonObject.put(SpfConstants.KEY_NAME, mSharedPreferenceUtil
                                .getString(SpfConstants.KEY_NAME, ""));
                        jsonObject.put(SpfConstants.KEY_ROOM_NUMBER, mSharedPreferenceUtil
                                .getString(SpfConstants.KEY_ROOM_NUMBER, ""));
                        WebSocketService.getWebSocketClient().send(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_logout:
                //退出登录
                if (mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "").equals("")) {
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
        ToastUtil.showToast(this, roomNumber + " 客户正在呼叫服务！", true);
        dialog = new ResponseServiceDialog(this, roomNumber);
        dialog.show(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //更新界面显示
                String roomNumber = mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, "");
                if (roomNumber.equals("")) {
                    tvInfo.setText("");
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
                                mSharedPreferenceUtil.getString(SpfConstants.KEY_NAME, "") + "\"" + "已退出！",
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

    /**
     * 退出登录
     */
    private static final int LOGOUT = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGOUT:
                    if (WebSocketService.getWebSocketClient() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("action", SocketConstants.ACTION_LOGOUT);
                            jsonObject.put(SpfConstants.KEY_ID, mSharedPreferenceUtil
                                    .getString(SpfConstants.KEY_ID, ""));
                            jsonObject.put(SpfConstants.KEY_NAME, mSharedPreferenceUtil
                                    .getString(SpfConstants.KEY_NAME, ""));
                            WebSocketService.getWebSocketClient().send(jsonObject.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

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
