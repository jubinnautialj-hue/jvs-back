package cn.bctools.remote.component;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.remote.config.DorisUtils;
import cn.bctools.remote.dto.AsyncDataDto;
import cn.bctools.remote.dto.DataSourceDetail;
import cn.bctools.remote.dto.JvsDataRemoteSecretDto;
import cn.bctools.remote.log.service.RemoteLogService;
import cn.bctools.remote.po.InParameterJsonPo;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataFactoryComponent {

    private final RemoteLogService remoteLogService;
    private final DataFactoryApi dataFactoryApi;
    private final DorisUtils dorisUtils;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 根据设计id获取数据
     *
     * @param remoteServer api服务信息
     * @param dataFactory  数据智仓信息
     * @return
     */
    @SuppressWarnings("all")
    public List<Map<String, Object>> getData(JvsDataRemoteServer remoteServer,
                                             JvsDataRemoteSecretDto secret,
                                             Map<String, Object> params,
                                             String invoker,
                                             String ip) {
        List<Map<String, Object>> data = Collections.emptyList();
        Boolean execute = Boolean.TRUE;
        try {
            //获取数据
            List<Map<String, Object>> dataFromFactory = this.getDataFromFactory(remoteServer, params);
            data = dataFromFactory.stream().map(e -> (Map<String, Object>) e).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("-----数据服务异常:获取数据失败-----");
            e.printStackTrace();
            execute = Boolean.FALSE;
        } finally {
            //保存调用日志
            remoteLogService.simpleSave(remoteServer, secret, execute, CollectionUtil.isNotEmpty(data), invoker, ip);
            ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                    .setTenantId(remoteServer.getTenantId())
                    .setDesignName(remoteServer.getName())
                    .setDesignId(remoteServer.getId())
                    .setType(3)
                    .setViewType(ConsanguinityViewTypeEnum.remote)
                    .setDataFactoryId(remoteServer.getDataSourceDetail().getExecuteName());
            //发送血缘关系消息
            send(consanguinityAnalyse);
        }
        return data;
    }

    /**
     * 获取数据
     *
     * @return
     */
    private List<Map<String, Object>> getDataFromFactory(JvsDataRemoteServer remoteServer, Map<String, Object> inParams) {
        DataSourceDetail dataSourceDetail = remoteServer.getDataSourceDetail();
        String outParameter = remoteServer.getOutParameter();
        if (StrUtil.isBlank(outParameter)) {
            throw new BusinessException("未设置返回字段");
        }
        TenantContextHolder.setTenantId(remoteServer.getTenantId());
        List<String> respFields = JSONUtil.parseArray(outParameter).stream().map(JSONUtil::parseObj).map(e -> {
            InParameterJsonPo inParameterJsonPo = e.toBean(InParameterJsonPo.class);
            String sql = "`" + inParameterJsonPo.getKey() + "`";
            if (StrUtil.isNotBlank(inParameterJsonPo.getAsKey())) {
                sql = sql + " as `" + inParameterJsonPo.getAsKey() + "`";
            }
            return sql;
        }).collect(Collectors.toList());
        R<String> r = dataFactoryApi.getTableName(dataSourceDetail.getExecuteName());
        if (!r.is()) {
            throw new BusinessException(r.getMsg());
        }
        String tableName = r.getData();
        String inParameter = remoteServer.getInParameter();
        if (StrUtil.isNotBlank(inParameter)) {
            Map<String, Object> map = new HashMap<String, Object>(inParams.size());
            JSONUtil.parseArray(inParameter).stream().map(JSONUtil::parseObj)
                    .map(e -> e.toBean(InParameterJsonPo.class))
                    .filter(e -> {
                        //判断是否有传入参数 如果没有判断是否存在默认值 如果都没有 就需要舍弃此数据
                        if (inParams.containsKey(e.getKey())) {
                            return true;
                        }
                        if (StrUtil.isNotBlank(e.getAsKey()) && inParams.containsKey(e.getAsKey())) {
                            inParams.put(e.getKey(), inParams.get(e.getAsKey()));
                            return true;
                        }
                        if (ObjUtil.isNotEmpty(e.getDefaultValue())) {
                            return true;
                        }
                        return false;
                    }).forEach(e -> map.put(e.getKey(), inParams.getOrDefault(e.getKey(), e.getDefaultValue())));
            inParams.clear();
            inParams.putAll(map);
        } else {
            inParams.clear();
        }
        return dorisUtils.search(tableName, inParams, respFields);
    }

    /**
     * 异步执行智仓设计 获取数据
     *
     * @param remoteServer api服务信息
     * @param executeNo    执行批次号
     */
    @Async
    public void getAsyncData(JvsDataRemoteServer remoteServer,
                             JvsDataRemoteSecretDto secret,
                             String executeNo,
                             Map<String, Object> params,
                             String invoker,
                             String ip) {
        String callbackAddr = remoteServer.getCallbackAddr();
        if (StrUtil.isNotBlank(callbackAddr)) {
            AsyncDataDto asyncDataDto = new AsyncDataDto();
            List<Map<String, Object>> data = this.getData(remoteServer, secret, params, invoker, ip);
            asyncDataDto.setExecuteNo(executeNo).setData(data);
            HttpUtil.createPost(callbackAddr)
                    .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                    .body(JSONUtil.toJsonStr(asyncDataDto)).execute();
        }
    }

    /**
     * 检查必填参数 以及参数格式
     *
     * @param inParams 参数
     * @param params   入参
     * @return 异常消息
     */
    public String checkParams(List<InParameterJsonPo> inParams, Map<String, Object> params) {
        for (InParameterJsonPo inParam : inParams) {
            String key = StrUtil.isNotBlank(inParam.getAsKey())?inParam.getAsKey():inParam.getKey();
            if (params.containsKey(key)) {
//                try {
//                    Object o = params.get(key);
//                    Class<?> aClass = inParam.getFieldType().getAClass();
//                    if (!o.getClass().equals(aClass)) {
//                        return StrUtil.format("参数:{}类型异常,类型应为:{}", inParam.getName(), inParam.getFieldType());
//                    }
//                } catch (ConvertException e) {
//                    return StrUtil.format("参数:{}类型异常,类型应为:{}", inParam.getName(), inParam.getFieldType());
//                }
            } else if (inParam.getRequiredIs()) {
                return StrUtil.format("参数:{}必填", inParam.getName());
            }
        }
        return null;
    }

    /**
     * 检查必填参数 以及参数格式
     *
     * @return 异常消息
     */
    public List<InParameterJsonPo> getAllInParameters(JvsDataRemoteServer remoteServer) {
        List<InParameterJsonPo> allInParameters = new ArrayList<>();
        String inParameter = remoteServer.getInParameter();
        if (StrUtil.isNotBlank(inParameter)) {
            List<InParameterJsonPo> parameterDtos = JSONUtil.toList(inParameter, InParameterJsonPo.class);
            allInParameters.addAll(parameterDtos);
        }
        return allInParameters;
    }

    /**
     * 生成返回示例
     *
     * @param outParams
     * @return
     */
    public String generateOutExample(String outParams) {
        List<Map<String, Object>> exampleData = new ArrayList<>();
        if (StrUtil.isNotBlank(outParams)) {
            List<InParameterJsonPo> parameterDtos = JSONUtil.toList(outParams, InParameterJsonPo.class);
            Map<String, Object> exampleItem = parameterDtos.stream().peek(e -> e.setValue("")
                    .setKey(StrUtil.isNotBlank(e.getAsKey()) ? e.getAsKey() : e.getKey())).collect(Collectors.toMap(InParameterJsonPo::getKey, InParameterJsonPo::getValue));
            exampleData = Collections.singletonList(exampleItem);
        }
        Map<String, Object> example = new HashMap<>();
        example.put("code", 0);
        example.put("message", "success");
        example.put("data", exampleData);
        example.put("timestamp", DateUtil.now());
        return JSONUtil.toJsonStr(example);
    }

    /**
     * 血缘关系 消息发送
     *
     * @param consanguinityAnalyse
     */
    public void send(ConsanguinityAnalyse consanguinityAnalyse) {
        consanguinityAnalyse.setType(3)
                .setViewType(ConsanguinityViewTypeEnum.screen);
        rabbitTemplate.convertAndSend("consanguinity_analyse_task", consanguinityAnalyse);
    }

}
