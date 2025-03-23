package cn.bctools.document.office.po;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OfficeImagePo {
    /**
     * 物理地址
     */
    private String originalFilePath;
    /**
     * url
     */
    private String fileLink;
}
