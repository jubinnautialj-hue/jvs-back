package cn.bctools.design.data.fields.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据事件类型
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum DataEventType {

    /**
     * 新增
     */
    DATA_NEW("新增数据"),
    /**
     * 修改
     */
    DATA_UPDATE("修改数据"),
    /**
     * 删除
     */
    DATA_DELETE("删除数据"),
    /**数据导入*/
    DATA_IMPORT("导入数据"),
    ;

    @ApiModelProperty("事件名称")
    private final String desc;

}
