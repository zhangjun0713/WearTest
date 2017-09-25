package com.ycsoft.wear.ui.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.ycsoft.wear.R;
import com.ycsoft.wear.ui.BaseDialog;

import butterknife.BindView;

/**
 * Created by zhang on 2017-09-22.
 * 过程提醒对话框
 */

public class ProgressRemindDialog extends BaseDialog {
    @BindView(R.id.tv_remind)
    TextView tvRemind;
    private String remindText;
    /**
     * 过程阶段
     */
    private int progressIndex = 1;

    public ProgressRemindDialog(Context context, String remindText) {
        super(context, R.layout.dialog_login, null);
        this.remindText = remindText;
    }

    @Override
    protected void initDatas() {

    }

    @Override
    protected void initViews() {
        tvRemind.setText(remindText);
        mHandler.sendEmptyMessageDelayed(UPDATE_REMIND_TEXT, 500);
    }

    private static final int UPDATE_REMIND_TEXT = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_REMIND_TEXT:
                    if (progressIndex == 0) {
                        tvRemind.setText(remindText);
                    } else if (progressIndex == 1) {
                        tvRemind.setText(remindText + ".");
                    } else if (progressIndex == 2) {
                        tvRemind.setText(remindText + "..");
                    } else if (progressIndex == 3) {
                        tvRemind.setText(remindText + "...");
                    }
                    if (progressIndex < 3) {
                        progressIndex++;
                    } else {
                        progressIndex = 0;
                    }
                    mHandler.sendEmptyMessageDelayed(UPDATE_REMIND_TEXT, 500);
                    break;
            }
        }
    };
}
