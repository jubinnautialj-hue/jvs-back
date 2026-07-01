package cn.bctools.design.data.fields.dto.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <携带参数类型>
 *
 * @author auto
 **/
@ApiModel("携带参数类型")
@Getter
@AllArgsConstructor
public enum CarryParameterTypeEnum {

    /**
     * 携带行内的数据
     */
    LINE("行内"),
    /**
     * 查询条件
     */
    QUERY("查询条件"),
    /**
     * 不携带
     */
    NO("不携带"),
    ;
    /**
     * 描述
     */
    private final String desc;
}
