package com.ycsoft.wear.model;

import java.io.Serializable;

/**
 * Created by zhang on 2017/9/12.
 * 服务员信息实体
 */

public class WaiterInfoEntity implements Serializable {
    /**
     * 服务员ID号
     */
    public int id;
    /**
     * 服务员名字
     */
    public String name;
    /**
     * 区域信息对象
     */
    public AreaInfoEntity areaInfo;

    @Override
    public String toString() {
        return "WaiterInfoEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", areaInfo=" + areaInfo +
                '}';
    }
}
