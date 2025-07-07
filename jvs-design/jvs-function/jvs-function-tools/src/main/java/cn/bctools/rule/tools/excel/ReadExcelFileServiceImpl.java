package cn.bctools.rule.tools.excel;


import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.word.utils.ExcelVariablesReplaceUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "读取Excel",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 15,
//        iconUrl = "rule-excel",
        explain = "根据单元格解析Excel"
)
public class ReadExcelFileServiceImpl implements BaseCustomFunctionInterface<ReadExcelFileDto> {

    @Override
    public Object execute(ReadExcelFileDto dto, Map<String, Object> params) {
        Map<Object, Map<String, Object>> map = new HashMap<>();
        map.put(dto.getSheet(), dto.getCell());
        if (ObjectNull.isNull(dto.getIgnoreFormat())) {
            dto.setIgnoreFormat(false);
        }
        List<Dict> dicts = ExcelVariablesReplaceUtil.searchData(new ByteArrayInputStream(HttpUtil.downloadBytes(dto.getFileUrl())), map, dto.getIgnoreFormat());
        Object o = dicts.get(0).get(dto.getSheet());
        return o;
    }
}
