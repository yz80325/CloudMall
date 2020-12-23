package com.yzh.mallcart.interceptor;

import com.yzh.common.constant.AuthServerConstant;
import com.yzh.common.constant.CartConstant;
import com.yzh.common.vo.MemberVo;
import com.yzh.mallcart.to.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法前，判断用户登录状态，并封装给controller
 */

public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal=new ThreadLocal<UserInfoTo>();

    /**
     * 在目标方法执行前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberVo!=null){
            //用户登了
            userInfoTo.setUserId(memberVo.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //如果没有临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String s = UUID.randomUUID().toString();
            userInfoTo.setUserKey(s);
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行以后,分配临时用户
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("yzhmall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
