package cn.bctools.data.factory.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
public class FactoryMoveDto {
    /**
     * 目录或子级id
     */
    private String id;

    /**
     * 拖拽方向 true向上 false向下
     */
    private Boolean isFront;

    /**
     * 涉及id
     */
    private String nextId;

    /**
     * 父级id
     */
    private String parentId;
}
