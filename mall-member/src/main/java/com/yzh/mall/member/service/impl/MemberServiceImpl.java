package com.yzh.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yzh.mall.member.dao.MemberLevelDao;
import com.yzh.mall.member.entity.MemberLevelEntity;
import com.yzh.mall.member.exception.PhoneExistException;
import com.yzh.mall.member.exception.UserNameExistException;
import com.yzh.mall.member.service.MemberLevelService;
import com.yzh.mall.member.util.HttpUtil;
import com.yzh.mall.member.vo.MemberLoginVo;
import com.yzh.mall.member.vo.MemberRegistVo;
import com.yzh.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.common.utils.PageUtils;
import com.yzh.common.utils.Query;

import com.yzh.mall.member.dao.MemberDao;
import com.yzh.mall.member.entity.MemberEntity;
import com.yzh.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    //用户注册
    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberEntity memberEntity=new MemberEntity();
        MemberLevelEntity LevelEntity=memberLevelDao.getDefaultLevle();
        memberEntity.setLevelId(LevelEntity.getId());

        //检验用户名和密码是否唯一
        checkPhoneUnique(memberRegistVo.getPhone());
        checkUserNameUnique(memberRegistVo.getUserName());
        //设置
        memberEntity.setMobile(memberRegistVo.getPhone());
        memberEntity.setUsername(memberRegistVo.getUserName());
        //密码
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(memberRegistVo.getPassword());
        memberEntity.setPassword(encode);

        //保存
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException{
        Integer mobile = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile>0){
            throw new PhoneExistException();
        }


    }

    @Override
    public void checkUserNameUnique(String username) throws UserNameExistException{
        Integer usercode = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (usercode>0){
            throw new UserNameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        String loginacct = memberLoginVo.getLoginacct();
        String password = memberLoginVo.getPassword();

        //取数据库
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity==null){
            //登录失败
            return null;
        }else {
            //获取到数据库password
            String passwordDB = memberEntity.getPassword();
            boolean matches = new BCryptPasswordEncoder().matches(password, passwordDB);
            if (matches){
                return memberEntity;
            }else {
                return null;
            }
        }

    }

    /**
     * 社交账号登录
     * @param socialUser
     * @return
     */
    @Override
    public MemberEntity login(SocialUser socialUser) {
        String uid = socialUser.getUid();
        //判断当前社交用户是否注册
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity!=null){
            //注册过
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            baseMapper.updateById(update);
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        }else {
            //没有当前社交用户
            MemberEntity regist=new MemberEntity();
            try {
                //https://api.weibo.com/2/users/show.json
                HashMap<String,String>query=new HashMap<>();
                query.put("access_token",socialUser.getAccess_token());
                query.put("uid",socialUser.getUid());
                HttpResponse response = HttpUtil.sendGet("https://api.weibo.com/2/users/show.json", query);
                if (response.getStatusLine().getStatusCode()==200){
                    String s = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(s);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    regist.setUsername(name);
                    regist.setGender("m".equals(gender)?1:0);
                }
            }catch (Exception e){

            }
            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            baseMapper.insert(regist);
            return regist;
        }
    }

}