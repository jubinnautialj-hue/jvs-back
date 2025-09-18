package cn.bctools.design.taskNotice.service.impl;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.project.entity.dto.AppTaskDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.taskNotice.dto.FlowNoticeRequestDto;
import cn.bctools.design.taskNotice.dto.FlowNoticeResponseDto;
import cn.bctools.design.taskNotice.entity.FlowTaskNotice;
import cn.bctools.design.taskNotice.entity.FlowTaskNoticeLog;
import cn.bctools.design.taskNotice.enums.FlowTaskNoticeEnum;
import cn.bctools.design.taskNotice.mapper.FlowTaskNoticeMapper;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeLogService;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeService;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.model.Node;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

/**
   待办提醒服务实现类
 * @author jerry_su
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowTaskNoticeServiceImpl extends ServiceImpl<FlowTaskNoticeMapper, FlowTaskNotice> implements FlowTaskNoticeService {
    private final JvsAppService jvsAppService;
    private final FlowTaskNoticeLogService flowTaskNoticeLogService;
    private static AppTaskDto appTaskDto;
    private final AuthUserServiceApi authUserServiceApi;
    private final AuthTenantConfigServiceApi configServiceApi;
    /**
     * 初始化应用的待办提醒配置信息
     * @param appId 应用ID
     * @retur
     */
    private void initTaskSettings(String appId){
        appTaskDto = jvsAppService.getAppById(appId).getTaskSetting();
    }

    /**
     * 创建单据
     * @param flowTask 工作流信息
     * @param nextNode 节点内容
     * @param flowTaskPersons 待办人员集合
     * @return
     */
    @Override
    public boolean create(FlowTask flowTask, Node nextNode, List<FlowTaskPerson> flowTaskPersons){
        initTaskSettings(flowTask.getJvsAppId());
        if (!Objects.isNull(appTaskDto) && appTaskDto.getEnableTask()){
            List<String> userIds = flowTaskPersons.stream().map(FlowTaskPerson::getUserId).collect(Collectors.toList());
            List<UserDto> users = authUserServiceApi.getByIds(userIds).getData();
            Map<String,String> userMap = users.stream().collect(Collectors.toMap(UserDto::getId, UserDto::getAccountName));
            List<FlowNoticeRequestDto> flowTaskList = new ArrayList<>();
            String domain = configServiceApi.domain(TenantContextHolder.getTenantId(), ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION).getData();
            flowTaskPersons.forEach(flowTaskPerson -> {
                FlowNoticeRequestDto flowNoticeRequestDto = new FlowNoticeRequestDto();
                //访问URL按需进行拼接  需要拼接 jvsAppId
                String routeUrl = appTaskDto.getTaskFormUrl();
                routeUrl = String.format(routeUrl,flowTaskPerson.getId(), flowTask.getJvsAppId());
                String formUrl = domain+routeUrl+"?processId="+flowTask.getId()+"&nodeId="+nextNode.getId();
                flowNoticeRequestDto.setWorkNum(flowTask.getId());
                flowNoticeRequestDto.setBizInstanceId(flowTask.getId());
                flowNoticeRequestDto.setBizNodeId(nextNode.getId());
                flowNoticeRequestDto.setBizTaskId(flowTaskPerson.getId());
                flowNoticeRequestDto.setCurrentNode(nextNode.getName());
                flowNoticeRequestDto.setTitle(StringUtils.isEmpty(flowTask.getTitle()) ? flowTask.getName() : flowTask.getTitle());
                flowNoticeRequestDto.setTaskType(0);
                flowNoticeRequestDto.setHandler(userMap.get(flowTaskPerson.getUserId()));
                flowNoticeRequestDto.setHandlerName(flowTaskPerson.getUserName());
                flowNoticeRequestDto.setApplicantName(flowTask.getCreateBy());
                flowNoticeRequestDto.setFormUrl(formUrl);
                flowNoticeRequestDto.setPriority(0);
                flowNoticeRequestDto.setCreateDate(System. currentTimeMillis());
                flowTaskList.add(flowNoticeRequestDto);
            });
           FlowNoticeResponseDto response = sendRequest(FlowTaskNoticeEnum.CREATE, flowTask, flowTaskList, nextNode);
           if (response.isSuccess()){
               JSONObject result = (JSONObject)response.getData();
               Map<String,String> resultMap = (Map<String,String>)result.get("bizTaskAndTaskId");
               addTaskNotice(resultMap,nextNode, flowTask,FlowTaskNoticeEnum.CREATE);
               return true;
           }
        }
        return false;
    }
    /**
     * 关闭单据
     * @param flowTask 工作流信息
     * @param bizTaskAndTaskIds 单据ID集合
     * @return
     */
    @Override
    public boolean close(FlowTask flowTask, List<String> bizTaskAndTaskIds){
        initTaskSettings(flowTask.getJvsAppId());
        if (!Objects.isNull(appTaskDto) && appTaskDto.getEnableTask()){
            FlowNoticeResponseDto response = sendRequest(FlowTaskNoticeEnum.CLOSE, flowTask, bizTaskAndTaskIds, null);
            if (response.isSuccess()){
                updateTaskNotice(bizTaskAndTaskIds,flowTask,FlowTaskNoticeEnum.CLOSE);
            }
            return response.isSuccess();
        }
        return false;
    }
    /**
     * 撤回单据
     * @param flowTask 工作流信息
     * @param bizTaskAndTaskIds 单据ID集合
     * @return
     */
    @Override
    public boolean recall(FlowTask flowTask, List<String> bizTaskAndTaskIds){
        initTaskSettings(flowTask.getJvsAppId());
        if (!Objects.isNull(appTaskDto) && appTaskDto.getEnableTask()){
            FlowNoticeResponseDto response = sendRequest(FlowTaskNoticeEnum.RECALL, flowTask, bizTaskAndTaskIds, null);
            if (response.isSuccess()){
                updateTaskNotice(bizTaskAndTaskIds,flowTask,FlowTaskNoticeEnum.RECALL);
            }
            return response.isSuccess();
        }
        return false;
    }

    @Override
    public boolean update(FlowTask flowTask, Node nextNode, List<FlowTaskPerson> flowTaskPersons){
        return false;
    }
    /**
     * 批量保存待办对照信息
     * @param result
     * @param nextNode
     * @param task
     */
    private void addTaskNotice(Map<String,String> result,Node nextNode, FlowTask task,FlowTaskNoticeEnum flowTaskNoticeEnum){
        List<FlowTaskNotice> flowTaskNoticeList = new ArrayList<>();
        for(Map.Entry<String,String> entry:result.entrySet()){
            FlowTaskNotice flowTaskNotice = new FlowTaskNotice();
            flowTaskNotice.setInstanceId(task.getId());
            flowTaskNotice.setNodeId(nextNode.getId());
            flowTaskNotice.setTaskId(entry.getKey());
            flowTaskNotice.setBizTaskId(entry.getValue());
            flowTaskNotice.setJvsAppId(task.getJvsAppId());
            flowTaskNotice.setAppId(appTaskDto.getTaskAppId());
            flowTaskNotice.setStatus(flowTaskNoticeEnum.ordinal());
            flowTaskNoticeList.add(flowTaskNotice);
        }
        saveBatch(flowTaskNoticeList);
    }
    /**
     * 批量更新待办对照信息
     * @param bizTaskAndTaskIds 单据ID集合
     * @param task 工作流信息
     * @param flowTaskNoticeEnum 待办类型
     */
    private void updateTaskNotice(List<String>bizTaskAndTaskIds, FlowTask task,FlowTaskNoticeEnum flowTaskNoticeEnum){
        update(new LambdaUpdateWrapper<FlowTaskNotice>()
            .set(FlowTaskNotice::getStatus, flowTaskNoticeEnum.ordinal())
            .eq(FlowTaskNotice::getInstanceId, task.getId())
            .eq(FlowTaskNotice::getAppId, appTaskDto.getTaskAppId())
            .in(FlowTaskNotice::getBizTaskId, bizTaskAndTaskIds)
        );
    }

    /**
     *
     * @param flowTaskNoticeEnum 待办类型
     * @param requestData 请求入参
     * @param task 工作流信息
     * @param nextNode 下一个节点
     * @return
     */
    private FlowNoticeResponseDto sendRequest(FlowTaskNoticeEnum flowTaskNoticeEnum, FlowTask task, List requestData, Node nextNode) {
        String jsonRequest = JSONArray.toJSONString(requestData);
        FlowNoticeResponseDto responseDto = new FlowNoticeResponseDto();
        responseDto.setSuccess(false);
        FlowTaskNoticeLog flowTaskNoticeLog = new FlowTaskNoticeLog();
        String apiUrl = "";
        switch (flowTaskNoticeEnum){
            case CREATE:
                apiUrl = appTaskDto.getTaskPushApi();
                break;
            case CLOSE:
                apiUrl = appTaskDto.getTaskCloseApi();
                break;
            case RECALL:
                apiUrl = appTaskDto.getTaskRecallApi();
                break;
            case UPDATE:
                apiUrl = appTaskDto.getTaskUpdateApi();
                break;
            default:
                break;
        }
        flowTaskNoticeLog.setApiUrl(apiUrl);
        flowTaskNoticeLog.setInstanceId(task.getId());
        flowTaskNoticeLog.setType(flowTaskNoticeEnum.name());
        if (!Objects.isNull(nextNode)){
            flowTaskNoticeLog.setNodeId(nextNode.getId());
        }
        flowTaskNoticeLog.setJvsAppId(task.getJvsAppId());
        flowTaskNoticeLog.setRequestData(requestData);
        try {
            String response = sendRequest(apiUrl, jsonRequest, appTaskDto.getTaskAppId(), appTaskDto.getTaskAppSecret());
            log.info("打印统一待办response:{}", response);
            if (response != null) {
                responseDto = JSON.parseObject(response, FlowNoticeResponseDto.class);
                flowTaskNoticeLog.setResponseData(responseDto);
                // 执行成功
                if (responseDto.getCode()==0) {
                    responseDto.setSuccess(true);
                }
            }
        } catch (Exception e) {
            log.error("统一待办发送失败", e);
        }
        flowTaskNoticeLogService.save(flowTaskNoticeLog);
        return responseDto;
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
        try{
            HttpPost httpPost = new HttpPost(serverUrl);
            httpPost.setHeader("x-gw-signature", sign);
            httpPost.setHeader("x-gw-timestamp", timestamp);
            httpPost.setHeader("x-gw-nonce", nonce);
            httpPost.setHeader("x-gw-appid", appId);
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setHeader("Cache-Control", "no-cache");
            HttpEntity entity = new StringEntity(params, "UTF-8");
            SSLContext sslContext = new SSLContextBuilder()
                    // 信任所有证书（临时方案核心）
                    .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                    .build();
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    // 关闭主机名验证（避免证书域名不匹配报错）
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
            httpPost.setEntity(entity);
            response = client.execute(httpPost);
        }catch (Exception e) {
            log.error("统一待办发送失败", e);
            return null;
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

    public boolean create(Map flowParam) throws Exception {
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
        FlowNoticeRequestDto a = new FlowNoticeRequestDto();
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
        List<FlowNoticeRequestDto> bb=new ArrayList<>();
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

    // @Override
    public boolean close(){

       /* String appId = "1827966645317992450";
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
        client.close();*/

        //return EntityUtils.toString(response.getEntity(), "UTF-8");
        return true;

    }
}
