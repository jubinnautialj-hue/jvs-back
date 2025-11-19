package cn.bctools.design.rule.impl.datamodel.count;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.dto.QueryConditionDto;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.permission.ResourcePermissionHandler;
import cn.bctools.design.permission.service.DesignPermissionService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Order(1)
@Service
@AllArgsConstructor
@Rule(
        value = "统计条数",
        group = RuleGroup.模型插件,
        test = true,
        order = 9,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数字,
//        iconUrl = "rule-cishupanduan",
        explain = "跳过数据权限，根据查询条件统计指定模型条数"
)
public class DataModelCountServiceImpl implements BaseCustomFunctionInterface<DataModelCountDto> {

    DynamicDataService dynamicDataService;
    DesignPermissionService designPermissionService;

    @Override
    @SneakyThrows
    public Object execute(DataModelCountDto dataModelDto, Map<String, Object> params) {
        String dataModelId = dataModelDto.getDataModelId();
        //判断请求入口是否是模型入口
        if (ResourcePermissionHandler.matcher()) {
            //如果是那这里需要根据设计 id重新获取数据权限
            designPermissionService.handleDesignDataScope(dataModelId);
        } else {
            DynamicDataUtils.freePermit();
        }
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id");
        List<QueryConditionDto> queryConditions = dataModelDto.getBody();
        Criteria criteria = DynamicDataUtils.buildDynamicCriteria(queryConditions);
        //排除逻辑删除数据
        criteria = DynamicDataUtils.initCriteria(criteria);
        return dynamicDataService.queryList(dataModelId, criteria, fieldList).size();

    }

    @Override
    public void inspect(DataModelCountDto o) {
        if (ObjectNull.isNull(o.getBody())) {
            throw new BusinessException("查询条件不能为空");
        }
        //并校验条件是否为空，如果为空，则查询条件不满足返回异常
        o.getBody().forEach(e -> {
            if (ObjectNull.isNull(e.getValue())) {
                throw new BusinessException(e.getFieldKey() + "查询条件为空");
            }
        });
    }

    @Override
    public void removeKey(Map<String, Object> body) {
        //不做清空操作。直接
    }
}
