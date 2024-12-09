package cn.bctools.design.rule.job;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.StackTraceElementUtils;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.constant.OssConstantType;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.RuleStartUtils;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.util.CurrentAppUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.oss.utils.OssUtils;
import cn.bctools.rule.entity.enums.RunType;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.HtmlGraph;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 调用逻辑的Handler
 *
 * @author My_gj
 */
@Component
public class RuleCommonPostHandler {

    @Resource
    JvsAppVersionService jvsAppVersionService;
    @Resource
    RuleStartUtils ruleStartUtils;
    @Resource
    RunLogService runLogService;
    @Resource
    RuleDesignService ruleService;

    @XxlJob("jvs-design-mgr")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("定时任务开始执行，入参为:{}", param);
        //兼容json格式
        JSONObject json = compatibilityJsonCheck(param);
        //这个地方的参数与传递的入参一致，否则会报错
        String ruleKey = json.getString("key");
        //找到对应的逻辑信息
        //查看这个逻辑执行器是否执行
        Date startTime = json.getDate("startTime");
        //如果小余等于当前时间，则直接返回结果。
        if (startTime.getTime() >= System.currentTimeMillis()) {
            XxlJobHelper.log("当前时间未操作开始时间，等待下一次执行");
        }
        //需要清空租户
        TenantContextHolder.clear();
        CurrentAppUtils.clear();
        //逻辑如果需要传递参数，直接查询数据库修改Data即可
        RuleDesignPo po = ruleService.getEnableDesign(ruleKey);
        JvsAppVersion app = jvsAppVersionService.getUseAppVersion(po.getJvsAppId());
        ModeUtils.setSwitchModel(new SwitchModeDto().setMode(app.getVersionType()));
        if (ObjectNull.isNull(po)) {
            XxlJobHelper.log("查询逻辑数据为空:{}", ruleKey);
            return;
        }
        //设置逻辑的租户id保证下级使用同级租户执行
        TenantContextHolder.setTenantId(po.getTenantId());
        XxlJobHelper.log("查询数据为:{}", JSONObject.toJSONString(po));
        RunLogPo logPo = runLogService.create(po.getJvsAppId(), ruleKey, RunType.JOB, json, po.getReqType(), po.getReqType(), false);
        OssUtils.setOssTemplateBusinessId(OssConstantType.OSS_RULE_JOB, po.getJvsAppId());

        RuleExecuteDto data = new RuleExecuteDto().setReqVariableMap(json).setVariableMap(json);
        RuleExecDto ruleExecDto = new RuleExecDto()
                .setExecuteDto(data)
                .setType(RunType.JOB)
                .setSecret(ruleKey)
                .setGraph(JSONObject.parseObject(po.getDesignDrawingJson(), HtmlGraph.class));
        try {
            ruleStartUtils.start(po, logPo, ruleExecDto);
        } catch (Exception e) {
            String s = StackTraceElementUtils.logThrowableToString(e, "\n");
            XxlJobHelper.log("执行定时逻辑失败", s);
        } finally {
            //需要清空上下文
            SystemThreadLocal.clear();
        }
        //将执行日志，打印到定时日志中,也可不打印，以逻辑为主
        String logs = logPo.getLogs();
        XxlJobHelper.log(logs);
        XxlJobHelper.log("执行后最后的结果为:{}", JSONObject.toJSONString(data));
    }

    /**
     * 参数兼容json格式
     *
     * @param param
     * @return
     */
    private JSONObject compatibilityJsonCheck(String param) {
        XxlJobHelper.log("调用逻辑的数据为{}", param);
        if (ObjectNull.isNull(param)) {
            XxlJobHelper.log("数据传递参数为空,{},执行执行错误", param);
            throw new BusinessException("执行错误");
        }
        try {
            //做json格式兼容
            JSONObject jsonObject = JSONObject.parseObject(param);
            XxlJobHelper.log("数据格式为json格式,兼容检测成功,调用参数为,{}", jsonObject.toString());
            return jsonObject;
        } catch (Exception e) {
            XxlJobHelper.log("定时任务请求参数异常 不是json ，请以 【{\"key\":\"xxx\",\"startTime\":\"xxxx\",\"xxx\":\"xxxx\"}】 ，值为,{}", param);
            throw new BusinessException("逻辑定时任务参数不正确请使用Json格式");
        }
    }
}
