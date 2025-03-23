package cn.bctools.data.factory.source.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author : admin
 */
@Data
@ApiModel("jar包信息")
@Accessors(chain = true)
@TableName(value = "sys_jar")
public class SysJar implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("名称")
    @TableField("jar_name")
    private String jarName;

    @ApiModelProperty("解释")
    @TableField("jar_explain")
    private String jarExplain;

    @ApiModelProperty("桶名称")
    @TableField("bucket_name")
    private String bucketName;

    @ApiModelProperty("文件名称")
    @TableField("file_path")
    private String filePath;
}


