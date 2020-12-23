package com.yzh.mall.auth.feign;

import com.yzh.common.utils.R;
import com.yzh.mall.auth.vo.SocialUser;
import com.yzh.mall.auth.vo.UserLoginVo;
import com.yzh.mall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo memberRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo memberLoginVo);

    @PostMapping("/member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUser socialUser);
}
