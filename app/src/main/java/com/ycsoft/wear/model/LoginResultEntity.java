package com.ycsoft.wear.model;

import java.io.Serializable;

/**
 * Created by zhang on 2017/9/12.
 * 服务员信息实体
 */

public class LoginResultEntity implements Serializable {
    /**
     * 结果
     */
    public boolean result;
    /**
     * 登录成功的验证Token
     */
    public String token;
    /**
     * 服务员信息对象
     */
    public WaiterInfoEntity waiterInfo;

    @Override
    public String toString() {
        return "LoginResultEntity{" +
                "result=" + result +
                ", token='" + token + '\'' +
                ", waiterInfo=" + waiterInfo +
                '}';
    }
}
