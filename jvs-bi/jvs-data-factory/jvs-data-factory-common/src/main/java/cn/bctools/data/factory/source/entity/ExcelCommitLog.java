package cn.bctools.data.factory.source.entity;

import cn.bctools.data.factory.source.enums.ExcelUpdateType;
import cn.bctools.data.factory.source.handler.OssBaseFileHandler;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * excel 上传历史
 * </p>
 *
 * @author admin
 * @since 2024-06-11
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "excel_commit_log", autoResultMap = true)
@ApiModel(value = "ExcelUpdateLog对象", description = "excel 上传历史")
public class ExcelCommitLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("上传文件名称")
    @TableField("file_name")
    private String fileName;

    @ApiModelProperty("文件配置")
    @TableField(value = "file_config", typeHandler = OssBaseFileHandler.class)
    private FileNameDto fileConfig;

    @ApiModelProperty("数据源id")
    @TableField("data_source_id")
    private String dataSourceId;

    @ApiModelProperty("批次号")
    @TableField("lot_no")
    private String lotNo;

    @ApiModelProperty("上传类型 append追加 overwrite覆盖")
    @TableField("upload_type")
    private ExcelUpdateType uploadType;

    @ApiModelProperty("操作状态 true可操作 false不可操作")
    @TableField("operate_status")
    private Boolean operateStatus;


    @ApiModelProperty("文件地址")
    @TableField(exist = false)
    private String fileUrl;

}
