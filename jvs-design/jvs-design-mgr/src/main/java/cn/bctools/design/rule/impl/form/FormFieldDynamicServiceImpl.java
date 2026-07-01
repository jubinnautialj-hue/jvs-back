package cn.bctools.design.rule.impl.form;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataFieldDynamicPo;
import cn.bctools.design.data.service.DataFieldDynamicService;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zjvs
 */
@Service
@AllArgsConstructor
@Rule(value = "添加修改表单组件",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 5,
//        iconUrl = "rule-slsrizhifuwu",
        explain = "通过传递参数确定是否需要动态添加表单或列表的设计字段，添加的同时自动添加模型的字段,如果存在相同的字段组件类型变化后将自动修改。"
)
public class FormFieldDynamicServiceImpl implements BaseCustomFunctionInterface<FormFieldDynamicDto> {

    FormService formService;
    CrudPageService crudPageService;
    DataFieldDynamicService dataFieldDynamicService;

    @Override
    public Object execute(FormFieldDynamicDto dto, Map<String, Object> params) {
        if (ObjectNull.isNull(dto.getList())) {
            return "";
        }
        //转换
        if (ObjectNull.isNotNull(dto.getList())) {
            List<DataFieldDynamicPo> dynamicPos = dto.getList().stream()
                    .map(e -> BeanCopyUtil.copy(e, DataFieldDynamicPo.class).setFieldJson(e)).collect(Collectors.toList());
            Map<String, String> model = new HashMap<>(8);
            //添加表单字段
            {
                Map<String, List<String>> collect = formService.listByIds(dto.getDesignId())
                        .stream()
                        .peek(a -> {
                            model.put(a.getDataModelId(), a.getJvsAppId());
                        })
                        .collect(Collectors.groupingBy(FormPo::getDataModelId, Collectors.mapping(FormPo::getId, Collectors.toList())));

                if (ObjectNull.isNotNull(collect)) {
                    collect.keySet().forEach(a -> {
                        dataFieldDynamicService.saveOrUpdateFields(model.get(a), dynamicPos, a, collect.get(a));
                    });
                }
            }
            //添加列表页字段
            {
                Map<String, List<String>> collect = crudPageService.listByIds(dto.getDesignId())
                        .stream()
                        .peek(a -> {
                            model.put(a.getDataModelId(), a.getJvsAppId());
                        })
                        .collect(Collectors.groupingBy(CrudPage::getDataModelId, Collectors.mapping(CrudPage::getId, Collectors.toList())));

                if (ObjectNull.isNotNull(collect)) {
                    collect.keySet().forEach(a -> {
                        dataFieldDynamicService.saveOrUpdateFields(model.get(a), dynamicPos, a, collect.get(a));
                    });
                }
            }
        }
        return true;
    }

}
