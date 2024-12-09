package cn.bctools.auth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs 首页数据获取的显示信息
 */
@Data
@Accessors(chain = true)
public class IndexAppletVo {

    /**
     * 兼容如历史信息
     */
    String indexUrl;

    /**
     * 展示的首页信息
     */
    List data;

    /**
     * 类型
     */
    IndexType type;

    /**
     * The enum Index type.
     */
    public static enum IndexType {
        /**
         * 指定 url，
         */
        url,
        /**
         * 动态首页
         */
        dynamicIndex;
    }

}
