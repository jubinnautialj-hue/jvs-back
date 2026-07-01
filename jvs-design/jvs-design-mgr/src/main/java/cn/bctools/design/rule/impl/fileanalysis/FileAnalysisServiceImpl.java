package cn.bctools.design.rule.impl.fileanalysis;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.service.DynamicDataService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.function.handler.ExpressionHandler;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.RuleSystemThreadLocal;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Service
@AllArgsConstructor
@Rule(value = "文件解析",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 4,
//        iconUrl = "rule-slsrizhifuwu",
        explain = "支持Excel和txt文件内容解析,不支持公式和函数,表头合并."
)
public class FileAnalysisServiceImpl implements BaseCustomFunctionInterface<FileAnalysisDto> {

    DataModelService dataModelService;
    DynamicDataService dynamicDataService;
    RuleDesignService ruleDesignService;
    DataFieldService dataFieldService;
    Map<String, IDataFieldHandler> iDataFieldHandler;


    @SneakyThrows
    @Override
    public Object execute(FileAnalysisDto dto, Map<String, Object> params) {
        switch (dto.getFileType()) {
            case xlsx:
                byte[] bytes = HttpUtil.downloadBytes(dto.getFileUrl());
                ByteArrayInputStream bookStream = new ByteArrayInputStream(bytes);
                ExcelReader reader = ExcelUtil.getReader(bookStream);
                DataModelPo dataModelPo;
                List<FieldBasicsHtml> fields = null;
                if (ObjectNull.isNull(dto.getModelName())) {
                    reader.setHeaderAlias(dto.getHeaderAlias());
                } else {
                    RuleExecDto rule = RuleSystemThreadLocal.getRule();
                    if (ObjectNull.isNull(rule)) {
                        throw new BusinessException("excel解析不支持测试");
                    }
                    RuleDesignPo one = ruleDesignService.getOne(new LambdaQueryWrapper<RuleDesignPo>().select(RuleDesignPo::getJvsAppId).eq(RuleDesignPo::getSecret, rule.getSecret()));
                    //使用模型的数据字段,并进行数据转换
                    dataModelPo = dataModelService.getOne(Wrappers.query(new DataModelPo().setId(dto.getModelName()).setAppId(one.getJvsAppId())));
                    if (ObjectNull.isNull(dataModelPo)) {
                        throw new BusinessException("数据集名称不存在");
                    }
                    //根据模型查询名称
                    fields = dataFieldService.getFields(one.getJvsAppId(), dataModelPo.getId(), true, true);
                    Map<String, String> allField = new HashMap<>(fields.size());
                    fields.forEach(e -> allField.put(e.getFieldName(), e.getFieldKey()));
                    reader.setHeaderAlias(allField);
                }
                List<Map<String, Object>> maps = reader.readAll();
                //删除多余行号
                if (dto.getI() > 2) {
                    for (int i = 2; i < dto.getI(); i++) {
                        maps.remove(0);
                    }
                }
                //判断是否做模型转换
                if (ObjectNull.isNotNull(dto.getModelName())) {
                    assert fields != null;
                    Map<String, FieldBasicsHtml> typeMaps = fields.stream().filter(e -> DataFieldType.SELECT_CONVERSION.contains(e.getType())).collect(Collectors.toMap(FieldPublicHtml::getFieldKey, Function.identity()));
                    if (ObjectNull.isNotNull(typeMaps)) {
                        //遍历数据转换
                        for (Map<String, Object> map : maps) {
                            Map<Object, Object> lineData = new HashMap<>(map);
                            for (String key : typeMaps.keySet()) {
                                FieldBasicsHtml fieldBasicsHtml = typeMaps.get(key);
                                IDataFieldHandler handler = iDataFieldHandler.get(fieldBasicsHtml.getType().getDesc());
                                Object o = map.get(key);
                                if (ObjectNull.isNotNull(o)) {
                                    Object obj = handler.getConversionKey(handler.toHtml(fieldBasicsHtml), o, lineData);
                                    map.put(key, obj);
                                }
                            }
                        }
                    }
                }
                Map<String, Object> list = maps.get(0);
                List<String> collect = list.keySet().stream().filter(e -> ExpressionHandler.patten.matcher(e).find()).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    throw new BusinessException("中文字段请填写转换规则需要转换的字段有", JSON.toJSONString(collect));
                }
                //转换
                reader.close();
                return maps;
            default:
                List<String> strings = FileUtil.readLines(new URL(dto.getFileUrl()), Charset.defaultCharset());
                //删除多余行号
                if (dto.getI() > 2) {
                    for (int i = 0; i < dto.getI() - 2; i++) {
                        strings.remove(i);
                    }
                }
                return strings;
        }
    }
}
