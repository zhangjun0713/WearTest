package com.ycsoft.wear.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhangjun on 2016/11/7.
 * SharedPreference操作类
 */

public class SharedPreferenceUtil {
    private Context context;
    private SharedPreferences spf;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtil(Context context, String name) {
        this.context = context;
        spf = this.context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 存入一个值
     *
     * @param key   键名
     * @param value 值（可以为Integer,Float,Long,String,Boolean,Set<String>类型的）
     * @throws ClassCastException 当value值为Set<?>时？不为String时会出现类型转换异常
     */
    public void setValue(String key, @NonNull Object value) throws ClassCastException {
        editor = spf.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Set<?>) {
            Set<String> setValue;
            setValue = (Set<String>) value;
            editor.putStringSet(key, setValue);
        }
        editor.apply();
    }

    /**
     * 获取int型值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getInt(String key, int defaultValue) {
        return spf.getInt(key, defaultValue);
    }

    /**
     * 获取String型值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        return spf.getString(key, defaultValue);
    }

    /**
     * 获取float型值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public float getFloat(String key, float defaultValue) {
        return spf.getFloat(key, defaultValue);
    }

    /**
     * 获取long型值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public long getLong(String key, long defaultValue) {
        return spf.getLong(key, defaultValue);
    }

    /**
     * 获取boolean型值
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return spf.getBoolean(key, defaultValue);
    }

    /**
     * 获取所有键值对
     *
     * @return
     */
    public Map<String, ?> getAll() {
        return spf.getAll();
    }

    /**
     * 删除一个键值
     *
     * @param key 键名
     */
    public void removeKey(String key) {
        spf.edit().remove(key).apply();
    }

    /**
     * 当需要一次添修改多个键值对时调用该方法拿到编辑对象，编辑完后记得调用提交commit()或者apply()提交编辑
     *
     * @return
     */
    public SharedPreferences.Editor getEditor() {
        return spf.edit();
    }
}
