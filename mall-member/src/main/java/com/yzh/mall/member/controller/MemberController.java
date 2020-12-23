package com.yzh.mall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.yzh.common.exception.BizCodeEnum;
import com.yzh.mall.member.exception.PhoneExistException;
import com.yzh.mall.member.exception.UserNameExistException;
import com.yzh.mall.member.vo.MemberLoginVo;
import com.yzh.mall.member.vo.MemberRegistVo;
import com.yzh.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yzh.mall.member.entity.MemberEntity;
import com.yzh.mall.member.service.MemberService;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.R;



/**
 * 会员
 *
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:46:55
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo){
        try {
            memberService.regist(memberRegistVo);
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        }catch (UserNameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }

    /**
     * 社交登录
     * @param socialUser
     * @return
     */
    @PostMapping("/oauth2/login")
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity memberEntity=memberService.login(socialUser);
        if (memberEntity!=null){
            return R.ok().put("member",memberEntity);
        }else {
            return R.error(BizCodeEnum.USERNAME_PASSWORD_EXCEPTION.getCode(),BizCodeEnum.USERNAME_PASSWORD_EXCEPTION.getMessage());
        }

    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo memberLoginVo){
        MemberEntity memberEntity=memberService.login(memberLoginVo);
        if (memberEntity!=null){
            return R.ok().put("member",memberEntity);
        }else {
            return R.error(BizCodeEnum.USERNAME_PASSWORD_EXCEPTION.getCode(),BizCodeEnum.USERNAME_PASSWORD_EXCEPTION.getMessage());
        }

    }
    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
   // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
 //   @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
 //   @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
 //   @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
