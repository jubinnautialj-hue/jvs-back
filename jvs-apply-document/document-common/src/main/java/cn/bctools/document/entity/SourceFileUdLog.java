package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.document.entity.enums.SourceFileUdOperateStatusEnums;
import cn.bctools.document.entity.enums.SourceFileUdOperateTypeEnums;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 下载与上传记录
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "source_file_ud_log")
@ApiModel
public class SourceFileUdLog extends BasalPo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("操作类型")
    private SourceFileUdOperateTypeEnums operateType;
    @ApiModelProperty("操作状态")
    private SourceFileUdOperateStatusEnums operateStatus;
    @ApiModelProperty("失败原因")
    private String errorMessage;
    @ApiModelProperty("数据id")
    private String dcId;
    @ApiModelProperty("失败原因-日志详细")
    private String errorLog;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("文件路径")
    private String fileName;
}
