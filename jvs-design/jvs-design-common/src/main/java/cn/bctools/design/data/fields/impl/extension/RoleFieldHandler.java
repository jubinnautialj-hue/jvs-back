package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
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
 * 表单字段: 角色选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@DesignField(value = "角色选择", type = DataFieldType.role)
@AllArgsConstructor
public class RoleFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<MultipleHtml>, ISelectorDataHandler {

    AuthRoleServiceApi roleApi;

    @Override
    public Object getEchoValue(MultipleHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        boolean isMulti = fieldDto.getMultiple();
        Map<String, Object> roleMap = roleApi.getAll().getData().stream().collect(Collectors.toMap(SysRoleDto::getId, SysRoleDto::getRoleName));
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);
        return dataFieldHandler.joinFormItems(roleMap, data, isMulti, false);
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        Map<String, String> roleMap = roleApi.getAll().getData().stream().collect(Collectors.toMap(SysRoleDto::getRoleName, SysRoleDto::getId));
        Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
        orDefault.putAll(roleMap);
        if (dto.getMultiple()) {
            //判断角色是否存在如果不存在不允许导入
            String collect = Arrays.stream(o.toString().split(",")).filter(e -> !roleMap.containsKey(e.trim())).collect(Collectors.joining(","));
            if (collect.length() > 0) {
                throw new BusinessException(collect + "角色不存在，导入失败");
            }
        } else {
            if (!roleMap.containsKey(o.toString().trim())) {
                throw new BusinessException(o.toString() + "角色不存在，导入失败");
            }
        }
        cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
        return null;
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        if (ObjectNull.isNull(o)) {
            return o;
        }
        Map<String, String> roleMap = roleApi.getAll().getData().stream().collect(Collectors.toMap(SysRoleDto::getRoleName, SysRoleDto::getId));
        return Arrays.stream(o.toString().split(REGEX))
                .map(String::trim)
                .map(roleMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"role\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"multiple\": true,\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"multiple\",\n" +
                "        \"prop\",\n" +
                "        \"sqlType\",\n" +
                "        \"disabled\"\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\":  \"请选择" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"name\":  \"" + DataFieldType.role.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
