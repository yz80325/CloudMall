package com.yzh.mall.member.dao;

import com.yzh.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yzh
 * @email sunlightcs@gmail.com
 * @date 2020-10-11 13:46:55
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
