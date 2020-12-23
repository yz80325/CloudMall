package com.yzh.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yzh.common.constant.AuthServerConstant;
import com.yzh.common.utils.R;
import com.yzh.mall.auth.feign.MemberFeignService;
import com.yzh.common.vo.MemberVo;
import com.yzh.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 处理社交登录
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;


    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws IOException {

        //1.根据code换AccessToken
        //client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        String urlSend = "https://api.weibo.com/oauth2/access_token";
        HttpClient httpClient=new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(urlSend);
        List<NameValuePair>urlParameters=new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("client_id","3429568593"));
        urlParameters.add(new BasicNameValuePair("client_secret","76459ebe52df2436496ba1933a376689"));
        urlParameters.add(new BasicNameValuePair("grant_type","authorization_code"));
        urlParameters.add(new BasicNameValuePair("redirect_uri","http://auth.yzhmall.com/oauth2.0/weibo/success"));
        urlParameters.add(new BasicNameValuePair("code",code));
        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode()==200){
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //获取到了
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if (oauthlogin.getCode()==0){

                ObjectMapper mapper = new ObjectMapper();
                LinkedHashMap<String, String> vo= (LinkedHashMap<String,String>)oauthlogin.get("member");
                String s = mapper.writeValueAsString(vo);
                MemberVo memberVo = JSON.parseObject(s, MemberVo.class);
                log.info("用户登录成功"+memberVo.toString());
                session.setAttribute(AuthServerConstant.LOGIN_USER,memberVo);
                //成功
                return "redirect:http://yzhmall.com";
            }else {
                //失败
                return "redirect:http://auth.yzhmall.com/login.html ";
            }
        }else {
            //失败
            return "redirect:http://auth.yzhmall.com/login.html ";
        }
    }
}
