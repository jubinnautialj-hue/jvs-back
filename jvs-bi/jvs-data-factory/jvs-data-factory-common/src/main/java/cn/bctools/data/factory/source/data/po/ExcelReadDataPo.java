package cn.bctools.data.factory.source.data.po;

import cn.bctools.oss.dto.FileNameDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * excel 读取入参
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ExcelReadDataPo extends ReadDataPo {

    /**
     * 文件地址
     */
    private String url;

    /**
     * 数据源id
     */
    private String dataSourceId;

    /**
     * 是否为追加
     */
    private Boolean addIs;

    /**
     * 文件配置
     */
    private FileNameDto baseFile;
}
