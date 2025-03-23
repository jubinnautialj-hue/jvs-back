package cn.bctools.chart.chart.bo;


import cn.bctools.chart.enums.NullStringEnums;
import cn.bctools.chart.enums.UnitEnums;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.enums.SortTypeEnums;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class FieldsData extends DataSourceField {

    /**
     * 函数表达式
     */
    private String functionStr;
    /**
     * 别名
     */
    private String aliasName;
    /**
     * 计算类型
     */
    private CollectTypeEnum calculateType;

    /**
     * 是否为降序
     */
    private Boolean descIs;
    /**
     * 格式化
     */
    private FormatParams formatParams;

    /**
     * 排序
     */
    private SortTypeEnums sort;
    /**
     * 多Y轴混合图 指标类型 是线还是柱
     */
    private String seriesType;
    /**
     * 同环比
     */
    private List<YoYMoMComparisonsData> children;

    @Data
    @Accessors(chain = true)
    public static class FormatParams {
        /**
         * 计算时取整方式 4 四舍五入 5直接截断默认为5
         */
        private Integer roundingMode;

        /**
         * 计算时的小数保留位数默认为0
         */
        private Integer decimalPlace;
        /**
         * 前缀
         */
        private String prefix;
        /**
         * 后缀
         */
        private String suffix;

        /**
         * 单位
         */
        private UnitEnums unit;
        /**
         * 空值替换
         **/
        private List<NullStringEnums> nullStringEnums;

        /**
         * 时间格式
         **/
        private String dateFormat;
        /**
         * 空值替换 替换后 替换为具体值
         **/
        private String replaceValue;


    }

}
