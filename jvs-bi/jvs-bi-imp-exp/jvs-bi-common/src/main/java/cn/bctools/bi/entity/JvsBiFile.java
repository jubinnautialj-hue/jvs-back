package cn.bctools.bi.entity;

import cn.bctools.bi.entity.enums.JvsBiFileOperateTypeEnums;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.database.entity.po.BasalPo;
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
 * 下载与上传记录
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_bi_file")
@ApiModel
public class JvsBiFile extends BasalPo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("操作类型")
    private JvsBiFileOperateTypeEnums operateType;
    @ApiModelProperty("服务类型")
    private TaskTypeEnums serveType;
    @ApiModelProperty("服务id")
    private String dataId;
    @ApiModelProperty("操作状态")
    private Boolean operateStatus;
    @ApiModelProperty("失败原因")
    private String errorMessage;
    @ApiModelProperty("失败原因-日志详细")
    private String errorLog;
    @ApiModelProperty("桶名称")
    private String bucketName;
    @ApiModelProperty("文件路径")
    private String fileName;
    @ApiModelProperty("是否只下载mock数据")
    @TableField(exist = false)
    private Boolean isMock;
}
