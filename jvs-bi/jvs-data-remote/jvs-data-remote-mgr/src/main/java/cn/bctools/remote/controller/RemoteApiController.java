package cn.bctools.remote.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.remote.component.DataFactoryComponent;
import cn.bctools.remote.dto.JvsDataRemoteSecretDto;
import cn.bctools.remote.dto.JvsRemoteServerQueryDto;
import cn.bctools.remote.enums.CredentialTypeEnum;
import cn.bctools.remote.log.entity.RemoteLog;
import cn.bctools.remote.log.service.RemoteLogService;
import cn.bctools.remote.po.InParameterJsonPo;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.bctools.remote.service.JvsDataRemoteServerService;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "[api]数据服务调用")
public class RemoteApiController {

    private final JvsDataRemoteServerService jvsDataRemoteServerService;
    private final DataFactoryComponent dataFactoryComponent;
    private final RemoteLogService remoteLogService;

    @ApiOperation("调用api获取智仓设计结果")
    @PostMapping("/api/run/{remoteId}")
    public R<List<Map<String, Object>>> run(@ApiParam("api服务id")@PathVariable("remoteId")String remoteId,
                                            @RequestHeader(value = "invoker",required = false)String invoker,
                                            @RequestHeader(value = "secret",required = false)String secret,
                                            @RequestBody(required = false) Map<String,Object> params, HttpServletRequest request, HttpServletResponse response){
        if(StrUtil.isBlank(invoker)){
            return R.failed("未检测到调用方(invoker)");
        }
        if(params == null){
            params = new HashMap<>();
        }

        //清除租户缓存 避免查询时追加租户限制
        TenantContextHolder.clear();
        //服务是否存在
        JvsDataRemoteServer remoteServer = jvsDataRemoteServerService.getById(remoteId);
        if(ObjectUtil.isNull(remoteServer)){
            return R.failed(StrUtil.format("服务:{}不存在",remoteId));
        }
        if(!remoteServer.getEnableIs()){
            return R.failed(StrUtil.format("服务:{}未启用",remoteServer.getName()));
        }
        //获取凭证组
        List<JvsDataRemoteSecretDto> jvsDataRemoteSecretDtos = Optional.of(remoteServer).map(JvsDataRemoteServer::getSecretJson).map(e -> JSONUtil.toList(e, JvsDataRemoteSecretDto.class)).orElse(Collections.emptyList());
        Map<String, JvsDataRemoteSecretDto> secretMap = jvsDataRemoteSecretDtos.stream().collect(Collectors.toMap(JvsDataRemoteSecretDto::getSecret, Function.identity()));

        //检查必填参数和参数类型
        List<InParameterJsonPo> allInParameters = dataFactoryComponent.getAllInParameters(remoteServer);
        String msg = dataFactoryComponent.checkParams(allInParameters, params);
        if(StrUtil.isNotBlank(msg)){
            return R.failed(msg);
        }

        String ipAddr = IpUtil.getIpAddr(request);
        //当前请求ip是否在白名单
        String whiteList = remoteServer.getWhiteList();
        boolean whiteIn = this.matchingIp(ipAddr, whiteList);
        if(!whiteIn && remoteServer.getWhiteStatus()){
            return R.failed(StrUtil.format("当前ip:{} 未在白名单中",ipAddr));
        }
        List<Map<String, Object>> data;
        CredentialTypeEnum credentialType = remoteServer.getCredentialType();
        if(ObjectUtil.equals(credentialType,CredentialTypeEnum.none)){
            JvsDataRemoteSecretDto publicModel = new JvsDataRemoteSecretDto().setSecret("1").setRemark("公开访问");
            data = this.getData(remoteServer,params,publicModel,response,request,invoker,ipAddr);
            return R.ok(data);
        }

        //服务类型密码
        if(StrUtil.isNotBlank(secret) && secretMap.containsKey(secret)){
            data =  this.getData(remoteServer,params,secretMap.get(secret),response,request,invoker,ipAddr);
            return R.ok(data);
        }
        return R.failed("凭证异常,当前凭证不正确");
    }


    @SneakyThrows
    @ApiOperation("下载API示例代码")
    @GetMapping("/download/code")
    public void downloadCode(HttpServletResponse response) {
        //查询，替换
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String s1 = "attachment; filename=".concat(URLUtil.encode("demo.java", StandardCharsets.UTF_8));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s1);
        response.setStatus(HttpStatus.OK.value());
        ServletOutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new ClassPathResource("Demo.java").getInputStream();
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.flush();
    }


    @SneakyThrows
    @ApiOperation("查询调用日志")
    @GetMapping("/remote/api/call/log")
    public R<Page<RemoteLog>> pageLog(Page<RemoteLog> page, JvsRemoteServerQueryDto dto) {
        Page<RemoteLog> remoteLogPage = remoteLogService.queryByCondition(page, dto);
        return R.ok(remoteLogPage);
    }

    /**
     * 匹配ip 可模糊匹配
     * 模糊匹配：例如：10.0.*.* 那么10.0.1.2匹配为true 10.1.2.3为false
     * @param currentIp
     * @param ipListStr
     * @return
     */
    private boolean matchingIp(String currentIp,String ipListStr){
        log.info("访问ip：{}",currentIp);
        boolean flag = Boolean.TRUE;
        try {
            if(StrUtil.isBlank(ipListStr)){
                return true;
            }
            List<String> ipList = JSONUtil.toList(ipListStr, String.class);
            if(CollectionUtil.isEmpty(ipList)){
                return true;
            }

            for (String ip : ipList){
                String replace = ip.replace(".", "\\.").replace("*", "\\d{1,3}");
                Pattern pattern = Pattern.compile(replace);
                Matcher matcher = pattern.matcher(currentIp);
                if(matcher.matches()){
                    flag = Boolean.FALSE;
                    break;
                }
            }
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取数据
     * @param remoteServer api服务信息
     * @param response 响应
     * @return
     */
    private List<Map<String, Object>> getData(JvsDataRemoteServer remoteServer,
                                              Map<String,Object> params,
                                              JvsDataRemoteSecretDto secret,
                                              HttpServletResponse response,
                                              HttpServletRequest request,
                                              String invoker,String ip){
        if(!remoteServer.getAsyncIs()){
            return dataFactoryComponent.getData(remoteServer,secret,params,invoker,ip);
        }
        String callbackAddr = request.getHeader("callback_addr");
        remoteServer.setCallbackAddr(callbackAddr);
        String executeNo = IdUtil.fastSimpleUUID();
        response.setHeader("EXECUTE_NO",executeNo);
        dataFactoryComponent.getAsyncData(remoteServer,secret,executeNo,params,invoker,ip);
        return null;
    }
}
