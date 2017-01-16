package com.ycsoft.wear.ui.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.ui.BaseDialog;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zhangjun on 2017/1/7.
 * 应答机顶盒的呼叫对话框
 */

public class ResponseServiceDialog extends BaseDialog {
    private static final String TAG = "ResponseServiceDialog";
    @BindView(R.id.tv_message)
    TextView tvMessage;
    private SharedPreferenceUtil mSharedPreferenceUtil;
    private String roomNumber;
    private Vibrator mVibrator;

    public ResponseServiceDialog(Context context, String roomNumber) {
        super(context, R.layout.dialog_response_service, null);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.roomNumber = roomNumber;
        mSharedPreferenceUtil = new SharedPreferenceUtil(context, Constants.SPF_NAME);
    }

    @Override
    protected void initDatas() {
    }

    @Override
    protected void initViews() {
        tvMessage.setText(roomNumber);
    }

    @OnClick({R.id.btn_negative, R.id.btn_positive})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_negative:
                stopVibrator();
                clearRoomInfo();
                dismiss();
                break;
            case R.id.btn_positive:
                stopVibrator();
                ToastUtil.showToast(getContext(), "等待机服务器应答", true);
                RequestParams params = new RequestParams(Constants.SERVER_IP + Constants.API_ACCEPT_SERVICE);
                params.setCharset("UTF-8");
                params.addBodyParameter("id", mSharedPreferenceUtil.getString("id", ""));
                params.addBodyParameter("name", mSharedPreferenceUtil.getString("name", ""));
                params.addBodyParameter("roomNumber", mSharedPreferenceUtil.getString("roomNumber", ""));
                x.http().get(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            mHandler.obtainMessage(1, jsonObject.getBoolean("result")).sendToTarget();
                            dismiss();
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
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(TAG, "RECEIVED result = " + msg.obj);
                    if (!(boolean) msg.obj) {
                        mSharedPreferenceUtil.removeKey("roomNumber");
                        ToastUtil.showToast(getContext(), "已经有其他服务员先确定了服务！", true);
                    }
                    mSharedPreferenceUtil.removeKey("needVibrate");
                    dismiss();
                    break;
            }
        }
    };

    @Override
    public void show(int width, int height) {
        super.show(width, height);
        //显示对话框，立即震动
        startVibrate();
    }

    /**
     * 开始震动
     */
    private void startVibrate() {
        if (mVibrator.hasVibrator()) {
            long[] mPattern = new long[]{
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500,
                    500, 500, 500, 500};//震动20s时间
            mVibrator.vibrate(mPattern, -1);
        }
    }

    /**
     * 停止震动
     */
    private void stopVibrator() {
        if (mVibrator != null)
            mVibrator.cancel();
    }

    @Override
    public void dismiss() {
        stopVibrator();
        super.dismiss();
    }

    @Override
    public void onBackPressed() {
        //按返回键则相当于点击了取消
        stopVibrator();
        clearRoomInfo();
        dismiss();
    }

    /**
     * 清除存入配置文件中的房间呼叫相关信息
     */
    private void clearRoomInfo() {
        //1.清除房间号
        mSharedPreferenceUtil.removeKey("roomNumber");
        //2.清除震动提醒
        mSharedPreferenceUtil.removeKey("needVibrate");
    }
}
