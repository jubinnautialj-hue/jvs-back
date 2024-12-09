package cn.bctools.design.rule.impl.datamodel;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取数据源集
 *
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataModelSelected implements ParameterSelected<String> {

    DataModelService modelService;

    @Override
    public String key() {
        return "jvsAppId";
    }

    @Override
    public List<ParameterOption<String>> getOptions() {

        //应用ID 必须要从前端 获取 如果获取 不到，即返回错误不可使用
        String appId = SystemThreadLocal.get(key());
        if (ObjectNull.isNull(appId)) {
            throw new BusinessException("请先创建模型");
        }
        // 查询模型数据
        return modelService.list(Wrappers.<DataModelPo>lambdaQuery()
                        .select(DataModelPo::getId, DataModelPo::getAppId, DataModelPo::getName)
                        .eq(StringUtils.isNotBlank(appId), DataModelPo::getAppId, appId))
                .stream()
                .map(e -> new ParameterOption<String>().setLabel(e.getName()).setValue(e.getId())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }


}
