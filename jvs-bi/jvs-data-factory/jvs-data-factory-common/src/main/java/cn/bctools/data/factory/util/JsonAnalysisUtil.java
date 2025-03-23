package cn.bctools.data.factory.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.enums.ApiDataFieldTypeEnum;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;

import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取jsonKey注意 包含子集key
 */
public class JsonAnalysisUtil {

    public static List<JsonAnalysisPo> analysis(String json) throws BusinessException {
        if (StrUtil.isBlank(json)) {
            throw new BusinessException("内容不能为空");
        }
        JSONValidator from = JSONValidator.from(json);
        if (!from.validate() || from.getType().equals(JSONValidator.Type.Value)) {
            throw new BusinessException("json格式不正确");
        }
        //如果第一层为数组并且只有单个值是不支持的 例如 [2,3,4,5,6,7,8]
        if (from.getType().equals(JSONValidator.Type.Array)) {
            JSONArray jsonArray = JSONObject.parseArray(json);
            if (jsonArray.isEmpty()) {
                throw new BusinessException("json不能为空");
            }
            if (JSONValidator.from(JSONObject.toJSONString(jsonArray.get(0))).getType().equals(JSONValidator.Type.Value)) {
                throw new BusinessException("暂不支持此种数据格式");
            }
        }
        if (from.getType().equals(JSONValidator.Type.Object)) {
            if (JSONObject.parseObject(json).isEmpty()) {
                throw new BusinessException("json不能为空");
            }
        }
        return getStructure(json, "");
    }

    /**
     * 递归解析表格
     *
     * @param json 需要解析的数据
     * @param path 上级
     */
    private static List<JsonAnalysisPo> getStructure(String json, String path) {
        JSONValidator from = JSONValidator.from(json);
        JSONValidator.Type type = from.getType();
        if (type.equals(JSONValidator.Type.Object)) {
            JSONObject jsonObject = JSONObject.parseObject(json);
            return jsonObject.keySet().stream().map(e -> {
                JsonAnalysisPo structure = new JsonAnalysisPo();
                String o = jsonObject.getString(e);

                ApiDataFieldTypeEnum dataFieldTypeEnum = ApiDataFieldTypeEnum.字符串;
                if (StrUtil.isNotBlank(o)) {
                    JSONValidator from1 = JSONValidator.from(o);
                    if (JSONUtil.isTypeJSON(o)) {
                        switch (from1.getType()) {
                            case Array:
                                dataFieldTypeEnum = ApiDataFieldTypeEnum.集合类型;
                                break;
                            case Object:
                                dataFieldTypeEnum = ApiDataFieldTypeEnum.对象;
                                break;
                            default:
                                dataFieldTypeEnum = getType(jsonObject.get(e));
                        }
                    } else {
                        dataFieldTypeEnum = getType(jsonObject.get(e));
                    }
                }

                if (dataFieldTypeEnum.equals(ApiDataFieldTypeEnum.集合类型) || dataFieldTypeEnum.equals(ApiDataFieldTypeEnum.对象)) {
                    List<JsonAnalysisPo> list = getStructure(o, StrUtil.isNotBlank(path) ? path + "." + e : e);
                    structure.setItems(list);
                } else {
                    structure.setValues(Arrays.asList(o));
                }
                structure.setApiDataFieldTypeEnum(dataFieldTypeEnum)
                        .setColumnCount(e)
                        .setJsonPath(path)
                        .setColumnName(e);
                return structure;
            }).collect(Collectors.toList());
        } else {
            JSONArray objects = JSONObject.parseArray(json);
            if (objects.isEmpty()) {
                return new ArrayList<>();
            }
            Object object = objects.get(0);
            String s = JSONObject.toJSONString(object);
            JSONValidator validator = JSONValidator.from(s);
            if (!validator.validate() || validator.getType().equals(JSONValidator.Type.Value)) {
                return new ArrayList<>();
            }
            List<JsonAnalysisPo> structure = getStructure(s, path);
            //设置值
            structure.stream().peek(e -> {
                List<Object> list = objects.stream().map(v -> JSONObject.parseObject(JSONObject.toJSONString(v)).get(e.getColumnName())).collect(Collectors.toList());
                e.setValues(list);
            }).collect(Collectors.toList());
            return structure;
        }
    }

    private static ApiDataFieldTypeEnum getType(Object value) {
        if (ObjectUtil.isNull(value)) {
            return ApiDataFieldTypeEnum.未识别;
        }
        if (value instanceof String) {
            return ApiDataFieldTypeEnum.字符串;
        }
        if (value instanceof List) {
            return ApiDataFieldTypeEnum.集合类型;
        }
        if (value instanceof Date || value instanceof TemporalAccessor) {
            return ApiDataFieldTypeEnum.时间;
        }
        if (value instanceof Boolean) {
            return ApiDataFieldTypeEnum.布尔;
        }
        if (value instanceof Map) {
            return ApiDataFieldTypeEnum.对象;
        }
        String s = value.toString();
        if (NumberUtil.isNumber(s)) {
            return ApiDataFieldTypeEnum.数字;
        }
        return ApiDataFieldTypeEnum.未识别;
    }

}
