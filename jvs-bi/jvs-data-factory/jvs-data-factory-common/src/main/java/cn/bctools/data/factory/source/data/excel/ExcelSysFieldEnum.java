package cn.bctools.data.factory.source.data.excel;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum  ExcelSysFieldEnum {

    唯一标识("jvs_excel_unique_key","varchar(%s)",DataFieldTypeEnum.VARCHAR,50,true),
    批次号("lot_no","varchar(%s)",DataFieldTypeEnum.VARCHAR,50,true),
    ;

    private final String fieldName;

    private final String fieldType;

    private final DataFieldTypeEnum dataFieldType;

    private final Integer length;

    private final Boolean notNull;

    /**
     * 判断是否有当前字段
     * @param fieldName
     * @return
     */
    public static Boolean containsByFieldName(String fieldName){
        return Arrays.stream(ExcelSysFieldEnum.values()).anyMatch(e -> StrUtil.equals(e.getFieldName(), fieldName));
    }

    /**
     * 判断是否有当前字段
     * @param fieldName
     * @return
     */
    public static ExcelSysFieldEnum valueOfByFieldName(String fieldName){
        return Arrays.stream(ExcelSysFieldEnum.values()).filter(e -> StrUtil.equals(e.getFieldName(), fieldName)).findFirst().orElse(null);
    }

}
