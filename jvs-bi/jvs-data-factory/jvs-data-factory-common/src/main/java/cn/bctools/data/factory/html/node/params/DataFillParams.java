package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.enums.DataFillTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 数据填充的入参
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class DataFillParams extends NodeHtml<DataFillParams> {

    private List<DataFillObj> dataFillObj;

    @Data
    @Accessors(chain = true)
    @ApiModel("数据填充的入参")
    public static class DataFillObj extends DataSourceField {
        /**
         * 差值 时间确认值
         */
        private Integer differenceValue;
        /**
         * 跨度单位 用于时间字段  例如  年 月 日
         * 如果为时间 值可查看 注意使用的是name不是value {@link cn.hutool.core.date.DateField}
         */
        private String spanUnit;
        /**
         * 填充方式
         */
        private DataFillTypeEnum type;
        /**
         * 是否为主要填充数据
         */
        private Boolean isMain;
        /**
         * 开始值 不管是时间还是数字统一转为 字符串方便处理
         */
        private String startValue;

        /**
         * 结束值  不管是时间还是数字统一转为 字符串方便处理
         */
        private String endValue;
        /**
         * 固定值
         */
        private String fixedValue;
        /**
         * 枚举值
         */
        private List<String> enumsValue;
        /**
         * 表名称后端生成
         */
        private String tableName;

    }
}
