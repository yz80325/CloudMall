package com.yzh.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.common.utils.PageUtils;
import com.yzh.mall.member.entity.MemberEntity;
import com.yzh.mall.member.exception.PhoneExistException;
import com.yzh.mall.member.exception.UserNameExistException;
import com.yzh.mall.member.vo.MemberLoginVo;
import com.yzh.mall.member.vo.MemberRegistVo;
import com.yzh.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:46:55
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo memberRegistVo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String username) throws UserNameExistException;

    MemberEntity login(MemberLoginVo memberLoginVo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

