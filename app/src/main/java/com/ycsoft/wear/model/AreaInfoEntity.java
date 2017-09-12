package com.ycsoft.wear.model;

import java.io.Serializable;

/**
 * Created by zhang on 2017/9/12.
 * 区域信息实体
 */

public class AreaInfoEntity implements Serializable {
    /**
     * 区域ID
     */
    public int id;
    /**
     * 区域名字
     */
    public String name;

    @Override
    public String toString() {
        return "AreaInfoEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
