package cn.bctools.report.render.calculation;

import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.render.Calculation;
import cn.bctools.report.render.CalculationType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.utils.SourceUtils;
import cn.bctools.report.utils.SqlUtils;
import cn.bctools.report.utils.USheetUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.cell.CellLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 交叉拓展
 * @author wl
 */
@Slf4j
@Component(CalculationType.CROSS_TAB)
@RequiredArgsConstructor
public class CrossTabCalculation implements Calculation {

    private final SourceUtils sourceUtils;

    /**
     * 交叉拓展数据查询语句
     * 参数解释
     * 1. 目录key
     * 2. 交叉拓展字段sql
     * 3. 基础数据查询sql
     * 4. 排序
     */
    private static final String CROSS_TABLE_SQL_FORMAT = "SELECT CONCAT({}) AS \"rowMenuKey\" ,{} FROM ({}) AS crosstab {}";

    @Override
    public boolean check(RenderingGroup conf) {
//        List<UCell> cells = conf.getCells();
//        long count = cells.stream().map(UCell::getCustom).map(UCellExpand::getField).filter(Objects::nonNull).map(UField::getExecuteName).filter(StrUtil::isNotBlank).distinct().count();
//        if(count>1){
//            throw new BusinessException("不支持多个数据源");
//        }
        return true;
    }

    @Override
    public List<UCell> data(RenderingGroup conf) {
        List<UCell> data = new ArrayList<>();

        List<UCell> uCells = conf.getCells();

        String tableNames = sourceUtils.getTableNames(uCells);

        List<UCell> crossTabs = uCells.stream().filter(e -> e.getCustom().isCrossTable()).sorted(Comparator.comparing(UCell::getC)).collect(Collectors.toList());
        //基准
        UCell base = CollectionUtil.getFirst(crossTabs);

        //固定单元格
        List<UCell> finalCells = uCells.stream().filter(e -> e.getC() < base.getC() && e.getR() < base.getR()).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(finalCells)) {
            data.addAll(finalCells);
        }

        //查左侧目录
        List<UCell> lefts = uCells.stream().filter(e -> e.getR().equals(base.getR())).filter(e -> e.getC() < base.getC()).sorted(Comparator.comparing(UCell::getC)).collect(Collectors.toList());
        List<List<Object>> leftMenus = findMenuData(lefts, tableNames);
        List<UCell> leftCells= computeLeft(leftMenus, lefts);
        data.addAll(leftCells);

        //查顶部目录
        List<UCell> tops = uCells.stream().filter(e -> e.getC().equals(base.getC())).filter(e -> e.getR() < base.getR()).sorted(Comparator.comparing(UCell::getR)).collect(Collectors.toList());
        List<List<Object>> topMenus = findMenuData(tops, tableNames);
        List<UCell> topCells = computeTop(topMenus, crossTabs, tops);
        data.addAll(topCells);

        //固定表头
        Map<Integer, List<UCell>> finalHeadMap = uCells.stream()
                .filter(e -> e.getC() >= base.getC()).filter(e -> e.getR() < base.getR()).sorted(Comparator.comparing(UCell::getR))
                .filter(e -> EValueType.固定值.equals(e.getCustom().getValueType())).collect(Collectors.groupingBy(UCell::getC, Collectors.toList()));

        if(MapUtil.isNotEmpty(finalHeadMap)){
            int count = topMenus.size();
            for (int i = 0; i < count; i++) {
                for (UCell crossTab : crossTabs) {
                    Integer c = crossTab.getC();
                    if(finalHeadMap.containsKey(c)){
                        List<UCell> list = finalHeadMap.get(c);
                        int finalI = i;
                        List<UCell> collect = list.stream().map(e -> {
                            UCell head = e.clone();
                            head.setC(e.getC()+ finalI *crossTabs.size());
                            return head;
                        }).collect(Collectors.toList());
                        data.addAll(collect);
                    }
                }
            }
        }

        //交叉表格表头
        List<String> tableHeads = topMenus.stream().map(e -> e.stream().map(StrUtil::toString).collect(Collectors.joining("_"))).collect(Collectors.toList());
        //创建交叉拓展属性sql
        String crossTabFieldSql = buildCrossTabFieldSql(tableHeads, crossTabs);
        //查询动态数据
        List<Map<String, Object>> crossTabData = findCrossTabData(crossTabs, lefts, tops,crossTabFieldSql);

        List<UCell> crossTabCells = computeCrossTab(crossTabData,leftMenus, tableHeads, crossTabs);
        data.addAll(crossTabCells);

        return data;
    }

    /**
     * 查询目录数据
     * [["1-1","1-2"],["2-1","2-2"]]
     * @param uCells
     * @return
     */
    private List<List<Object>> findMenuData(List<UCell> uCells,String tableNames){
        List<List<Object>> data = new ArrayList<>();
        List<UField> fields = uCells.stream().map(UCell::getCustom).filter(e -> EValueType.数据集.equals(e.getValueType())).map(UCellExpand::getField).collect(Collectors.toList());

        UField first = CollectionUtil.getFirst(fields);

//        String tableName = sourceUtils.getTableNames(uCells);

        String collect = fields.stream().map(SqlUtils::buildQueryField).distinct().collect(Collectors.joining(","));

        String group = fields.stream().map(e -> StrUtil.format("`{}`.`{}`", e.getExecuteName(), e.getFieldKey(), e.getFieldKey())).collect(Collectors.joining(","));

        String sql = "SELECT "+ collect + " FROM "+ tableNames;

        //查询条件
        String where = SqlUtils.buildQueryWhere(first.getExecuteName());
        if(StrUtil.isNotBlank(where)){
            sql += " WHERE " + where;
        }

        //分组
        sql += " GROUP BY "+ group;

        //排序
        String orderBy = SqlUtils.buildOrderBy(uCells);
        if(StrUtil.isNotBlank(orderBy)){
            sql = sql + orderBy;
        }

        List<Map<String, Object>> tableData = sourceUtils.getData(sql);
        for (Map<String, Object> row : tableData) {
            List<Object> menu = fields.stream().map(e -> row.get(e.getFieldKey())).collect(Collectors.toList());
            data.add(menu);
        }
        return data;
    }

    /**
     * 查询交叉拓展数据
     * SELECT
     * 	CONCAT(`6d8b3c21fef66abe7f3951f552ca12cc`,"_",`db64e03c5087c62d5682cbff6bf3bbd7`) AS "rowMenuKey",
     * 	`e9f40e1f1d1658681dad2dac4ae0971e` [ "2020_1" ] AS "2020_1",
     * 	`78a5eb43deef9a7b5b9ce157b9d52ac4` [ "2020_2" ] AS "2020_2",
     * 	`e9f40e1f1d1658681dad2dac4ae0971e` [ "2021_1" ] AS "2021_1",
     * 	`78a5eb43deef9a7b5b9ce157b9d52ac4` [ "2021_2" ] AS "2021_2"
     * FROM
     * 	(
     * 	SELECT
     * 		`6107341d65d1b16ab39ac361369f4eeb`.`6d8b3c21fef66abe7f3951f552ca12cc`,
     * 		`6107341d65d1b16ab39ac361369f4eeb`.`db64e03c5087c62d5682cbff6bf3bbd7`,
     * 		MAP_AGG ( CONCAT( `6107341d65d1b16ab39ac361369f4eeb`.`84cdc76cabf41bd7c961f6ab12f117d8`, '_', `6107341d65d1b16ab39ac361369f4eeb`.`7436f942d5ea836cb84f1bb2527d8286` ), `6107341d65d1b16ab39ac361369f4eeb`.`e9f40e1f1d1658681dad2dac4ae0971e` ) AS "e9f40e1f1d1658681dad2dac4ae0971e",
     * 		MAP_AGG ( CONCAT( `6107341d65d1b16ab39ac361369f4eeb`.`84cdc76cabf41bd7c961f6ab12f117d8`, '_', `6107341d65d1b16ab39ac361369f4eeb`.`7436f942d5ea836cb84f1bb2527d8286` ), `6107341d65d1b16ab39ac361369f4eeb`.`78a5eb43deef9a7b5b9ce157b9d52ac4` ) AS "78a5eb43deef9a7b5b9ce157b9d52ac4"
     * 	FROM
     * 		( SELECT * FROM ods_6107341d65d1b16ab39ac361369f4eeb ) AS `6107341d65d1b16ab39ac361369f4eeb`
     * 	GROUP BY
     * 		`6107341d65d1b16ab39ac361369f4eeb`.`6d8b3c21fef66abe7f3951f552ca12cc`,
     * 		`6107341d65d1b16ab39ac361369f4eeb`.`db64e03c5087c62d5682cbff6bf3bbd7`
     * 	) AS crosstab
     * ORDER BY
     * 	`6d8b3c21fef66abe7f3951f552ca12cc` ASC,
     * 	`db64e03c5087c62d5682cbff6bf3bbd7` ASC
     * @param crossTabs 交叉拓展字段
     * @param lefts 目录字
     * @param tops 表头
     * @return 交叉拓展数据
     */
    private List<Map<String,Object>> findCrossTabData( List<UCell> crossTabs,List<UCell> lefts,List<UCell> tops,String crossTabFieldSql){
        String aggKey = tops.stream().map(UCell::getCustom).filter(e->EValueType.数据集.equals(e.getValueType())).map(UCellExpand::getField).map(SqlUtils::buildFullFieldKey).collect(Collectors.joining(",'_',"));
        String group = lefts.stream().map(UCell::getCustom).filter(e->EValueType.数据集.equals(e.getValueType())).map(UCellExpand::getField).map(SqlUtils::buildFullFieldKey).collect(Collectors.joining(","));
        String agg = crossTabs.stream().map(UCell::getCustom).filter(e->EValueType.数据集.equals(e.getValueType())).map(UCellExpand::getField).map(e -> StrUtil.format("MAP_AGG(CONCAT({}),`{}`.`{}`) AS \"{}\"", aggKey, e.getExecuteName(),e.getFieldKey(),e.getFieldKey())).collect(Collectors.joining(","));
        UField field = CollectionUtil.getFirst(crossTabs).getCustom().getField();
        String executeName = field.getExecuteName();
        String tableName = sourceUtils.getTableNames(crossTabs);
        String baseSql = "SELECT "+group+","+agg +" FROM " + tableName ;

        //查询条件
        String where = SqlUtils.buildQueryWhere(executeName);
        if(StrUtil.isNotBlank(where)){
            baseSql += " WHERE " + where;
        }

        baseSql+=" GROUP BY "+group;

        String rowMenuKey = lefts.stream()
                .map(UCell::getCustom)
                .filter(e -> EValueType.数据集.equals(e.getValueType()))
                .map(UCellExpand::getField)
                .map(SqlUtils::buildFieldKey)
                .collect(Collectors.joining(","));

        String orderBy = SqlUtils.buildOrderByNoTableName(lefts);

        String sql = StrUtil.format(CROSS_TABLE_SQL_FORMAT,rowMenuKey,crossTabFieldSql,baseSql,orderBy);

        return sourceUtils.getData(sql);
    }

    /**
     * 创建交叉报表sql
     * @param tableHeads 交叉表格表头
     * @param crossTabs 交叉拓展单元格
     * @return sql
     */
    private String buildCrossTabFieldSql(List<String> tableHeads,List<UCell> crossTabs){
        return tableHeads.stream().map(e -> crossTabs.stream().filter(v ->EValueType.数据集.equals(v.getCustom().getValueType())).map(v ->{
            String fieldKey = USheetUtils.getUCellFieldKey(v);
            return StrUtil.format("`{}`[\"{}\"] AS \"{}\"", fieldKey, e,e+"_"+fieldKey);
        }).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.joining(","));
    }

    /**
     * 计算左侧目录
     * @param leftMenus
     * @param lefts
     * @return
     */
    private List<UCell> computeLeft(List<List<Object>> leftMenus,List<UCell> lefts){
        List<UCell> list = new ArrayList<>();
        for (int i = 0; i < leftMenus.size(); i++) {
            List<Object> objects = leftMenus.get(i);
            for (int i1 = 0; i1 < objects.size(); i1++) {
                Object v = objects.get(i1);
                UCell uCell = lefts.get(i1);
                UCell uCell1 = uCell.clone();
                //空值替换
                v = USheetUtils.nullReplacement(uCell, v);
                uCell1.setV(v);
                uCell1.setC(uCell.getC());
                uCell1.setR(uCell.getR()+i);
                list.add(uCell1);
            }
        }
        return list;
    }

    /**
     * 计算顶部目录数据
     * @param topMenus
     * @param crossTabs
     * @param tops
     * @return
     */
    private List<UCell> computeTop( List<List<Object>> topMenus,List<UCell> crossTabs,List<UCell> tops){
        List<UCell> dynamicHeads = tops.stream().filter(e -> EValueType.数据集.equals(e.getCustom().getValueType())).collect(Collectors.toList());
        List<UCell> list = new ArrayList<>();
        int addC = 0;
        for (List<Object> objects : topMenus) {
            for (int i2 = 0; i2 < crossTabs.size(); i2++) {
                for (int i1 = 0; i1 < objects.size(); i1++) {
                    Object v = objects.get(i1);
                    UCell uCell = dynamicHeads.get(i1);
                    UCell uCell1 = uCell.clone();
                    //空值替换
                    v = USheetUtils.nullReplacement(uCell, v);
                    uCell1.setV(v);
                    uCell1.setR(uCell.getR());
                    uCell1.setC(uCell.getC()+addC);
                    list.add(uCell1);
                }
                addC++;
            }
        }
        return list;
    }

    /**
     * 计算交叉数据
     *
     * @param crossTabData       查询的交叉数据
     * @param leftMenus     目录数据
     * @param topConditions 顶部目录分类条件
     * @param crossTabs     交叉单元格
     * @return
     */
    private List<UCell> computeCrossTab(List<Map<String,Object>> crossTabData, List<List<Object>> leftMenus, List<String> topConditions, List<UCell> crossTabs){
        List<UCell> list = new ArrayList<>();

        Map<String, Map<String, Object>> rowMenuMap = crossTabData.parallelStream().collect(Collectors.toMap(e -> StrUtil.toString(e.get("rowMenuKey")), Function.identity(), (v1, v2) -> v2));

        //用于函数 排除多余的选项
        List<Integer> crossTabCList = crossTabs.stream().map(UCell::getC).collect(Collectors.toList());
        Integer crossTabR = CollectionUtil.getFirst(crossTabs).getOR();

        //行计数器
        AtomicInteger rowNum = new AtomicInteger(0);
        //循环目录
        leftMenus.stream().map(e -> e.stream().map(StrUtil::toString).collect(Collectors.joining())).forEach(rowMenuKey -> {
            //当前行的交叉拓展数据
            Map<String, Object> row = rowMenuMap.get(rowMenuKey);
            //每一行开始都要重置列号计数
            AtomicInteger colNum = new AtomicInteger(0);
            Map<Integer,UCell> uRowData = new HashMap<>();
            //循环交叉表头 获取每一列得数据
            topConditions.forEach(e -> {
                int increment = colNum.get();
                for (UCell uCell : crossTabs) {
                    UCell clone = uCell.clone();
                    if (EValueType.数据集.equals(clone.getCustom().getValueType())) {
                        setETLValue(e, uCell, row, clone);
                    }
                    if(EValueType.公式.equals(clone.getCustom().getValueType())){
                        setFunctionValue(clone, row, crossTabR, crossTabCList, uRowData);
                    }
                    clone.setR(clone.getR()+ rowNum.get());
                    clone.setC(clone.getC()+  increment);
                    list.add(clone);
                    uRowData.put(clone.getOC(),clone);
                }
                colNum.getAndAdd(crossTabs.size());
            });
            rowNum.getAndIncrement();
        });
        return list;
    }

    /**
     * 设置数据集值
     * @param e
     * @param uCell
     * @param row
     * @param clone
     */
    private static void setETLValue(String e, UCell uCell, Map<String, Object> row, UCell clone) {
        Object o = null;
        if (row !=null) {
            String fieldKey = USheetUtils.getUCellFieldKey(uCell);
            o = row.get(e +"_"+fieldKey);
        }
        //空值替换
        o = USheetUtils.nullReplacement(uCell, o);
        clone.setV(o);
    }

    /**
     * 设置公式值
     * @param clone
     * @param row
     * @param crossTabR
     * @param crossTabCList
     * @param uRowData
     */
    private static void setFunctionValue(UCell clone, Map<String, Object> row, Integer crossTabR, List<Integer> crossTabCList, Map<Integer, UCell> uRowData) {
        String f = clone.getF();
        clone.setV(null);
        if (row !=null && StrUtil.isNotBlank(f)) {
            List<String> locationNames = ReUtil.findAllGroup1("([A-Za-z]+\\d+)", f);
            Map<String, CellLocation> locationRefMap = locationNames
                    .stream()
                    .distinct()
                    .collect(Collectors.toMap(Function.identity(), ExcelUtil::toLocation, (v1, v2) -> v1));

            Map<String, String> collect = locationRefMap
                    .entrySet()
                    .stream()
                    .filter(v -> crossTabR.equals(v.getValue().getY()) && crossTabCList.contains(v.getValue().getX()))
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> {
                        CellLocation value = v.getValue();
                        UCell uCell1 = uRowData.get(value.getX());
                        String colName = ExcelUtil.indexToColName(uCell1.getC());
                        return colName + (uCell1.getR() + 1);
                    }));

            for (Map.Entry<String, String> entry : collect.entrySet()) {
                f = StrUtil.replace(f, entry.getKey(), entry.getValue());
            }
            clone.setF(f);
        }
    }

    @Override
    public List<URange> merge(RenderingGroup conf, List<UCell> cells) {
        return USheetUtils.merge(conf,cells);
    }

    @Override
    public void stats(RenderingGroup conf, List<UCell> cells) {
        try {
            USheetUtils.stats(conf,cells);
        } catch (Exception e) {
            log.error("--------->交叉报表统计计算异常<---------");
        }
    }
}
