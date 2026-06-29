package cn.bctools.document.po;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.InputStream;

@Data
@ApiModel("解压返回值")
@Accessors(chain = true)
public class UnzipPo {
    /**
     * 是否为文件夹
     */
    private Boolean isDirectory;
    /**
     * 文件名称 也可以说是 路径
     */
    private String name;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 上级文件路径
     */
    private String parentLevel;
    /**
     * 文档id
     */
    private String dcId;
    /**
     * 是否为顶级
     */
    private Boolean topIs;
    /**
     * 文件流
     */
    private InputStream inputStream;
}
