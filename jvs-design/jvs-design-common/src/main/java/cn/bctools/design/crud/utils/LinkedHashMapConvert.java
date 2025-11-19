package cn.bctools.design.crud.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;


/**
 * Excel类型转换: LinkedHashMap
 *
 * @Author: GuoZi
 */
@Component
public class LinkedHashMapConvert implements Converter<LinkedHashMap> {

    @Override
    public Class supportJavaTypeKey() {
        return LinkedHashMap.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public LinkedHashMap convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return JSONObject.parseObject(cellData.toString(), LinkedHashMap.class);
    }

    @Override
    public CellData convertToExcelData(LinkedHashMap value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return new CellData<>(JSONUtil.toJsonStr(value));
    }

}
