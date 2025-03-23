package cn.bctools.chart.dto;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@ApiModel("数据联动")
@Data
@Accessors(chain = true)
public class LinkDataDto {
    @ApiModelProperty("是否为and")
    private Boolean isAnd;

    @ApiModelProperty("联动具体数据")
    private List<LinkData> linkData;

    @ApiModel("绑定关系")
    @Data
    @Accessors(chain = true)
    public static class LinkData {
        @ApiModelProperty("条件类型 目前只能是等于 方便后续的变更")
        private QueryEnums queryEnums;
        @ApiModelProperty("值  应该是 只有一个值 但是方便后续扩展 先直接使用数组")
        private List<Object> value;
        @ApiModelProperty("数据格式")
        private String format;
        @ApiModelProperty("实际字段key")
        private String fieldKey;
        @ApiModelProperty("字段类型")
        private DataFieldTypeEnum fieldType;
    }

    /**
     * 方便其他地方判断空
     */
    public Boolean checkIsEmpty() {
        return this.getLinkData() == null || this.getLinkData().isEmpty();
    }
}
