package com.ycsoft.weartest.ui.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ycsoft.weartest.R;
import com.ycsoft.weartest.common.Constants;
import com.ycsoft.weartest.socket.SendToBoxSocketClient;
import com.ycsoft.weartest.ui.BaseDialog;
import com.ycsoft.weartest.util.SharedPreferenceUtil;
import com.ycsoft.weartest.util.ToastUtil;

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
                mSharedPreferenceUtil.removeKey("roomNumber");
                dismiss();
                break;
            case R.id.btn_positive:
                stopVibrator();
                ToastUtil.showToast(getContext(), "等待机顶盒应答", true);
                new SendToBoxSocketClient(mSharedPreferenceUtil.getString("name", ""), mHandler);
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(TAG, "RECEIVED what = " + 1);
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
}
