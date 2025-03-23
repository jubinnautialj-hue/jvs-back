package cn.bctools.data.factory.source.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QueryTypeDto {

    /**
     * 中文名称
     */
    private String name;

    /**
     * 简写
     */
    private String shorthand;

    /**
     * 类型
     */
    private String type;
}
