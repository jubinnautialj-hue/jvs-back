//package cn.bctools.rule.datafactory;
//
//import cn.bctools.common.exception.BusinessException;
//import cn.bctools.common.utils.BeanToMapUtils;
//import cn.bctools.common.utils.ObjectNull;
////import cn.bctools.data.factory.api.DataFactoryApi;
//import cn.bctools.rule.common.LinkFieldSelected;
//import cn.bctools.rule.common.ParameterOption;
//import cn.hutool.core.lang.Dict;
//import com.alibaba.fastjson2.JSONObject;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class DataFactoryLinkSelected implements LinkFieldSelected<String> {
//
//    //DataFactoryApi dataFactoryApi;
//
//    @Override
//    public Object link(String id, String body) {
//        fields field = fields.valueOf(body);
//        switch (field) {
//            case body:
//                List<JSONObject> data1 = dataFactoryApi.getTable(id).getData();
//                if (ObjectNull.isNull(data1)) {
//                    throw new BusinessException("没有找到关联字段");
//                }
//                return data1
//                        .stream()
//                        .map(e -> {
//                            List<Dict> collect = Arrays.asList(Dict.create().set("label", "等于").set("value", "等于")).stream().collect(Collectors.toList());
//                            Map<String, Object> map = BeanToMapUtils.beanToMap(collect);
//                            map.put("enabledQueryTypes", collect);
//                            return new ParameterOption<String>().setExtend(map).setLabel(e.getString("fieldName")).setValue(e.getString("fieldKey"));
//                        })
//                        .collect(Collectors.toList());
//            case fields:
//                List<JSONObject> data = dataFactoryApi.getTable(id).getData();
//                if (ObjectNull.isNull(data)) {
//                    throw new BusinessException("没有找到关联字段");
//                }
//                return data
//                        .stream()
//                        .map(e ->
//                                new ParameterOption<String>().setLabel(e.getString("fieldName")).setValue(e.getString("fieldKey"))
//                        )
//                        .collect(Collectors.toList());
//
//        }
//        throw new BusinessException("没有找到关联字段");
//    }
//
//    @Override
//    public List<String> fields() {
//        return Arrays.stream(fields.values()).map(e -> e.name()).collect(Collectors.toList());
//    }
//
//    /**
//     * 关联选择项的字段，选择了数据模型后，将数据模型和字段进行返回给前端
//     */
//    enum fields {
//        query, body, fields, x, y, xAxis, yAxis, groupBy, value, groupBy2;
//    }
//}
