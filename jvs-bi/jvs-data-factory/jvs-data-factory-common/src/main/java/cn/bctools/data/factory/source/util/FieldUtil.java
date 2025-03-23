package cn.bctools.data.factory.source.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;

import java.util.List;

public class FieldUtil {

    /**
     * 获取数据库字段属性 设置的范围限制
     * VARCHAR(8) -> 8
     * DECIMAL(18,5) -> 18,5
     * @param type 字段名称
     * @return
     */
    public static String subStr(String type){
        try {
            List<String> group1 = ReUtil.findAllGroup1("(\\(.*?\\))", type);
            if(CollectionUtil.isEmpty(group1)){
                return null;
            }
            String first = CollectionUtil.getFirst(group1);
            first = StrUtil.replace(first, StringPool.LEFT_BRACKET, "");
            return StrUtil.replace(first, StringPool.RIGHT_BRACKET, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
