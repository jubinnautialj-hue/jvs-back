package cn.bctools.design.data.fields;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.fields.impl.container.TabFieldHandler;
import cn.bctools.design.data.fields.impl.container.TableFormFieldHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据回显类
 * <p>
 * TODO 查询性能待优化
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
public class DataFieldHandler {

    @Autowired
    Map<String, IDataFieldHandler> handlerMap;

    public static final String LINK_MULTI = "~";
    public static final String SEPARATOR_MULTI = ", ";
    public static final String SEPARATOR_PATH = "/";
    public static final String UNNAMED = "";


    /**
     * 数据回显
     *
     * @param fieldDto 字段设计数据
     * @param data     原数据
     * @param lineData 原始行级数据，用于逻辑的字典转换，如， 省市区县， 多个字段进行联合转换时
     * @return 回显数据
     */
    public Object echo(FieldBasicsHtml fieldDto, Object data, Map<String, Object> lineData, boolean override) {

        if (Objects.isNull(fieldDto) || Objects.isNull(data)) {
            return data;
        }
        DataFieldType fieldType = fieldDto.getType();
        if (Objects.isNull(fieldType)) {
            return data;
        }
        IDataFieldHandler handler = handlerMap.get(fieldType.getDesc());
        if (Objects.nonNull(handler)) {
            // 处理回显数据
            try {
                if (ObjectNull.isNotNull(fieldDto.getDesignJson())) {
                    FieldBasicsHtml publicHtml = handler.toHtml(fieldDto);
                    data = handler.getEcho(publicHtml, data, override, lineData);
                }
            } catch (Exception e) {
                log.warn("数据回显异常", e);
            }
        }
        return data;
    }

    public Object echo(FieldBasicsHtml fieldDto, Object data, Map<String, Object> lineData) {
        return echo(fieldDto, data, lineData, false);
    }


    /**
     * 获取支持的查询类型
     *
     * @param fieldDto  字段数据
     * @param fieldJson 字段设计数据
     * @return 回显数据
     */
    public List<DataQueryType> getEnabledQueryTypes(DataFieldType fieldType, Map<String, Object> fieldJson) {
        if (Objects.isNull(fieldType)) {
            return Collections.emptyList();
        }
        IDataFieldHandler handler = handlerMap.get(fieldType.getDesc());
        if (Objects.nonNull(handler)) {
            // 处理回显数据
            try {
                FieldBasicsHtml o = handler.toHtml(fieldJson);
                return handler.getEnabledQueryTypes(o);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("数据回显异常", e);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 通过套字段类型获取对应的数据回显类
     *
     * @param fieldType 字段类型
     * @return 模板数理类
     */
    public IDataFieldHandler getHandler(DataFieldType fieldType) {
        if (handlerMap.containsKey(fieldType)) {
            throw new BusinessException("设计类型不能为空");
        }
        return this.handlerMap.get(fieldType);
    }

    public Object joinFormItemsValue(Map<String, Object> map, Object data, boolean isMulti, boolean showPath) {
        if (data instanceof List && isMulti) {
            List<Object> list = (List<Object>) data;
            return list.stream().map(obj -> joinItemPath(map, obj, showPath)).collect(Collectors.toList());
        } else {
            return joinItemPath(map, data, showPath);
        }
    }

    /**
     * 拼接表单选项的显示值
     * <p>
     * 1. 单个数据: 单选
     * 2. 一维数组: 多选
     * 3. 二维数组: 显示路径
     *
     * @param map      键值对数据<数据值, 显示值>
     * @param data     原数据(可能为单个数据, 一维数组, 二维数组)
     * @param isMulti  是否多选(选项之间使用,分隔)
     * @param showPath 是否显示路径(路径使用/分隔)
     * @return 显示值
     */
    public String joinFormItems(Map<String, Object> map, Object data, boolean isMulti, boolean showPath) {
        List<Object> list;
        if (data instanceof List) {
            list = (List<Object>) data;
        } else {
            list = Collections.singletonList(data);
        }
        if (!isMulti && list.size() > 1) {
            // 字段配置为单选, 但是数据为多选, 则只保留第一个数据
            list = Collections.singletonList(list.get(0));
        }
        return list.stream().map(obj -> joinItemPath(map, obj, showPath)).collect(Collectors.joining(DataFieldHandler.SEPARATOR_MULTI));
    }

    /**
     * 拼接单个表单选项的路径显示值
     *
     * @param map      键值对数据<数据值, 显示值>
     * @param data     原数据(可能为单个数据, 一维数组)
     * @param showPath 是否显示路径(路径使用/分隔)
     * @return 显示值
     */
    private String joinItemPath(Map<String, Object> map, Object data, boolean showPath) {
        if (ObjectNull.isNull(data)) {
            return "";
        }
        List<String> result;
        if (data instanceof List && !showPath) {
            List list = (List) data;
            String lastId;
            if (ObjectUtils.isEmpty(list)) {
                lastId = UNNAMED;
            } else {
                lastId = list.get(list.size() - 1).toString();
            }
            data = Collections.singletonList(lastId);
        }
        if (showPath) {
            result = (List<String>) data;
        } else {
            result = Collections.singletonList(data.toString());
        }
        return result.stream().map(map::get)
                .map(name -> Objects.isNull(name) ? UNNAMED : name.toString())
                .collect(Collectors.joining(DataFieldHandler.SEPARATOR_PATH));
    }

    /**
     * 处理上下级的数据
     * <p>
     * 需要显示上下级路径时, 补全数据
     * 不需要显示上下级路径时, 只保留最下级的数据
     * 例:
     * 1. "3" -> ["1", "2", "3"]
     * 2. ["3", "6", "9"] -> [["1", "2", "3"], ["4", "5", "6"], ["7", "8", "9"]]
     *
     * @param data         原始数据(可能为单个数据, 一维数组)
     * @param isMulti      是否多选
     * @param showPath     是否显示路径
     * @param nodeList     节点数据集合
     * @param getKey       获取当前节点标识
     * @param getParentKey 获取父级节点标识
     * @return 处理之后的数据(二维数组)
     */
    public <T> Object handlePathId(Object data,
                                   boolean isMulti,
                                   boolean showPath,
                                   List<T> nodeList,
                                   @NotNull Function<T, String> getKey,
                                   @NotNull Function<T, String> getParentKey) {
        List<Object> result;
        if (data instanceof List) {
            result = (List<Object>) data;
        } else {
            result = new ArrayList<>(1);
            result.add(data);
        }
        if (!isMulti && result.size() > 1) {
            // 字段配置为单选, 但是数据为数组, 则只保留第一个数据
            Object firstOne = result.get(0);
            result.clear();
            result.add(firstOne);
        }
        for (int i = 0; i < result.size(); i++) {
            Object obj = result.get(i);
            boolean isList = (obj instanceof List);
            if (isList && !showPath) {
                // 旧数据存的是二维数组, 处理为一维数组, 只保留最后一个数据
                List<String> list = (List<String>) obj;
                result.set(i, list.get(list.size() - 1));
            }
            if (!isList && showPath) {
                // 显示路径
                String targetId = obj.toString();
                List<T> passingBy = TreeUtils.getSearchPath(nodeList, targetId, getKey, getParentKey);
                List<String> deptIdList = passingBy.stream().map(getKey).collect(Collectors.toList());
                result.set(i, deptIdList);
            }
        }
        return result;
    }

}
