package com.shzlw.poli.rest;

import com.shzlw.poli.service.ZhizhiUserSSOService;
import com.zhizhi.common.utils.AESUtil;
import com.zhizhi.common.utils.Sha256Utils;
import com.zhizhi.uc.sdk.result.SSOReulstExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ZhizhiUcTest extends AbstractWsTest {

    @Autowired
    private ZhizhiUserSSOService zhizhiUserSSOService;

    @Test
    public void getUserInfo() throws Exception {
        //获取用户信息

//        zhizhiUserSSOService.getUserInfo();
        SSOReulstExt ssoReulstExt = zhizhiUserSSOService.getUserByAccount("123","15051818672");
        System.out.println(ssoReulstExt);
    }

    @Test
    public void authUser() throws Exception {
        //用户信息鉴权
        SSOReulstExt ssoReulstExt = zhizhiUserSSOService.login("123","15051818672",Sha256Utils.getSHA256("123456"),"123");
        System.out.println(ssoReulstExt);

    }


}
