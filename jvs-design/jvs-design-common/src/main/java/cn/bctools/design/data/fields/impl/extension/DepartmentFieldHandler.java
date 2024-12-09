package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.constant.CacheConsts;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.fields.impl.ISelectorDataHandler;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表单字段: 部门选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "部门选择", type = DataFieldType.department)
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class DepartmentFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<MultipleHtml>, ISelectorDataHandler {

    AuthDeptServiceApi deptApi;

    @Override
    public Object getEchoValue(MultipleHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        boolean isMulti = !ObjectNull.isNull(fieldDto.getMultiple()) && fieldDto.getMultiple();
        boolean showPath = !ObjectNull.isNull(fieldDto.getShowalllevels()) && fieldDto.getShowalllevels();
        List<SysDeptDto> deptList = SystemThreadLocal.get(DataFieldType.department.getDesc());
        if (ObjectNull.isNull(deptList)) {
            //获取所有的部门
            deptList = deptApi.getAll().getData();
            SystemThreadLocal.set(DataFieldType.department.getDesc(), deptList);
        }
        Map<String, Object> deptMap = deptList.stream().collect(Collectors.toMap(SysDeptDto::getId, SysDeptDto::getName));
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);

        data = dataFieldHandler.handlePathId(data, isMulti, showPath, deptList, SysDeptDto::getId, SysDeptDto::getParentId);
        return dataFieldHandler.joinFormItems(deptMap, data, isMulti, showPath);
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        if (dto.getMultiple() && o.toString().contains(",")) {
            Arrays.stream(o.toString().split(",")).forEach(e -> getConversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
        } else {
            Object obj = getConversionKey(dto, o, lineData);
            //如果转换成功，则将 id放进对象中
            Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
            if (ObjectNull.isNotNull(obj)) {
                orDefault.put(o.toString(), obj.toString());
                cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
                return obj.toString();
            } else {
                throw new BusinessException(o.toString().trim() + "部门不存在，不支持导入");
            }
        }
        return null;
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        if (ObjectNull.isNull(o)) {
            return o;
        }
        List<String> collect = Arrays.stream(o.toString().split("/")).map(String::trim).collect(Collectors.toList());
        List<SysDeptDto> data = deptApi.getAllTree().getData();
        //遍历数据,找出名称与这个字段相同的数据
        Object next = getNextDeptId(data, collect, 0);
        return next;
    }

    private Object getNextDeptId(List<SysDeptDto> sysDeptDto, List<String> collect, int i) {
        if (ObjectNull.isNotNull(sysDeptDto) && i < collect.size()) {
            String name = collect.get(i);
            for (SysDeptDto deptDto : sysDeptDto) {
                if (i + 1 == collect.size() && deptDto.getName().equals(name)) {
                    return deptDto.getId();
                }
                //如果名称相同找下级
                if (deptDto.getName().equals(name)) {
                    List<SysDeptDto> childList = deptDto.getChildList();
                    return getNextDeptId(childList, collect, ++i);
                }
            }
        }
        return null;
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"department\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"multiple\": false,\n" +
                "    \"showalllevels\": true,\n" +
                "    \"collapsetags\": false,\n" +
                "    \"emitPath\": false,\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"multiple\",\n" +
                "        \"prop\",\n" +
                "        \"sqlType\",\n" +
                "        \"showalllevels\",\n" +
                "        \"collapsetags\",\n" +
                "        \"emitPath\",\n" +
                "        \"disabled\",\n" +
                "        \"isDefault\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请选择部门\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.department.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }

}
