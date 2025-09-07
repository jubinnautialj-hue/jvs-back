package cn.bctools.design.workflow.service.impl;

import cn.bctools.design.project.entity.dto.AppTaskDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.workflow.dto.FlowQwNoticeDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNoticeLog;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskNoticeLogService;
import cn.bctools.design.workflow.service.FlowTaskNoticeService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
企微消息
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowQwNoticeServiceImpl implements FlowTaskNoticeService {
    JvsAppService jvsAppService;
    FlowTaskNoticeLogService flowTaskNoticeLogService;
    static AppTaskDto appTaskDto;
    /**
     * 初始化应用的待办提醒配置信息
     * @param appId 应用ID
     * @retur
     */
    private void initTaskSettings(String appId){
        appTaskDto = jvsAppService.getAppById(appId).getTaskSetting();
    }
    @Override
    public Boolean create(Node nextNode, FlowTask flowTask, List<FlowTaskPerson> flowTaskPersons) throws Exception {
       initTaskSettings(flowTask.getJvsAppId());
        if (appTaskDto.getEnableTask()){
            List<FlowQwNoticeDto> flowTaskList = new ArrayList<>();
            flowTaskPersons.forEach(flowTaskPerson -> {
                FlowQwNoticeDto flowQwNoticeDto = new FlowQwNoticeDto();
                flowQwNoticeDto.setWorkNum("");
                flowQwNoticeDto.setBizInstanceId(flowTask.getId());
                flowQwNoticeDto.setBizTaskId("");
                flowQwNoticeDto.setBizNodeId(nextNode.getId());
                flowQwNoticeDto.setTitle(flowTask.getTitle());
                flowQwNoticeDto.setTaskType(0);
                flowQwNoticeDto.setHandler("");
                flowQwNoticeDto.setHandlerName(flowTaskPerson.getUserName());
                flowQwNoticeDto.setApplicantName("");
                flowQwNoticeDto.setFormUrl("");
                flowQwNoticeDto.setPriority(0);
                flowQwNoticeDto.setCreateDate(System. currentTimeMillis());
                flowTaskList.add(flowQwNoticeDto);
            });
            sendRequest(appTaskDto.getTaskPushApi(), flowTaskList, nextNode, flowTask, "create");
        }
        return false;
    }
    public Boolean create(Map flowParam) throws Exception {
        //String createUrl = qwNoticeConfig.getCreate();
//        String appId = qwNoticeConfig.getAppId();
//        String appSecret = qwNoticeConfig.getAppSecret();
//        String appId = "1827966645317992450";
//        String appSecret = "c2352cae-b1f2-4475-8602-407b42ba2f60";
//        String apiPath = "/iuap-ipaas-runtime/cwgx2mh/tasks/push"; //待办具体接口
//        String serverUrl="https://busi.powerbeijing.com"; //待办接口地址

        String appId = "1935266555846426625";
        String appSecret = "fb77eb2a-a266-4c0f-ac50-790e758bdd27";
        String apiPath = "/iuap-ipaas-runtime/cwgx2mh/tasks/push"; //待办具体接口
        String serverUrl="https://esbtest.powerbeijing.com"; //待办接口地址

        long now = new Date().getTime();
        String timestamp = Long.toString((long) Math.floor(now/1000));         //当前时间戳，单位秒
        String nonce = Long.toHexString(now) + "-" + Long.toHexString((long) Math.floor(Math.random() * 0xFFFFFF)); //随机字符串
        String sign = getMD5(SHA256(appSecret + nonce + timestamp));

        // 请求接口的示例参数
        FlowQwNoticeDto a = new FlowQwNoticeDto();
        a.setWorkNum("B288822070000912");
        a.setBizInstanceId("15530384446286192906");
        a.setBizNodeId("15530384446286192905");
        a.setBizTaskId("15530384446286192907");
        a.setTitle("请假申请7777");
        a.setTaskType(0);
        a.setHandler("WANGYQ4716");
        a.setHandlerName("王月强");
        a.setApplicantName("测试");
        a.setFormUrl("https://www.baidu.com");
        a.setPriority(0);
        a.setCreateDate(new Date().getTime());
        List<FlowQwNoticeDto> bb=new ArrayList<>();
        bb.add(a);
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
        response.close();
        client.close();
        return true;
        //return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    @Override
    public Boolean close(List<String> ids) throws Exception{

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

        //return EntityUtils.toString(response.getEntity(), "UTF-8");
        return true;

    }

    @Override
    public Boolean recall(List<String> bizTaskAndTaskIds) throws Exception {
        return true;
    }

    @Override
    public Boolean update(Node nextNode, FlowTask flowTask, List<FlowTaskPerson> flowTaskPersons) throws Exception {
        return true;
    }

    private boolean sendRequest(String apiUrl, List<FlowQwNoticeDto> requestData, Node nextNode, FlowTask task, String type) {
        String jsonRequest = JSONArray.toJSONString(requestData);
        boolean isSucceed = false;
        FlowTaskNoticeLog flowTaskNoticeLog = new FlowTaskNoticeLog();
        flowTaskNoticeLog.setApiUrl(apiUrl);
        flowTaskNoticeLog.setTaskId(task.getId());
        flowTaskNoticeLog.setType(type);
        flowTaskNoticeLog.setNodeId(nextNode.getId());
        flowTaskNoticeLog.setDataId(task.getDataId());
        flowTaskNoticeLog.setJvsAppId(task.getJvsAppId());
        List<Map<String,Object>> requestDataList = new ArrayList<>();
        BeanUtils.copyProperties(requestData, requestDataList);
        flowTaskNoticeLog.setRequestData(requestDataList);
        try {
            String response = sendRequest(apiUrl, jsonRequest, appTaskDto.getTaskAppId(), appTaskDto.getTaskAppSecret());
            log.info("打印统一待办response:{}", response);
            if (response != null) {
                Map apiResponse = JSON.parseObject(response, Map.class);
                flowTaskNoticeLog.setResponseData(apiResponse);
                JSONObject data = (JSONObject) apiResponse.get("data");
                // 执行成功
                if ("0".equals(apiResponse.get("code"))) {
                    isSucceed = true;
                }
            }
        } catch (Exception e) {
        }
        flowTaskNoticeLogService.save(flowTaskNoticeLog);
        return isSucceed;
    }

    /**
     * 公共方法，发送请求
     * @param serverUrl 请求接口地址
     * @param params 请求参数
     * @param appSecret 应用密钥
     * @param appId 应用ID
     * @return
     * @throws IOException
     */
    private String sendRequest(String serverUrl, String params, String appId, String appSecret) throws IOException {
        long now = System. currentTimeMillis() ;
        String timestamp = Long.toString((long) Math.floor(now / 1000));
        //当前时间戳，单位秒
        String nonce = Long.toHexString(now) + "-" +
                Long.toHexString((long) Math.floor(Math.random() * 0xFFFFFF));
        String sign = getMD5(SHA256(appSecret + nonce + timestamp));
        //创建CloseableHttpClient
        CloseableHttpResponse response;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(serverUrl);
            httpPost.setHeader("x-gw-signature", sign);
            httpPost.setHeader("x-gw-timestamp", timestamp);
            httpPost.setHeader("x-gw-nonce", nonce);
            httpPost.setHeader("x-gw-appid", appId);
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setHeader("Cache-Control", "no-cache");
            HttpEntity entity = new StringEntity(params, "UTF-8");
            Header[] allHeaders = httpPost.getAllHeaders();
            httpPost.setEntity(entity);
            response = client.execute(httpPost);
        }
        return EntityUtils.toString(response.getEntity());
    }
    public static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }
    private static String SHA(final String text, final String type) {
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
