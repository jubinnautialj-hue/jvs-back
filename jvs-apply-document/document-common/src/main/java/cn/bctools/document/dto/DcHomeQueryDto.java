package cn.bctools.document.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DcHomeQueryDto {

    /**
     * 选择的文库id
     */
    private String ids;

    /**
     * 是否显示文库的文档
     */
    private Boolean showDoc = Boolean.FALSE;
}
