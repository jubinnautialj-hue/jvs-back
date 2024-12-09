package cn.bctools.function.entity.vo;

import cn.bctools.function.enums.JvsParamType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Parameter implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 类型
     */
    JvsParamType type;
    /**
     * 值
     */
    Object value;
    /**
     * 标签
     */
    String label;
}
