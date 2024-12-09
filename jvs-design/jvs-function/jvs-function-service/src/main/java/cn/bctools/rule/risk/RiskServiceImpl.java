package cn.bctools.rule.risk;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.risk.api.RiskApi;
import cn.bctools.risk.dto.RiskLogBatchDto;
import cn.bctools.risk.dto.RiskLogDto;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Rule(value = "规则引擎",
        group = RuleGroup.服务插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        customStructure = true,
        order = 1,
//        iconUrl = "rule-config",
        explain = "调用规则功能执行规则"
)
@AllArgsConstructor
public class RiskServiceImpl implements BaseCustomFunctionInterface<RiskDto> {

    RiskApi riskApi;

    @Override
    public Object execute(RiskDto riskDto, Map<String, Object> params) {
        List<Map<String, Object>> objects = new ArrayList<>();
        objects.add(riskDto.getBody());
        RiskLogDto riskLogDto = null;
        try {
            RiskLogBatchDto riskLogDtos = riskApi.projectFlow(riskDto.getRiskName(), "业务逻辑执行", objects, SpringContextUtil.getApplicationContextName()).getData();
            riskLogDto = riskLogDtos.getList().get(0);
        } catch (Exception e) {
            log.error("规则引擎不可用");
        }
        return JSONObject.parseObject(JSONObject.toJSONString(riskLogDto));
    }

}
