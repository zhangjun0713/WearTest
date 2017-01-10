package com.ycsoft.weartest.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ycsoft.weartest.R;

import butterknife.ButterKnife;


/**
 * Created by zhangjun on 2016/11/1.
 * 自定义Dialog父类
 */

public abstract class BaseDialog extends Dialog {

    private int layoutRes;
    private View contentView;

    public BaseDialog(Context context, int layoutRes, @Nullable View contentView) {
        this(context, R.style.YCDialogStyle, layoutRes, contentView);
    }

    public BaseDialog(Context context, int themeResId, int layoutRes,
                      @Nullable View contentView) {
        super(context, themeResId);
        this.layoutRes = layoutRes;
        this.contentView = contentView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (contentView == null) {
            setContentView(layoutRes);
        } else {
            setContentView(contentView);
        }
        ButterKnife.bind(this);
        initViews();
        initDatas();
    }

    /**
     * 初始化数据显示
     */
    protected abstract void initDatas();

    /**
     * 初始化控件
     */
    protected abstract void initViews();

    /**
     * 显示时调用此方法
     *
     * @param width  Dialog的宽度
     * @param height Dialog的高度
     */
    public void show(int width, int height) {
        show();
        setShowLayout(width, height);
    }

    /**
     * 显示时调用{@link #show(int, int)}方法，不要调用该方法。
     */
    @Override
    public void show() {
        super.show();
    }

    /**
     * 设置Dialog的显示宽高属性
     *
     * @param width  宽度
     * @param height 高度
     */
    private void setShowLayout(int width, int height) {
        //设置宽度全屏，要设置在show的后面
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = width;
        layoutParams.height = height;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

}
