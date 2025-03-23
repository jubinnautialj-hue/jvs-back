package cn.bctools.data.factory.doris.po;


import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class AvgSettingPo {
    /**
     * 小数位数
     */
    private Integer decimalDigits;
    /**
     * 是否截断 不进行4舍五入
     */
    private Boolean truncation;
}
