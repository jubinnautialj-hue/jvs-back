package cn.bctools.data.factory.source.data.excel;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
public class TitleReadDataListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 每批次的数据量
     */
    List<DataSourceStructure> structures = new ArrayList<>();

    public void setStructures(List<DataSourceStructure> structures) {
        this.structures = structures;
    }


    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {

    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        //去除前后的空格
        List<String> list = headMap.values().stream().map(StrUtil::trim).collect(Collectors.toList());
        int sheetNo = context.readSheetHolder().getSheetNo();
        DataSourceStructure dataSourceStructure = this.structures.get(sheetNo);
        boolean empty = dataSourceStructure.getStructure().isEmpty();
        //如果原来的结构存在就需要判断是否与现在的名称一致
        if (!empty) {
            //忽略系统固定添加的字段
            List<String> sysFiled = Arrays.stream(ExcelSysFieldEnum.values()).map(ExcelSysFieldEnum::getFieldName).collect(Collectors.toList());
            List<String> nameList = dataSourceStructure.getStructure().stream().map(DataSourceStructure.Structure::getColumnCount).filter(e -> !sysFiled.contains(e)).collect(Collectors.toList());
            if (!new HashSet<>(nameList).containsAll(list)) {
                String sheetName = context.readSheetHolder().getSheetName();
                String tableName = dataSourceStructure.getName();
                List<String> noContainsFields = list.stream().filter(e -> !nameList.contains(e)).collect(Collectors.toList());
                String format = StrUtil.format("sheet：{} 字段：{} 在表：{} 中不存在,无法导入",sheetName,noContainsFields,tableName);
                throw new BusinessException(format);
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

}