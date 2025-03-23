package cn.bctools.report.render.calculation;

import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellType;
import cn.bctools.report.model.univer.conf.URange;
import cn.bctools.report.render.Calculation;
import cn.bctools.report.render.CalculationType;
import cn.bctools.report.render.RenderingGroup;
import cn.bctools.report.utils.ExcelUtils;
import cn.bctools.report.utils.USheetContext;
import cn.bctools.report.utils.USheetUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.cell.CellLocation;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公式计算
 * @author wl
 */
@Component(CalculationType.FUNCTION)
public class FunctionCalculation implements Calculation {

    private static final String CH_COMMA = "，";
    private static final String CH_COLON = "：";

    @Override
    public boolean check(RenderingGroup conf) {
        return true;
    }

    @Override
    public List<UCell> data(RenderingGroup conf) {
        List<UCell> confCells = conf.getCells();
        //公式单元格
        UCell funcCell = CollectionUtil.getFirst(confCells);
        String f = funcCell.getF();
        if(StrUtil.isBlank(f)){
            return confCells;
        }
        f = replaceCh(f);

        EDataGrowthPlan dataGrowthPlan = funcCell.getCustom().getDataGrowthPlan();

        List<String> locationNames = ReUtil.findAllGroup1("([A-Za-z]+\\d+)", f);

        Map<String, CellLocation> locationRefMap = locationNames
                .stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::getLocation, (v1, v2) -> v1));

        List<UCell> cells = USheetContext.getCells();

        //纵向拓展
        if(!EDataGrowthPlan.NONE.equals(dataGrowthPlan) && !EDataGrowthPlan.CROSS_TAB.equals(dataGrowthPlan)){

            List<Integer> rList = locationRefMap.values().stream().map(CellLocation::getY).collect(Collectors.toList());
            List<Integer> cList = locationRefMap.values().stream().map(CellLocation::getX).collect(Collectors.toList());

            Map<String, List<UCell>> collect = cells.stream().filter(e -> rList.contains(e.getOR()) && cList.contains(e.getOC())).collect(Collectors.groupingBy(e -> e.getOR()+"_"+ e.getOC()));

            int count = collect.values().stream().map(List::size).max(Integer::compareTo).orElse(0);

            List<UCell> growthCells = new ArrayList<>();
            Map<String,LocationAddr> recordMap = new HashMap<>();
            for (int i = 0; i < count; i++) {
                UCell clone = funcCell.clone();
                if (EDataGrowthPlan.VERTICAL.equals(dataGrowthPlan)) {
                    clone.setR(clone.getR()+i);
                }else{
                    clone.setC(clone.getC()+i);
                }
                String cloneFuc = clone.getF();
                for (Map.Entry<String, CellLocation> entry : locationRefMap.entrySet()) {
                    String key1 = entry.getKey();
                    CellLocation cellLocation = entry.getValue();
                    String key = cellLocation.getY()+"_"+cellLocation.getX();
                    List<UCell> uCells = collect.get(key);
                    String ref;
                    if(i<uCells.size()){
                        UCell uCell = uCells.get(i);
                        String colName = ExcelUtil.indexToColName(uCell.getC());
                        ref = colName+(uCell.getR()+1);
                        recorde(recordMap,key,uCell);
                    }else{
                        ref = getRefAndIncrement(recordMap, key);
                    }
                    cloneFuc = StrUtil.replace(cloneFuc,key1,ref);
                }
                clone.setF(cloneFuc).setV(null).setT(UCellType.NUMBER);
                growthCells.add(clone);
            }
            return growthCells;
        }else{
            f = StrUtil.replace(f, StringPool.COLON, StringPool.COMMA);
            Map<String, String> refChanges = locationRefMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                        CellLocation location = e.getValue();
                        List<UCell> collect = cells.stream()
                                .filter(v -> location.getY() == v.getOR() && location.getX() == v.getOC())
                                .sorted(UCell::compareTo)
                                .collect(Collectors.toList());

                        UCell first = CollectionUtil.getFirst(collect);
                        String firstRef = ExcelUtils.indexToRef(first.getR(), first.getC());
                        UCell last = CollectionUtil.getLast(collect);
                        String lastRef = ExcelUtils.indexToRef(last.getR(), last.getC());
                        return firstRef + ":" + lastRef;
                    }));

            for (Map.Entry<String, String> entry : refChanges.entrySet()) {
                f = ReUtil.replaceAll(f, entry.getKey(), entry.getValue());
            }
            UCell clone = funcCell.clone();
            clone.setF(f).setV(null).setT(UCellType.NUMBER);
            return Collections.singletonList(clone);
        }
    }

    @Override
    public List<URange> merge(RenderingGroup conf, List<UCell> cells) {
        return Collections.emptyList();
    }

    private String updateLocation(String calStr){
        if(StrUtil.contains(calStr,StringPool.COLON)){
            List<String> split = StrUtil.split(calStr, StringPool.COLON);
            return split.stream().map(this::update).collect(Collectors.joining(StringPool.COMMA));
        }
        return update(calStr);
    }

    private String update(String ref){
        List<UCell> cells = USheetContext.getCells();

        CellLocation location = getLocation(ref);
        List<UCell> collect = cells
                .stream()
                .filter(cell -> cell.getCustom().getOR() == location.getY() && cell.getCustom().getOC() == location.getX())
                .collect(Collectors.toList());

        if(CollectionUtil.isEmpty(collect)){
            return ref;
        }
        UCell min = collect.stream().sorted(Comparator.comparing(UCell::getR)).sorted(Comparator.comparing(UCell::getC)).findFirst().get();
        String minRef = USheetUtils.getRef(min);
        UCell max = collect.stream().sorted(Comparator.comparing(UCell::getR).reversed()).sorted(Comparator.comparing(UCell::getC).reversed()).findFirst().get();
        String maxRef = USheetUtils.getRef(max);
        if(StrUtil.equals(minRef,maxRef)){
            return minRef;
        }
        return minRef+StringPool.COLON+maxRef;
    }

    private CellLocation getLocation(String locationRef){
        return ExcelUtil.toLocation(locationRef);
    }

    /**
     * 替换中文符号
     * @param f
     * @return
     */
    private String replaceCh(String f){
        String replace = StrUtil.replace(f, CH_COLON, StringPool.COLON);
        return StrUtil.replace(replace, CH_COMMA, StringPool.COMMA);
    }

    /**
     *
     * @param recordMap
     * @param key
     * @param uCell
     */
    private void recorde(Map<String,LocationAddr> recordMap,String key,UCell uCell){
        recordMap.compute(key, (locationRef,dto) -> {
            if(dto==null){
                return new LocationAddr().setLocationRef(locationRef).setDataGrowthPlan(uCell.getCustom().getDataGrowthPlan())
                        .setR(uCell.getR())
                        .setC(uCell.getC());
            }
            if(EDataGrowthPlan.VERTICAL.equals(dto.getDataGrowthPlan())){
                dto.setR(uCell.getR());
            }
            if(EDataGrowthPlan.HORIZONTAL.equals(dto.getDataGrowthPlan())){
                dto.setC(uCell.getC());
            }
            return dto;
        });
    }

    /**
     * 获取坐标引用
     * @param recordMap
     * @param key
     */
    private String getRefAndIncrement(Map<String,LocationAddr> recordMap,String key){
        LocationAddr locationAddr = recordMap.get(key);
        if(locationAddr==null){
            return key;
        }
        EDataGrowthPlan dataGrowthPlan = locationAddr.getDataGrowthPlan();
        String ref = key;
        if(EDataGrowthPlan.VERTICAL.equals(dataGrowthPlan)){
            int r = locationAddr.incrementR()+1;
            Integer c = locationAddr.getC();
            String colName = ExcelUtil.indexToColName(c);
            ref = colName+r;
        }
        if(EDataGrowthPlan.HORIZONTAL.equals(dataGrowthPlan)){
            int r = locationAddr.getR()+1;
            int c = locationAddr.incrementC();
            String colName = ExcelUtil.indexToColName(c);
            ref = colName+r;
        }
        return ref;
    }

    @Data
    @Accessors(chain = true)
    private static class LocationAddr{
        private String locationRef;

        private EDataGrowthPlan dataGrowthPlan;

        private Integer r;

        private Integer c;

        public Integer incrementR(){
            this.r +=1;
            return r;
        }
        public Integer incrementC(){
            this.c +=1;
            return c;
        }
    }
}
