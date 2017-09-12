package com.ycsoft.wear.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.ycsoft.wear.R;
import com.ycsoft.wear.common.Constants;
import com.ycsoft.wear.common.SocketConstants;
import com.ycsoft.wear.common.SpfConstants;
import com.ycsoft.wear.service.WebSocketService;
import com.ycsoft.wear.ui.BaseDialog;
import com.ycsoft.wear.util.SharedPreferenceUtil;
import com.ycsoft.wear.util.ToastUtil;
import com.ycsoft.wear.util.ToolUtil;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

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

    public ResponseServiceDialog(Context context, String roomNumber) {
        super(context, R.layout.dialog_response_service, null);
        this.roomNumber = roomNumber;
        mSharedPreferenceUtil = new SharedPreferenceUtil(context, SpfConstants.SPF_NAME);
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
                cancelPressed();
                break;
            case R.id.btn_positive:
                ToolUtil.stopVibrator(getContext());
                ToastUtil.showToast(getContext(), "等待机服务器应答", true);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action", SocketConstants.ACTION_ACCEPT_SERVICE);
                    jsonObject.put(SpfConstants.KEY_ID, mSharedPreferenceUtil.getString(SpfConstants.KEY_ID, ""));
                    jsonObject.put(SpfConstants.KEY_NAME, mSharedPreferenceUtil.getString(SpfConstants.KEY_NAME, ""));
                    jsonObject.put(SpfConstants.KEY_ROOM_NUMBER, mSharedPreferenceUtil.getString(SpfConstants.KEY_ROOM_NUMBER, ""));
                    jsonObject.put(SpfConstants.KEY_AREA_NAME, mSharedPreferenceUtil.getString(SpfConstants.KEY_AREA_NAME, ""));
                    WebSocketClient mWebSocketClient = WebSocketService.getWebSocketClient();
                    if (Constants.isConnectedServer) {
                        if (mWebSocketClient != null)
                            mWebSocketClient.send(jsonObject.toString());
                    } else {
                        ToastUtil.showToast(getContext(), "已经与服务器断开连接！", true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void show(int width, int height) {
        super.show(width, height);
        //显示对话框，立即震动
        ToolUtil.startVibrate(getContext(), 30);
    }

    @Override
    public void onBackPressed() {
        //按返回键则相当于点击了取消
        cancelPressed();
    }

    /**
     * 点击了取消按钮
     */
    private void cancelPressed() {
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_NEED_VIBRATE);
        ToolUtil.stopVibrator(getContext());
        clearRoomInfo();
        dismiss();
    }

    /**
     * 清除存入配置文件中的房间呼叫相关信息
     */
    private void clearRoomInfo() {
        mSharedPreferenceUtil.removeKey(SpfConstants.KEY_ROOM_NUMBER);
    }
}
