package cn.bctools.index.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs The type Form query params dto.
 */
@Data
@Accessors(chain = true)
public class FormQueryParamsDto {

    /**
     * key
     */
    private String prop;

    /**
     * value
     */
    private Object value;
}
