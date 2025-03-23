package cn.bctools.document.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 两个数组进行比较 判断出需要新增 删除
 */
public class ListUpdateUtil {
//    public static <T,K> List<T> getUpdate(List<T> originalList, List<T> nowList, Function<T,K> comparisonKey) {
//        //获取需要对比的值的结果
//        List<K> kList = originalList.parallelStream().map(comparisonKey::apply).collect(Collectors.toList());
//        //需要删除的值
//
//        //获取变更的值
//        nowList.parallelStream()
//        //新增的值
//    }


    /**
     * 对比结果返回值
     */
    public static class ListUpdate<T> {
        /**
         * 需要删除的
         */
        private List<T> removeList;
        /**
         * 需要新增的
         */
        private List<T> addList;
        /**
         * 需要修改的
         */
        private List<T> updateList;
    }
}
