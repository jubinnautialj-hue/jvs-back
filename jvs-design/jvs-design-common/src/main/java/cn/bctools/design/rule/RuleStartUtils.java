package cn.bctools.design.rule;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.sensitive.SensitiveInfoUtils;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.config.DesignConfig;
import cn.bctools.design.rule.entity.*;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.entity.enums.type.OutputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.error.MessageTipsDto;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.utils.RuleDesignUtils;
import cn.bctools.rule.utils.TaskLogUtil;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.ResultDto;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.field.FieldDescription;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@AllArgsConstructor
@Slf4j
@Component
public class RuleStartUtils {

    RedisUtils redisUtils;
    RuleDesignService ruleDesignService;
    RunLogService runLogService;
    OssTemplate ossTemplate;
    static final String http = "http";

    static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
    DesignConfig designConfig;

    public final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            MAX_THREADS,
            MAX_THREADS,
            10L,
            TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    public static final DateTimeFormatter FORMAT_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String FORMAT_LOG = "%s [ %s ] %s ";

    /**
     * 根据逻辑执行获取结果，并返回响应头信息
     *
     * @param response    响应对象
     * @param po          逻辑设计
     * @param logPo       日志对象
     * @param data        执行数据
     * @param ruleExecDto
     * @return
     */
    public R getRuleReturn(HttpServletResponse response, RuleDesignPo po, RunLogPo logPo, RuleExecuteDto data, RuleExecDto ruleExecDto) {
        start(po, logPo, ruleExecDto);
        //如果最后的返回为流式返回结果,则直接导出文件结果
        if (ObjectNull.isNotNull(data.getEndResult()) && ClassType.文件.equals(data.getEndResult().getClassType())) {
            //处理流式输出
            RuleFile value = (RuleFile) data.getEndResult().getValue();
            //判断最后输出节点的类型, 看是否是文件输出类型,如果是就以文件形式的Post结果输出如果是异步
            if (value.getOutputType().equals(OutputType.download)) {
                response.setHeader("output_format", URLUtil.encode(value.getOriginalName()));
            }
            //兼容预览和下载格式
            response.setHeader("output_type", value.getOutputType().toString());
            if (!value.getUrl().startsWith(http)) {
                value.setUrl(ossTemplate.fileLink(value.getFileName(), value.getBucketName()));
            }
            return R.ok(value);
        }
        //返回执行日志对象
        //获取同步返回结果
        R r = R.ok().setMsg("");
        //如果最后一个节点为消息节点
        if (ruleExecDto.getExecuteDto().getStats() && ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getMessageResult())) {
            //成功的返回消息
            r = R.ok(ruleExecDto.getExecuteDto().getMessageResult()).setMsg(ruleExecDto.getExecuteDto().getSyncMessageTips());
        } else if (!ruleExecDto.getExecuteDto().getStats()) {
            //写入消息状态,末认所有都是返回成功状态
            response.setHeader("output_status", ruleExecDto.getExecuteDto().getStats().toString());
            ResultDto endResult = ruleExecDto.getExecuteDto().getEndResult();
            if (endResult.getValue() instanceof MessageTipsDto) {
                MessageTipsDto value = (MessageTipsDto) endResult.getValue();
                response.setHeader("message_close", String.valueOf(value.getOff()));
                r = R.ok().setMsg(ruleExecDto.getExecuteDto().getSyncMessageTips()).setData(value.getData());
            } else {
                r = R.ok().setMsg(ruleExecDto.getExecuteDto().getErrorMessage());
            }
        } else if (ruleExecDto.getExecuteDto().getStats() && ObjectNull.isNotNull(ruleExecDto.getExecuteDto().getErrorMessage())) {
            response.setHeader("output_status", String.valueOf(false));
            r = R.ok().setMsg(ruleExecDto.getExecuteDto().getErrorMessage());
        } else if (ObjectNull.isNotNull(data.getEndResult())) {
            Object value = data.getEndResult().getValue();
            if (value instanceof MessageTipsDto) {
                response.setHeader("output_status", ((MessageTipsDto) value).getOnOff().toString());
                response.setHeader("message_close", ((MessageTipsDto) value).getOff().toString());
                r.setData(((MessageTipsDto) value).getData()).setMsg(((MessageTipsDto) value).getMessage());
            } else {
                r.setData(data.getEndResult().getValue());
            }
        }
        return r;
    }

    public void start(RuleDesignPo po, RunLogPo logPo, RuleExecDto data) {
        //添加执行缓存， 根据类型判断是否有缓存，如果有缓存直接返回数据,不能测试类型
        if (ObjectNull.isNotNull(po.getCacheMinute()) && po.getCacheMinute() > 0 && !data.getType().equals(RunType.TEST)) {
            if (ObjectNull.isNull(po.getReqType())) {
                //兼容历史数据
                po.setReqType(RuleType.Low_code_logic);
            }
            String redisKey = SysConstant.redisKey("rule:cache:" + data.getSecret(), JSON.toJSONString(data.getExecuteDto().getReqVariableMap()));
            switch (po.getReqType()) {
                case External_API_logic:
                case Source_code_development_docking_logic:
                case Low_code_logic:
                    data.getExecuteDto().setStats(true).setEndResult((ResultDto) redisUtils.get(redisKey));
                default:
                    //其它类型不处理
            }
            //如果结果不为空,将执行结果添加到缓存中
            if (ObjectNull.isNotNull(data.getExecuteDto().getEndResult()) && (data.getExecuteDto().getStats())) {
                return;
            }
        }
        SystemThreadLocal.set("jvsAppId", po.getJvsAppId());
        run(po, logPo, data);

    }

    private void run(RuleDesignPo po, RunLogPo logPo, RuleExecDto data) {
        logPo.setTid(IdGenerator.getIdStr(36));
        data.setTid(logPo.getTid());
        data.setOpenLogRecording(Optional.ofNullable(po.getOpenLogRecording()).orElse(true));
        logPo.setParentId(data.getSecret());
        RuleDesignUtils.sequentialExecution(po.getDesignDrawingJson(), data);
        data.getExecuteDto().setIsEnd(true);
        logPo.setVariableMap(data.getExecuteDto().getVariableMap());
        //避免返回数据带有引用关系
        logPo.setErrorMsg(data.getExecuteDto().getErrorMessage());
        //组装打印日志
        String tenantId = TenantContextHolder.getTenantId();
        logPo.setTenantId(tenantId);
        logPo.setEndTime(LocalDateTime.now());
        LinkedList<TaskLogUtil.Log> logsList = TaskLogUtil.get();
        SecurityContext context = SecurityContextHolder.getContext();
        //校验返回参数类型
        if (RuleType.External_API_logic.equals(po.getReqType())) {

            try {
                if (ObjectNull.isNotNull(data.getExecuteDto().getEndResult())) {
                    Object value = data.getExecuteDto().getEndResult().getValue();
                    if (value instanceof List) {
                        //如果是数组，则分批处理并聚合
                        List<Object> collect = ((List<?>) value).stream().map(e -> checkParameterOut(e, po.getParameterOut())).collect(Collectors.toList());
                        data.getExecuteDto().getEndResult().setValue(collect);
                    } else {
                        //如果是对象直接 copy
                        Object obj = checkParameterOut(value, po.getParameterOut());
                        //正确的直接将返回数据值处理进去
                        data.getExecuteDto().getEndResult().setValue(obj);
                    }
                }
            } catch (Exception e) {
                //将当前节点放到错误节点里面
                data.getExecuteDto().getErrorNodeId().add(data.getExecuteDto().getExecNodeId());
                data.getExecuteDto().getEndResult().setErrorMessage(e.getMessage());
                data.getExecuteDto().setStats(false);
                data.getExecuteDto().setErrorMessage(e.getMessage());
                data.getExecuteDto().setMessageResult(e.getMessage());
            }

        }
        //避免返回数据带有引用关系
        logPo.setErrorMsg(data.getExecuteDto().getErrorMessage());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        EXECUTOR.execute(() -> {
            log.info("线程保存日志");
            TenantContextHolder.setTenantId(tenantId);
            RequestContextHolder.setRequestAttributes(requestAttributes);
            SecurityContextHolder.setContext(context);
            logPo.setResult(JSON.parseObject(JSON.toJSONString(data.getExecuteDto(), JSONWriter.Feature.LargeObject)));
            if (data.getOpenLogRecording()) {
                String logs = logsList
                        .stream()
                        .distinct()
                        .map(e -> String.format(FORMAT_LOG, FORMAT_DATETIME.format(e.getTime()), e.getType(), e.getMsg()))
                        .collect(Collectors.joining("<br/>"));
                BaseFile baseFile = ossTemplate.putFile("jvs-design-mgr", "rule/run/log", IdGenerator.getIdStr() + ".log", new ByteArrayInputStream(logs.getBytes()));
                logPo.setLogs(baseFile.getFileName());
            } else {
                logPo.setLogs("未开启日志");
            }
            logPo.setStatus(ObjectNull.isNull(data.getExecuteDto().getErrorMessage(), data.getExecuteDto().getErrorNodeId()));
            runLogService.saveLog(logPo);
            log.info("线程保存日志完成");
        });

        //如果是测试执行才做结构定义推断
        if (logPo.getRunType().equals(RunType.TEST)) {
            data.getGraph().getLineList().forEach(e -> {
                //如果线的类型不是空，也不是异常线，则置空进行处理，避免下次正式运行时存在问题
                if (ObjectNull.isNotNull(e.getState()) && !"abnormal".equals(e.getState()) && !"async".equals(e.getState())) {
                    e.setState("");
                }
            });
            String designDrawingJson = JSONObject.toJSONString(data.getGraph());
            //动态定义设计结构
            ruleDesignService.update(Wrappers.lambdaUpdate(RuleDesignPo.class).eq(RuleDesignPo::getId, po.getId()).eq(RuleDesignPo::getUpdateTime, po.getUpdateTime()).set(RuleDesignPo::getDesignDrawingJson, designDrawingJson));
        }
    }

    /**
     * 校验出参数据格式
     *
     * @param value        数据值
     * @param parameterOut 校验格式
     */
    private Object checkParameterOut(Object value, RuleParameterInDto parameterOut) {
        if (ObjectNull.isNull(parameterOut)) {
            return value;
        }
        //检查数组信息
        List<BodyInDto> checkList = parameterOut.getBodyList();
        if (ObjectNull.isNull(checkList)) {
            return value;
        }
        //当前这一级的数据进行校验
        return recursiveVerification(new HashMap(), value, checkList);
    }

    /**
     * 校验数据
     *
     * @param obj       新的数据结构对象
     * @param value     逻辑处理的最后一个值
     * @param checkList
     * @return
     */
    private static Object recursiveVerification(Object obj, Object value, List<BodyInDto> checkList) {
        //判断第一层是数组还是对象
        for (BodyInDto bodyInDto : checkList) {
            //校验必填写
            String path = bodyInDto.getPath();
            Object read = JvsJsonPath.read(value, path);
            if (Optional.ofNullable(bodyInDto.getNecessity()).orElse(false) && ObjectNull.isNull(read)) {
                //报错
                throw new RuleException(RuleExceptionEnum.出参校验不通过, bodyInDto.getLabel() + "必填写");
            } else if (ObjectNull.isNotNull(read)) {
                //校验类型是否正常
                if (!inputTypeMap.get(bodyInDto.getInputType()).isAssignableFrom(read.getClass())) {
                    throw new RuleException(RuleExceptionEnum.出参校验不通过, bodyInDto.getLabel() + "类型不正确");
                }
                //正则校验
                if (ObjectNull.isNotNull(bodyInDto.getRule())) {
                    if (!Pattern.compile(bodyInDto.getRule()).matcher(read.toString()).matches()) {
                        throw new RuleException(RuleExceptionEnum.出参校验不通过, bodyInDto.getLabel() + "校验不通过");
                    }
                }
                //如果是字符串类型则处理脱敏处理
                if (inputTypeMap.get(bodyInDto.getInputType()).equals(String.class) && ObjectNull.isNotNull(bodyInDto.getEncryptionExpress())) {
                    //是否有脱敏
                    read = SensitiveInfoUtils.getSensitiveKey().get(bodyInDto.getEncryptionExpress()).apply(read.toString());
                }
                JvsJsonPath.set(obj, path, read);
            } else {
                if (ObjectNull.isNotNull(bodyInDto.getDefaultValue())) {
                    //设置默认的值
                    //没有默认值，系统自动给默认值
                    if (String.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, bodyInDto.getDefaultValue());
                    }
                    if (Integer.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, Integer.valueOf(bodyInDto.getDefaultValue().toString()));
                    }
                    if (Boolean.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, Boolean.valueOf(bodyInDto.getDefaultValue().toString()));
                    }
                    if (List.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, JSONArray.parseArray(bodyInDto.getDefaultValue().toString()));
                    }
                    if (Object.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, com.alibaba.fastjson2.JSONObject.parseObject(bodyInDto.getDefaultValue().toString()));
                    }
                    if (Number.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, new BigDecimal(bodyInDto.getDefaultValue().toString()));
                    }
                } else {
                    //没有默认值，系统自动给默认值
                    if (String.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, "");
                    }
                    if (Integer.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, 0);
                    }
                    if (Boolean.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, false);
                    }
                    if (List.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, new ArrayList<>());
                    }
                    if (Object.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, new HashMap<>());
                    }
                    if (Number.class.equals(inputTypeMap.get(bodyInDto.getInputType()))) {
                        JvsJsonPath.set(obj, path, new BigDecimal(0));
                    }
                }
            }

            if (ObjectNull.isNotNull(bodyInDto.getChildren())) {
                //下一层数据结构
                recursiveVerification(obj, value, bodyInDto.getChildren());
            }
        }
        return obj;
    }


    static final Map<String, Class> inputTypeMap = new HashMap<>();

    static {
        inputTypeMap.put("string", String.class);
        inputTypeMap.put("integer", Integer.class);
        inputTypeMap.put("boolean", Boolean.class);
        inputTypeMap.put("array", List.class);
        inputTypeMap.put("object", Object.class);
        inputTypeMap.put("number", Number.class);
    }
}
