package com.yzh.mall.order.feign;

import com.yzh.mall.order.vo.MemeberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("mall-member")
public interface MemberFeign {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    List<MemeberAddressVo> findAddress(@PathVariable("memberId") Long memberId);

}
