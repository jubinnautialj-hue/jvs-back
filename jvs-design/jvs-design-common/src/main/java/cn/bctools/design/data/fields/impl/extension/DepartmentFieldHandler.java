package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
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
        long startTime = System.currentTimeMillis();
        
        boolean isMulti = !ObjectNull.isNull(fieldDto.getMultiple()) && fieldDto.getMultiple();
        boolean showPath = !ObjectNull.isNull(fieldDto.getShowalllevels()) && fieldDto.getShowalllevels();
        String b = SystemThreadLocal.get("functionName");
        if (ObjectNull.isNotNull(b)) {
            if (b.contains("-导出-") || b.contains("-导入-")) {
                showPath = true;
            }
        }

        // 优先从lineData中获取预加载的部门缓存（跨线程访问，解决并行处理问题）
        Map<String, Object> deptNameMap = null;
        List<SysDeptDto> deptList = null;
        
        if (ObjectNull.isNotNull(lineData)) {
            // 尝试从lineData中获取预加载的缓存
            Object deptNameMapCache = lineData.get("__DEPT_NAME_MAP_CACHE__");
            Object deptListCache = lineData.get("__DEPT_LIST_CACHE__");
            
            if (deptNameMapCache instanceof Map) {
                deptNameMap = (Map<String, Object>) deptNameMapCache;
                log.debug("[部门选择-Echo] 使用预加载的部门名称Map缓存，部门数量: {}", deptNameMap.size());
            }
            
            if (deptListCache instanceof List) {
                deptList = (List<SysDeptDto>) deptListCache;
            }
        }
        
        // 降级处理：如果lineData中没有缓存，尝试从ThreadLocal获取
        if (ObjectNull.isNull(deptNameMap)) {
            deptNameMap = SystemThreadLocal.get("DEPT_NAME_MAP_CACHE");
            if (ObjectNull.isNotNull(deptNameMap)) {
                log.debug("[部门选择-Echo] 使用ThreadLocal中的部门名称Map缓存");
            }
        }
        
        if (ObjectNull.isNull(deptList)) {
            deptList = SystemThreadLocal.get(DataFieldType.department.getDesc());
        }
        
        // 最终降级：调用远程API
        if (ObjectNull.isNull(deptNameMap)) {
            long apiStart = System.currentTimeMillis();
            deptList = deptApi.getAll().getData();
            long apiDuration = System.currentTimeMillis() - apiStart;
            
            if (apiDuration > 50) {
                log.warn("[部门选择-Echo] 未找到预加载缓存，调用远程API耗时: {}ms，建议优化为批量预加载", apiDuration);
            }
            
            if (ObjectNull.isNotNull(deptList)) {
                // 构建部门名称Map
                long mapBuildStart = System.currentTimeMillis();
                deptNameMap = deptList.stream()
                    .collect(Collectors.toMap(SysDeptDto::getId, SysDeptDto::getName, (v1, v2) -> v1));
                long mapBuildDuration = System.currentTimeMillis() - mapBuildStart;
                
                if (mapBuildDuration > 10) {
                    log.warn("[部门选择-Echo] 构建部门名称Map耗时: {}ms，部门数量: {}", mapBuildDuration, deptList.size());
                }
            }
        }
        
        // 如果需要路径处理但deptList为空，需要获取完整的部门列表
        if (showPath && ObjectNull.isNull(deptList)) {
            deptList = SystemThreadLocal.get(DataFieldType.department.getDesc());
            if (ObjectNull.isNull(deptList)) {
                deptList = deptApi.getAll().getData();
            }
        }
        
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);
        Object result;

        if (data instanceof List) {
            boolean finalShowPath = showPath;
            List<SysDeptDto> finalDeptList = deptList;
            Map<String, Object> finalDeptNameMap = deptNameMap;
            
            result = ((List<?>) data).stream().map(e -> {
                Object processedData = e;
                if (finalShowPath && ObjectNull.isNotNull(finalDeptList)) {
                    processedData = dataFieldHandler.handlePathId(e, isMulti, finalShowPath, finalDeptList, 
                        SysDeptDto::getId, SysDeptDto::getParentId);
                }
                return dataFieldHandler.joinFormItems(finalDeptNameMap, processedData, isMulti, finalShowPath);
            }).collect(Collectors.joining(","));
        } else {
            if (showPath && ObjectNull.isNotNull(deptList)) {
                data = dataFieldHandler.handlePathId(data, isMulti, showPath, deptList, 
                    SysDeptDto::getId, SysDeptDto::getParentId);
            }
            result = dataFieldHandler.joinFormItems(deptNameMap, data, isMulti, showPath);
        }
        
        long totalDuration = System.currentTimeMillis() - startTime;
        if (totalDuration > 100) {
            log.warn("[部门选择-Echo] 单次处理耗时过长: {}ms，数据: {}, 是否多选: {}, 是否显示路径: {}", 
                totalDuration, data, isMulti, showPath);
        }
        
        return result;
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        if (dto.getMultiple() && (o.toString().contains(",") || o.toString().contains("，"))) {
            String v = o.toString().replace("，", ",");
            Arrays.stream(v.toString().split(",")).forEach(e -> getConversionKey(dto, e.toString().trim(), lineData, cascaderFieldPathIdsMap, generateCascaderList));
            return null;
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
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        if (ObjectNull.isNull(o)) {
            return o;
        }
        if (o.toString().contains("/")) {
            List<String> collect = Arrays.stream(o.toString().split("/")).map(String::trim).collect(Collectors.toList());
            List<SysDeptDto> data = deptApi.getAllTree().getData();
            log.info("获取部门数据" + JSONObject.toJSONString(data));
            //遍历数据,找出名称与这个字段相同的数据
            Object next = getNextDeptId(data, collect, 0);
            return next;
        } else {
            List<SysDeptDto> data = deptApi.search(new SysDeptDto().setName(o.toString())).getData();
            if (ObjectNull.isNotNull(data)) {
                String id = deptApi.search(new SysDeptDto().setName(o.toString())).getData().get(0).getId();
                return id;
            } else {
                throw new BusinessException(o.toString() + " 部门不存在");
            }
        }
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
        String str = "{\n" + "    \"prop\": \"" + field + "\",\n" + "    \"type\": \"department\",\n" + "    \"label\": \"" + name + "\",\n" + "    \"span\": 24,\n" + "    \"display\": true,\n" + "    \"status\": \"\",\n" + "    \"tips\": {\n" + "        \"text\": \"\",\n" + "        \"position\": \"right\"\n" + "    },\n" + "    \"multiple\": false,\n" + "    \"showalllevels\": true,\n" + "    \"collapsetags\": false,\n" + "    \"emitPath\": false,\n" + "    \"showFrom\": [\n" + "        \"label\",\n" + "        \"span\",\n" + "        \"multiple\",\n" + "        \"prop\",\n" + "        \"sqlType\",\n" + "        \"showalllevels\",\n" + "        \"collapsetags\",\n" + "        \"emitPath\",\n" + "        \"disabled\",\n" + "        \"isDefault\"\n" + "    ],\n" + "    \"sqlType\": \"array\",\n" + "    \"rules\": [\n" + "        {\n" + "            \"required\": false,\n" + "            \"message\": \"请选择部门\",\n" + "            \"trigger\": \"change\"\n" + "        }\n" + "    ],\n" + "    \"name\": \"" + DataFieldType.department.getDesc() + "\",\n" + "    \"disabled\": false\n" + "}";
        return JSONObject.parseObject(str);
    }

}
