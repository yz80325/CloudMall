package com.yzh.mall.auth.vo;

import lombok.Data;

/**
 * 登录信息
 */
@Data
public class UserLoginVo {
    private String loginacct;
    private String password;
}
