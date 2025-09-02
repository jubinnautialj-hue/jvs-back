package cn.bctools.design.rule.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.entity.enums.type.OutputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.error.MessageTipsDto;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.UrlUtils;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.ResultDto;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 * @describe 跑逻辑与设计请求接口
 */
@Slf4j
@AllArgsConstructor
@Api(tags = "逻辑执行")
@RestController
@RequestMapping("/app/use/{appId}/rule")
public class RunController {

    RedisUtils redisUtils;
    DesignHandler designHandler;
    RuleDesignService ruleService;
    RuleStartUtils ruleStartUtils;
    RunLogService runLogService;
    DataFieldService dataFieldService;
    FormService formService;
    Map<String, IDataFieldHandler> iDataFieldHandler;
    TableFormFieldHandler tableFormFieldHandler;
    TabFieldHandler tabFieldHandler;
    DynamicDataService dynamicDataService;
    OssTemplate ossTemplate;
    static final String http = "http";

    /**
     * 根据逻辑标识key运行逻辑流程, 并返回执行结果
     * <p>
     * 参数优先级:
     * 路径参数 > 请求体参数 > 数据库参数
     *
     * @param ruleKey      请求模板参数
     * @param variableMap  数据值对象
     * @param formdesignid 表单回显接口判断按钮是否显隐
     * @return 运行跑出来的结果数据
     * @author guojing
     */
    @SneakyThrows
    @Log(back = false)
    @ApiOperation("执行逻辑")
    @PostMapping("/run/{ruleKey}")
    public Object run(@PathVariable String ruleKey, @RequestBody(required = false) Map<String, Object> variableMap, @RequestHeader(value = "formdesignid", required = false) String formdesignid, @RequestHeader(value =
            "cruddesignid", required = false) String cruddesignid, @PathVariable String appId, HttpServletResponse response) {
        //根据参数获取的值
//        RuleSystemThreadLocal.setParameterSelectedOption(variableMap);
        //根据租户ID调用自己的服务
        RuleDesignPo po = ruleService.getEnableDesign(ruleKey);
        if (ObjectNull.isNotNull(cruddesignid) && variableMap.containsKey("dataModelId") && variableMap.containsKey("id")) {
            //覆盖新的数据
            Map<String, Object> stringObjectMap = dynamicDataService.querySingle(appId, variableMap.get("dataModelId").toString(), variableMap.get("id").toString());
            variableMap.putAll(stringObjectMap);
        }
        //直接返回
        if (ObjectNull.isNull(po)) {
            log.error("没有找到设计");
            return R.ok().setMsg("");
        }
        //不匹配直接返回
        if (!po.getJvsAppId().equals(appId)) {
            log.error("应用和设计应用不一致");
            return R.ok().setMsg("");
        }
        if (ObjectNull.isNotNull(po.getComponentType())) {
            //如果是表单，直接查看是否有表单内部的删除数据
            //todo 此处数据结构，需要判断是否是脱离数据的结构，用于判断数据类型
            switch (po.getComponentType()) {
                case form:
                    FormPo byId = formService.getById(po.getComponentDesignId());
                    if (ObjectNull.isNotNull(variableMap, byId) && !variableMap.isEmpty()) {
                        dataFieldService.getFields(appId, byId.getDataModelId(), true, false).stream().filter(e -> DataFieldType.CONTAINER.contains(e.getFieldType())).forEach(e -> {
                            FieldPublicHtml publicHtml = iDataFieldHandler.get(e.getFieldType().getDesc()).toHtml(e);
                            if (ObjectNull.isNotNull(publicHtml)) {
                                //保存或更新下级表格里的数据
                                delTableLine(publicHtml, variableMap, publicHtml.getDataLinkageModelId());
                            }
                        });
                    }
                    //根据表单结构，做删除数据操作
                default:
                    //其它类型不做操作
            }
        }
        log.info("执行逻辑, ruleKey: {}, 租户id: {}", ruleKey, po.getTenantId());
        // 获取逻辑流程运行时参数
        // 1.获取数据库参数
        Map<String, Object> ruleVariable = new HashMap<>(16);
        if (po.getParameterPos() != null) {
            ruleVariable.putAll((po.getParameterPos()));
        }
        // 2.获取请求体参数
        if (ObjectUtils.isNotEmpty(variableMap)) {
            ruleVariable.putAll(variableMap);
        }

        // 3.获取请求路径上的参数(优先级最高)
        Map<String, Object> urlParams = UrlUtils.getUrlParams();
        if (ObjectUtils.isNotEmpty(urlParams)) {
            ruleVariable.putAll(urlParams);
        }
        RunLogPo logPo = runLogService.create(appId, po.getSecret(), RunType.REAL, ruleVariable, po.getReqType(), po.getReqType(), po.getSync());
        ruleVariable.put("ruleKey", ruleKey);
        RuleExecuteDto data = new RuleExecuteDto().setVariableMap(ruleVariable).setReqVariableMap(ruleVariable);
        String key = String.format(RuleConstant.RULE_KEY_FORMAT, logPo.getId());
        SystemThreadLocal.set("redisKey", key);
        redisUtils.set(key, data, Long.valueOf(60 * 5));
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        RuleExecDto ruleExecDto = new RuleExecDto().setExecuteDto(data).setType(RunType.REAL).setSecret(po.getSecret()).setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
        R r = getRuleReturn(response, po, logPo, data, key, authentication, ruleExecDto);
        //如果是表单触发的逻辑，需要进行控制按钮是否需要使用
        if (ObjectNull.isNotNull(formdesignid) && r.getData() instanceof Map) {
            DynamicDataUtils.setDesignId(formdesignid);
            designHandler.handleButtonInfo((Map<String, Object>) r.getData(), EnvConstant.FORM_BUTTON_DISPLAY);
            //转换数据值
            FormPo formPo = formService.getById(formdesignid);
            Map<String, Object> body = dynamicDataService.paresMapWithEcho(appId, (Map<String, Object>) r.getData(), formPo.getDataModelId(), formdesignid, false);
            r.setData(body);
        }
        return r;
    }

    private R getRuleReturn(HttpServletResponse response, RuleDesignPo po, RunLogPo logPo, RuleExecuteDto data, String key, Authentication authentication, RuleExecDto ruleExecDto) {
        ruleStartUtils.start(po, logPo, ruleExecDto);
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

    /**
     * 下载逻辑引擎流文件
     *
     * @param value
     * @return
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestBody RuleFile value) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("output_format", URLUtil.encode(value.getOriginalName()));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.formData().filename(URLUtil.encode(value.getOriginalName() + value.getFileType())).build());
        headers.set("Access-Control-Expose-Headers", "Content-Disposition");
        return new ResponseEntity<byte[]>(HttpUtil.downloadBytes(value.getUrl()), headers, HttpStatus.OK);
    }


    /**
     * 递归删除表格里面的数据
     *
     * @param publicHtml
     * @param data
     * @param modelId
     */
    private void delTableLine(FieldPublicHtml publicHtml, Map<String, Object> data, String modelId, String... parentPath) {
        List<String> strings = new ArrayList<String>(Arrays.asList(parentPath));
        strings.add(publicHtml.getProp());
        //如果有表格数据，但没有设计，可能导致类型为空
        if (ObjectNull.isNull(publicHtml.getType())) {
            return;
        }
        switch (publicHtml.getType()) {
            case tableForm:
                //递归获取下级的控件，判断是否是表格控件，如果是表格控件，并模型不为空，则把控件里面的数据，把控件数据获取出来进行保存或更新
                TableFormItemHtml tableFormItemHtml = tableFormFieldHandler.toHtml(publicHtml.getDesignJson());
                String dataKey = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining());
                String tablePathKey = strings.stream().filter(ObjectNull::isNotNull).collect(Collectors.joining(StrUtil.DOT));
                //如果是表格组件,直接更新数据，根据路径获取数据
                JSONPath.remove(data, tablePathKey + "_line");
                //判断有没有删除的字段
                Object dels = JvsJsonPath.read((data), tablePathKey + "_del");
                //关联模型不为空的情况
                if (ObjectNull.isNotNull(dels) && ObjectNull.isNotNull(tableFormItemHtml.getDataModelId())) {
                    List<Map> linkedHashMapList = (List) dels;
                    linkedHashMapList.forEach(e -> {
                        String id = Optional.ofNullable(e.get(Get.name(DataFieldPo::getId))).map(String::valueOf).orElse(null);
                        //删除字段
                        if (ObjectNull.isNotNull(id)) {
                            try {
                                dynamicDataService.onlyRemove(tableFormItemHtml.getDataModelId(), id);
                            } catch (BusinessException businessException) {
                                //屏蔽删除错误，可能数据已经被删除了
                            }
                        }
                    });
                }
                break;
            case tab:
                TabItemHtml tabItemHtml = tabFieldHandler.toHtml(publicHtml.getDesignJson());
                if (tabItemHtml.getNext()) {
                    Map<String, List<FieldBasicsHtml>> column = tabItemHtml.getColumn();
                    for (String tabKey : column.keySet()) {
                        List<FieldPublicHtml> fieldPublicHtmls = column.get(tabKey).stream().filter(s -> DataFieldType.CONTAINER.contains(s.getType())).collect(Collectors.toList());
                        if (ObjectNull.isNotNull(fieldPublicHtmls)) {
                            for (FieldPublicHtml fieldPublicHtml : fieldPublicHtmls) {
                                List<String> tableKeys = new ArrayList<String>(strings);

                                tableKeys.add(tabKey);
                                List<String> collect = tableKeys.stream().filter(ObjectNull::isNotNull).collect(Collectors.toList());
                                //将数据再传递至下级控件继续递归
                                delTableLine(fieldPublicHtml, data, modelId, collect.toArray(new String[0]));
                            }
                        }
                    }
                }
                //寻找下级
                break;
            case step:
                //TODO 步骤条下级还未解析
                break;
            default:
                break;
        }
    }

}

