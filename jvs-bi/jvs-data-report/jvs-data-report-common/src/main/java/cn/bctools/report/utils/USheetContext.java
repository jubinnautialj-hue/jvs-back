package cn.bctools.report.utils;

import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.model.univer.conf.USearch;
import cn.bctools.report.model.univer.conf.USearchField;
import cn.bctools.report.render.Occupied;
import cn.bctools.report.render.RenderingGroup;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * sheet页数据
 */
public class USheetContext {

    /**
     * sheet页 用户自定义的合并项
     */
    private static final ThreadLocal<Map<String,URange>> CUSTOM_MERGE = new TransmittableThreadLocal<>();

    /**
     * 根据用户自定义的合并项 生成的合并数据
     */
    private static final ThreadLocal<List<URange>> GENERATE_MERGE = new ThreadLocal<>();

    /**
     * 占用
     */
    private static final ThreadLocal<Map<Integer,Map<Integer,Occupied>>> OCCUPIED = TransmittableThreadLocal.withInitial(HashMap::new);

    /**
     * 单元格
     */
    private static final ThreadLocal<List<UCell>> CELLS = TransmittableThreadLocal.withInitial(ArrayList::new);

    /**
     * 查询条件
     * {数据集id:{字段key:字段entity}}
     */
    private static final ThreadLocal<Map<String, Map<String, USearchField>>> SEARCH = TransmittableThreadLocal.withInitial(HashMap::new);


    /**
     * 设置单元格合并设置
     * @param defaultList
     */
    public static void setCustomMerge(List<URange> defaultList){
        Map<String, URange> collect = defaultList.stream().collect(Collectors.toMap(e -> e.getStartRow() + "_" + e.getStartColumn(), Function.identity(), (v1, v2) -> v1));
        CUSTOM_MERGE.set(collect);
    }

    /**
     * 获取单元格合并设置
     * @return
     */
    public static Map<String,URange> getCustomMerge(){
        return CUSTOM_MERGE.get();
    }

    /**
     * 设置单元格占用
     * @param renderingGroup
     * @param list
     */
    public static void setOccupied(RenderingGroup renderingGroup, List<UCell> list){
        Map<Integer, Map<Integer, Occupied>> collect = list.parallelStream().map(e -> new Occupied()
                .setBaseR(renderingGroup.getBaseR())
                .setBaseC(renderingGroup.getBaseC())
                .setOR(e.getCustom().getOR())
                .setOC(e.getCustom().getOC())
                .setR(e.getR())
                .setC(e.getC())
        ).collect(Collectors.groupingBy(Occupied::getR, Collectors.toMap(Occupied::getC, Function.identity(), (v1, v2) -> v1)));
        Map<Integer, Map<Integer, Occupied>> occupied = OCCUPIED.get();
        collect.forEach((rowNum,columnMap) -> {
            if(occupied.containsKey(rowNum)){
                occupied.get(rowNum).putAll(columnMap);
            }else{
                occupied.put(rowNum, columnMap);
            }
        });
    }

    /**
     * 获取单元格占用
     * @return
     */
    public static Map<Integer,Map<Integer,Occupied>> getOccupied(){
        return OCCUPIED.get();
    }

    /**
     * 添加单元格
     * @param list
     */
    public static void setCells(List<UCell> list){
        CELLS.get().addAll(list);
    }

    /**
     * 获取所有单元格
     * @return
     */
    public static List<UCell> getCells(){
        return CELLS.get();
    }

    /**
     * 缓存生成的单元格的合并数据
     * @param list 生成的单元格的合并数据
     */
    public static void setMerge(List<URange> list){
        GENERATE_MERGE.set(list);
    }

    /**
     * 获取生成的单元格的合并数据
     * @return 生成的单元格的合并数据
     */
    public static List<URange> getMerge(){
        return GENERATE_MERGE.get();
    }


    /**
     * 设置查询条件
     * @param queryConf
     */
    public static void setSearch(List<USearch> queryConf){
        if(CollectionUtil.isNotEmpty(queryConf)){
            Map<String, Map<String, USearchField>> collect = queryConf.stream().collect(Collectors.toMap(USearch::getExecuteName, e -> e.getFields().stream().collect(Collectors.toMap(USearchField::getFieldKey, Function.identity(), (v1, v2) -> v1)), (v1, v2) -> v1));
            SEARCH.set(collect);
        }
    }

    /**
     * 根据表id获取查询条件
     * @param executeName
     * @return
     */
    public static Map<String,USearchField> getSearchFields(String executeName){
        if(SEARCH.get() == null){
            return null;
        }
        return SEARCH.get().get(executeName);
    }


    public static void clear(){
        CUSTOM_MERGE.remove();
        OCCUPIED.get().clear();
        CELLS.get().clear();
        GENERATE_MERGE.remove();
        SEARCH.get().clear();
    }
}
