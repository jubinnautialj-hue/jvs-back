package cn.bctools.rule.tools.json;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author st
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "json工具",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.未识别,
        testShowEnum = TestShowEnum.JSON,
        order = 22,
//        iconUrl = "rule-youjian",
        explain = "根据填写或传入的JSON数据定义节点对象。"
)
public class JsonServiceImpl implements BaseCustomFunctionInterface<JsonDto> {

    @Override
    public Object execute(JsonDto jsonDto, Map<String, Object> params) {
        if (JSONUtil.isTypeJSONArray(jsonDto.getContent())) {
            return JSONArray.parseArray(jsonDto.getContent());
        } else if (JSONUtil.isTypeJSONObject(jsonDto.getContent())) {
            return JSONObject.parseObject(jsonDto.getContent());
        } else {
            throw new BusinessException("不支持的操作类型");
        }
    }

    @Override
    public void inspect(JsonDto o) {
        if (!JSONUtil.isTypeJSONObject(o.getContent())) {
            throw new BusinessException("不支持的操作类型");
        }
    }
}
