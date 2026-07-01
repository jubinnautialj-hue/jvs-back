package cn.bctools.rule.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class BindBaseDto {
    /**
     * 绑定条件类型
     */
    LinkTypeEnum bindType;
    /**
     * 条件公式,现在默认为等于
     */
    List<BindConditionsDto> conditions;

    /**
     * 根据条件设置多个数据值
     */
    List<BindBaseBodyDto> params;

    /**
     * 公式内容
     */
    String formulaContent;
    /**
     * 公式触发逻辑
     */
    String formula;

}
