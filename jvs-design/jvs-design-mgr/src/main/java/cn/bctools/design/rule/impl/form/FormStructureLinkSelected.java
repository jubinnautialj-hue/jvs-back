package cn.bctools.design.rule.impl.form;

import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Form structure link selected.
 */
@Slf4j
@Service
@AllArgsConstructor
public class FormStructureLinkSelected implements LinkFieldSelected<String> {

    /**
     * The Field service.
     */
    DataFieldService fieldService;
    /**
     * The Form service.
     */
    FormService formService;

    @Override
    public Object link(String id, String body) {
        //查询表单，获取表单的结构
        FormPo formPo = formService.getById(id);
        return fieldService.getAllField(formPo.getJvsAppId(), formPo.getDataModelId(), true, true, e -> false)
                .stream()
                .filter(e -> !e.getFieldType().equals(DataFieldType.p))
                .filter(e -> !e.getFieldType().equals(DataFieldType.pageTable))
                .map(e -> new ParameterOption<String>().setLabel(e.getFieldName() + "_" + e.getFieldKey()).setValue(e.getFieldKey())).collect(Collectors.toList());
    }

    @Override
    public List<String> fields() {
        ArrayList<String> objects = new ArrayList<>();
        objects.add("body");
        return objects;
    }

}
