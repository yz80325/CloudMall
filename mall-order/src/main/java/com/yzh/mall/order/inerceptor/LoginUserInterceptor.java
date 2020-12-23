package com.yzh.mall.order.inerceptor;

import com.yzh.common.constant.AuthServerConstant;
import com.yzh.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberVo>login=new ThreadLocal<>();
    //前置拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //远程调用的时候如果是/order/order/status/直接放行
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if (match){
            return true;
        }

        MemberVo attribute = (MemberVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute!=null){
            login.set(attribute);
            //登录
            return true;
        }else {
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.yzhmall.com/login.html");
            return false;
        }

    }
}
