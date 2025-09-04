package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.config.QwNoticeConfig;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.workflow.dto.ApprovalRecordFieldDto;
import cn.bctools.design.workflow.dto.FlowDesignNodeDto;
import cn.bctools.design.workflow.dto.FlowQwNoticeDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.ApproveResultDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.*;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.web.utils.HttpRequestUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
企微消息
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowQwNoticeServiceImpl implements FlowQwNoticeService {
    QwNoticeConfig qwNoticeConfig;

    @Override
    public String create(Map flowParam) throws Exception {

        String createUrl = qwNoticeConfig.getCreate();
//        String appId = qwNoticeConfig.getAppId();
//        String appSecret = qwNoticeConfig.getAppSecret();
        String appId = "1827966645317992450";
        String appSecret = "c2352cae-b1f2-4475-8602-407b42ba2f60";
        log.info("createUrl:{}",createUrl);

        String apiPath = "/iuap-ipaas-runtime/cwgx2mh/task/close"; //待办具体接口
        String serverUrl="https://busi.powerbeijing.com"; //待办接口地址

        long now = new Date().getTime();
        String timestamp = Long.toString((long) Math.floor(now/1000));         //当前时间戳，单位秒
        String nonce = Long.toHexString(now) + "-" + Long.toHexString((long) Math.floor(Math.random() * 0xFFFFFF)); //随机字符串
        String sign = getMD5(SHA256(appSecret + nonce + timestamp));

        // 请求接口的示例参数
        FlowQwNoticeDto a = new FlowQwNoticeDto();
        a.setWorkNum("B2888220700116");
        a.setBizInstanceId("1553038444628619266");
        a.setBizNodeId("1553038444628619267");
        a.setBizTaskId("1553038444628619298");
        a.setTitle("请假申请");
        a.setTaskType(0);
        a.setHandler("SUTY2859");
        a.setHandlerName("苏腾跃");
        a.setApplicantName("测试");
        a.setFormUrl("https://www.baidu.com");
        a.setPriority(0);
        a.setCreateDate(new Date().getTime());
        List<String> bb=new ArrayList<>();
        bb.add("a");
        String params = JSONArray.toJSONString(bb);
        SSLContext sslContext = new SSLContextBuilder()
                // 信任所有证书（临时方案核心）
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                // 关闭主机名验证（避免证书域名不匹配报错）
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpPost httpPost = new HttpPost(serverUrl+apiPath);
        httpPost.setHeader("x-gw-signature", sign);
        httpPost.setHeader("x-gw-timestamp", timestamp);
        httpPost.setHeader("x-gw-nonce", nonce);
        httpPost.setHeader("x-gw-appid", appId);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Cache-Control", "no-cache");
        HttpEntity entity=new StringEntity(params,"UTF-8");
        httpPost.setEntity(entity);
        CloseableHttpResponse response=client.execute(httpPost);
//        System.out.println(EntityUtils.toString(response.getEntity(), "UTF-8"));
        response.close();
        client.close();
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    @Override
    public String close(List<String> ids) throws Exception{

        String appId = "1827966645317992450";
        String appSecret = "c2352cae-b1f2-4475-8602-407b42ba2f60";
//        String appId = "1937072455152041986";
//        String appSecret = "7e62d5bd-2807-46a4-ba5d-4546ef5d1ef8";
        long now = new Date().getTime();
        String timestamp = Long.toString((long) Math.floor(now/1000));         //当前时间戳，单位秒
        String nonce = Long.toHexString(now) + "-" + Long.toHexString((long) Math.floor(Math.random() * 0xFFFFFF)); //随机字符串
        String sign = getMD5(SHA256(appSecret + nonce + timestamp));

        String apiPath = "/iuap-ipaas-runtime/cwgx2mh/task/close"; //待办具体接口
        String serverUrl="https://busi.powerbeijing.com"; //待办接口地址

        String params = JSONArray.toJSONString(ids);

        //创建CloseableHttpClient
//        CloseableHttpClient client =  HttpClientBuilder.create().build();
        // 2. 核心修改：创建绕过SSL验证的HttpClient
        SSLContext sslContext = new SSLContextBuilder()
                // 信任所有证书（临时方案核心）
                .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                // 关闭主机名验证（避免证书域名不匹配报错）
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpPost httpPost = new HttpPost(serverUrl+apiPath);
        httpPost.setHeader("x-gw-signature", sign);
        httpPost.setHeader("x-gw-timestamp", timestamp);
        httpPost.setHeader("x-gw-nonce", nonce);
        httpPost.setHeader("x-gw-appid", appId);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Cache-Control", "no-cache");
        HttpEntity entity=new StringEntity(params,"UTF-8");
        httpPost.setEntity(entity);
        CloseableHttpResponse response=client.execute(httpPost);
        // 4. 资源关闭（避免泄漏）
        response.close();
        client.close();

        return EntityUtils.toString(response.getEntity(), "UTF-8");

    }

    public static String SHA256(final String strText)
    {
        return SHA(strText, "SHA-256");
    }

    private static String SHA(final String text, final String type)
    {
        // 返回值
        String strResult = null;
        // 是否是有效字符串
        if (text != null && text.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密类型
                MessageDigest messageDigest = MessageDigest.getInstance(type);
                // 传入要加密的字符串
                messageDigest.update(text.getBytes());
                // 得到 byte 类型结果
                byte byteBuffer[] = messageDigest.digest();
                // 将 byte 转换为 string
                StringBuffer strHexString = new StringBuffer();
                // 遍历 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回结果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }

    public static String getMD5(String text) {
        if (text == null || text.trim().length() == 0) {
            return null;
        }
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    text.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        // 将 byte 转换为 string
        StringBuffer strHexString = new StringBuffer();
        // 遍历 byte buffer
        for (int i = 0; i < secretBytes.length; i++) {
            String hex = Integer.toHexString(0xff & secretBytes[i]);
            if (hex.length() == 1) {
                strHexString.append('0');
            }
            strHexString.append(hex);
        }
        return strHexString.toString();
    }
}
