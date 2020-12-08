package com.yzh.mall.thirdparty;


import com.aliyun.oss.OSSClient;
import com.yzh.mall.thirdparty.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallThirdPartyApplicationTests {
	@Autowired
	OSSClient ossClient;

	@Autowired
	SmsComponent smsComponent;
	@Test
	public void testUpload() throws FileNotFoundException {
/*		// Endpoint以杭州为例，其它Region请按实际情况填写。
		String endpoint = "oss-ap-northeast-1.aliyuncs.com";
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维

// 创建OSSClient实例。
		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);*/

// 上传文件流。
		InputStream inputStream = new FileInputStream("E:\\desktop\\学习\\Linux学习\\快捷键截图\\cp.PNG");
		ossClient.putObject("yzh-202113", "cp.PNG", inputStream);

// 关闭OSSClient。
		//ossClient.shutdown();
		System.out.println("上传成功");
	}

	@Test
	public void sendSms(){
		smsComponent.sendSmsCode("15738841897","11111");
	}


}
