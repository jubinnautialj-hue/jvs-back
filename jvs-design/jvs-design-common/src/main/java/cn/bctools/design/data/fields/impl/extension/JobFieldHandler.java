package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.auth.api.api.AuthJobServiceApi;
import cn.bctools.auth.api.dto.SysJobDto;
import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
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
 * 表单字段: 岗位选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@DesignField(value = "岗位选择", type = DataFieldType.job)
@AllArgsConstructor
public class JobFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<MultipleHtml>, ISelectorDataHandler {

    AuthJobServiceApi jobApi;

    @Override
    public Object getEchoValue(MultipleHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        boolean isMulti = fieldDto.getMultiple();
        //获取所有岗位
        Map<String, Object> jobMap = jobApi.getAll().getData().stream().collect(Collectors.toMap(SysJobDto::getId, SysJobDto::getName));
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);

        return dataFieldHandler.joinFormItems(jobMap, data, isMulti, false);
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        Map<String, String> jobMap = jobApi.getAll().getData().stream().collect(Collectors.toMap(SysJobDto::getName, SysJobDto::getId));
        Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
        orDefault.putAll(jobMap);
        if (dto.getMultiple()) {
            //判断角色是否存在如果不存在不允许导入
            String collect = Arrays.stream(o.toString().split(",")).filter(e -> !jobMap.containsKey(e.trim())).collect(Collectors.joining(","));
            if (collect.length() > 0) {
                throw new BusinessException(collect + "岗位不存在，导入失败");
            }
        } else {
            if (!jobMap.containsKey(o.toString().trim())) {
                throw new BusinessException(o.toString() + "岗位不存在，导入失败");
            }
        }
        cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
        return null;
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        List<String> collect = Arrays.stream(o.toString().split(REGEX)).map(String::trim).collect(Collectors.toList());
        return jobApi.getByNames(collect).getData().stream().map(SysJobDto::getId).collect(Collectors.toList());
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"job\",\n" +
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
                "            \"message\": \"请选择职位\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"name\": \"" + DataFieldType.job.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
