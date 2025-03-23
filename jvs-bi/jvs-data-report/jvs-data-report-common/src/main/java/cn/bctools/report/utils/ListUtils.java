package cn.bctools.report.utils;

import cn.bctools.report.model.univer.UCell;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.google.common.base.Objects;

import java.util.*;

/**
 * 列表工具
 */
public class ListUtils {

    /**
     * 按指定的size列表进行拆分
     * @param list
     * @param sizeList
     * @return
     */
    public static List<List<UCell>> partition(List<UCell> list,List<Integer> sizeList){
        if(CollectionUtil.isEmpty(sizeList)){
            return Collections.singletonList(list);
        }
        List<List<UCell>> lists = new ArrayList<>();;
        int start = 0;
        for (Integer sub : sizeList) {
            int end = start + sub;
            List<UCell> subData = ListUtil.sub(list, start, end);
            lists.add(subData);
            start = end;
        }
        return lists;
    }

    public static List<Integer> statistic(List<UCell> cells){
        Object prefix = null;
        int num = 0;
        List<Integer> list = new ArrayList<>();
        Iterator<UCell> iterator = cells.iterator();
        while(iterator.hasNext()){
            UCell cell = iterator.next();
            Object v = cell.getV();
            if(prefix==null){
                num++;
                prefix = v;
            }else{
                if(Objects.equal(prefix,v)){
                    num++;
                }else{
                    prefix = v;
                    list.add(num);
                    num=1;
                }
            }
            if(!iterator.hasNext()){
                list.add(num);
                break;
            }
        }
        return list;
    }

    public static List<Integer> statisticBySign(List<UCell> cells){
        Object prefix = null;
        int num = 0;
        List<Integer> list = new ArrayList<>();
        Iterator<UCell> iterator = cells.iterator();
        while(iterator.hasNext()){
            UCell cell = iterator.next();
            Object v = cell.getCustom().getCustomMergeSign();
            if(prefix==null){
                num++;
                prefix = v;
            }else{
                if(Objects.equal(prefix,v)){
                    num++;
                }else{
                    prefix = v;
                    list.add(num);
                    num=1;
                }
            }
            if(!iterator.hasNext()){
                list.add(num);
                break;
            }
        }
        return list;
    }

    public static boolean isIn(Collection<Integer> scope,Integer num){
        Integer min = CollectionUtil.min(scope);
        Integer max = CollectionUtil.max(scope);
        return num>=min && num<=max;
    }
}
