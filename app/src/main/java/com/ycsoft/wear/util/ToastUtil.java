package com.ycsoft.wear.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jeremy on 2016/10/15.
 * Toast提示工具
 */

public class ToastUtil {

	private static Toast toast;

	public static void showToast(Context context,
	                             String content, boolean isShort) {
		if (toast == null) {
			if (isShort) {
				toast = Toast.makeText(context,
						content,
						Toast.LENGTH_SHORT);
			} else {
				toast = Toast.makeText(context,
						content,
						Toast.LENGTH_LONG);
			}
		} else {
			toast.setText(content);
			if (isShort && toast.getDuration() != Toast.LENGTH_SHORT) {
				toast.setDuration(Toast.LENGTH_SHORT);
			} else if (!isShort && toast.getDuration() == Toast.LENGTH_SHORT) {
				toast.setDuration(Toast.LENGTH_LONG);
			}

		}
		toast.show();
	}

}
