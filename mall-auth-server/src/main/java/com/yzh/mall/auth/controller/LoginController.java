package com.yzh.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yzh.common.constant.AuthServerConstant;
import com.yzh.common.exception.BizCodeEnum;
import com.yzh.common.utils.R;
import com.yzh.common.vo.MemberVo;
import com.yzh.mall.auth.feign.MemberFeignService;
import com.yzh.mall.auth.feign.SmsFeign;
import com.yzh.mall.auth.vo.UserLoginVo;
import com.yzh.mall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    @Autowired
    SmsFeign smsFeign;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;
    @ResponseBody
    @GetMapping("/sms/getCode")
    public R SendCode(@RequestParam("phone") String phone){
        //验证是否是60秒内
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(!StringUtils.isEmpty(redisCode)){
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis()-l<60000){
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        //接口防刷
        //验证码再次校验

        String substring = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();
        //redis
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,substring,10, TimeUnit.MINUTES);
        //防止同一手机60s内再次发送
        smsFeign.sendCode(phone,substring.split("_")[0]);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult bindingResult, RedirectAttributes attributes){
        if (bindingResult.hasErrors()){
            Map<String,String>errors=new HashMap<>();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            attributes.addFlashAttribute("errors",errors);
            //校验出错
            return "redirect:http://auth.yzhmall.com/reg.html";
        }
        //真正注册
        String code = userRegistVo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        if (!StringUtils.isEmpty(s)){
            String s1 = s.split("_")[0];
            if (code.equals(s1)){
                //删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                //验证码通过
                R regist = memberFeignService.regist(userRegistVo);
                if (regist.getCode()==0){
                    //成功
                    return "redirect:http://auth.yzhmall.com/login.html";
                }else {
                    Map<String,String>errors=new HashMap<>();
                    errors.put("msg",(String) regist.get("msg"));
                    attributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.yzhmall.com/reg.html";
                }

            }else {
                Map<String,String>errors=new HashMap<>();
                errors.put("code","验证码错误");
                attributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.yzhmall.com/reg.html";
            }
        }else {
            Map<String,String>errors=new HashMap<>();
            errors.put("code","验证码已过期");
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.yzhmall.com/reg.html";
        }
    }
    @GetMapping("/login.html")
    public String loginPage(HttpSession httpSession){
        Object attribute = httpSession.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute==null){
            return "index";
        }else {
            return "redirect:http://yzhmall.com";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) throws JsonProcessingException {
        R login = memberFeignService.login(vo);
        if (login.getCode()==0){
            ObjectMapper mapper = new ObjectMapper();
            LinkedHashMap<String, String> aa= (LinkedHashMap<String,String>)login.get("member");
            String s = mapper.writeValueAsString(aa);
            MemberVo memberVo = JSON.parseObject(s, MemberVo.class);
            session.setAttribute(AuthServerConstant.LOGIN_USER,memberVo);
            return "redirect:http://yzhmall.com";
        }else {
            Map<String,String> errors=new HashMap<>();
            errors.put("msg", String.valueOf(login.get("msg")));
            attributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.yzhmall.com/login.html";
        }

    }

}
