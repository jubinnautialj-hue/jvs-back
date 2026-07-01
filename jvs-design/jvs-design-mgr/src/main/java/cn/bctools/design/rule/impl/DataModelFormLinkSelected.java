package cn.bctools.design.rule.impl;

import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 关联查询模型关联的所有表单
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataModelFormLinkSelected implements LinkFieldSelected<String> {

    FormService formService;

    @Override
    public Object link(String id, String field) {
        List<FormPo> formPos = formService.list(Wrappers.<FormPo>lambdaQuery().select(FormPo::getName, FormPo::getId).eq(FormPo::getDataModelId, id));
        return formPos.stream()
                .map(form ->
                        new ParameterOption<String>()
                                .setLabel(form.getName())
                                .setValue(form.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> fields() {
        return Collections.singletonList("sendFormId");
    }
}
