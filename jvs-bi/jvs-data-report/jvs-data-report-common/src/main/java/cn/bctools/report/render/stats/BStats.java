package cn.bctools.report.render.stats;

import cn.bctools.report.dto.GroupRecord;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.stats.StatsSetup;
import cn.bctools.report.model.univer.style.BD;
import cn.bctools.report.model.univer.style.UStyle;
import cn.bctools.report.utils.UWorkBookUtils;
import cn.bctools.report.utils.UWorkbookContext;
import cn.hutool.core.bean.BeanUtil;

import java.util.List;
import java.util.Map;

public interface BStats {

    /**
     * 是否启用 统计设置
     * @param group 统计设置单元格
     * @return true 启用 false未启用
     */
    boolean status(UCell group);

    /**
     * 排序
     * @param groups 分组单元格
     */
    void sort(List<UCell> groups);

    /**
     * 执行小计 总计等统计配置
     * @param groupCell
     * @param cells
     * @param gridLinkedList
     * @param groupRecords
     * @param rowNos
     * @return
     */
    List<UCell> calculation(UCell groupCell,final List<UCell> cells, List<List<UCell>> gridLinkedList, Map<String, GroupRecord> groupRecords, List<Integer> rowNos,List<Integer> colNos,List<Integer> copyRNos,List<Integer> copyCNos,List<Integer> newRowIndex,List<Integer> newColIndex);

    /**
     * 创建单元格样式
     * 根据小计 总计配置生成
     * 只覆盖字体大小 字体加粗 斜体 下划线 字体颜色 单元格背景色
     * @param uCell 分组单元格
     * @param statsSetup 统计设置
     * @return 样式id
     */
    default String buildStyle(UCell uCell, StatsSetup statsSetup){
        UStyle presetStyle = statsSetup.getStyle();
        Object s = uCell.getS();
        UStyle style;
        String newStyleKey = UWorkBookUtils.newStyleKey();
        UStyle newStyle;
        if (s!=null) {
            if(s instanceof String){
                UStyle originStyle = UWorkbookContext.getStyle(s.toString());
                style = BeanUtil.copyProperties(originStyle, UStyle.class);
            }else{
                style = BeanUtil.copyProperties(s,UStyle.class);
            }
            replaceStyle(presetStyle,style);
            newStyle = style;
        }else{
            newStyle = presetStyle;
        }
        UWorkbookContext.setStyle(newStyleKey,newStyle);
        return newStyleKey;
    }

    default void replaceStyle(UStyle presetStyle, UStyle style){
        if(presetStyle==null || style==null){
            return;
        }
        //设置字体大小
        style.setFs(presetStyle.getFs())
                //字体颜色
                .setCl(presetStyle.getCl())
                //是否斜体
                .setIt(presetStyle.getIt())
                //是否加粗
                .setBl(presetStyle.getBl())
                //下划线
                .setUl(presetStyle.getUl())
                //文本位置
                .setHt(presetStyle.getHt())
                //单元格背景色
                .setBg(presetStyle.getBg());
    }

    default BD getDefaultBD(){
        BD.Style style = new BD.Style().setS(1);
        return new BD().setL(style).setB(style).setL(style).setT(style);
    }

    default void changeIndex(int index,List<Integer> indexRecords){
        for (int i = 0; i < indexRecords.size(); i++) {
            Integer record = indexRecords.get(i);
            if(record>=index){
                indexRecords.set(i,record+1);
            }
        }
        indexRecords.add(index);
    }
}
