package cn.bctools.document.entity;

import cn.bctools.common.entity.dto.UserDto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("版本管理")
@EqualsAndHashCode(callSuper = false)
@TableName(value = "dc_version", autoResultMap = true)
public class DcVersion implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("文档id")
    @TableField("dc_id")
    private String dcId;
    @ApiModelProperty("版本号")
    private String versionNumber;
    @ApiModelProperty("mongodb的数据")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private Map mongodbContent;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableField(exist = false)
    @ApiModelProperty(value = "分组时间")
    private String groupTime;
    @ApiModelProperty("文件存储路径")
    private String filePath;
    @ApiModelProperty("是否为自动封装的版本号")
    private Boolean voluntarilyIs;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("提交人")
    private String createById;
    @ApiModelProperty("提交人信息")
    @TableField(exist = false)
    private UserDto userDto;
}
