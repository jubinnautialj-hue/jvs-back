package cn.bctools.design.crud.utils;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/**
 * Excel类型转换: Decimal128
 *
 * @Author: GuoZi
 */
@Component
public class Decimal128ListConvert implements Converter<Decimal128> {

    @Override
    public Class supportJavaTypeKey() {
        return Decimal128.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Decimal128 convertToJavaData(CellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return Decimal128.parse(cellData.toString());
    }

    @Override
    public CellData convertToExcelData(Decimal128 value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        CellData<Double> doubleCellData = new CellData<>();
        doubleCellData.setNumberValue(new BigDecimal(value.doubleValue()));
        doubleCellData.setType(CellDataTypeEnum.NUMBER);
        return doubleCellData;
    }

}
