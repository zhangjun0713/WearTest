package com.ycsoft.wear.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ycsoft.wear.common.YCApplication;

/**
 * Created by zhangjun on 2016/10/17.
 * 基Activity，所有的Activity继承该Activity以方便统一管理
 */

public abstract class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((YCApplication) getApplication()).addActivity(this);
		initActivity();
		initView();
		initData();
	}

	/**
	 * 初始化Activity，在此设置布局文件
	 */
	protected abstract void initActivity();

	/**
	 * 初始化控件
	 */
	protected abstract void initView();

	/**
	 * 初始化显示数据
	 */
	protected abstract void initData();

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((YCApplication) getApplication()).removeActivity(this);
	}
}
