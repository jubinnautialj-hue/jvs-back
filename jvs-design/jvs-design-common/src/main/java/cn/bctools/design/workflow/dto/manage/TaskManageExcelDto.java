package cn.bctools.design.workflow.dto.manage;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务管理列表入参")
@ColumnWidth(20)
public class TaskManageExcelDto {

    @ExcelIgnore
    private String id;

    @ApiModelProperty(value = "流程名称")
    @ExcelProperty(value = "流程名称")
    private String name;

    @ApiModelProperty(value = "流程编号")
    @ExcelProperty(value = "流程编号")
    @ColumnWidth(7)
    private String taskCode;

//    @ApiModelProperty(value = "流程标题")
//    @ExcelProperty(value = "流程标题")
//    private String title;

    @ApiModelProperty(value = "发起人单位名称")
    @ExcelProperty(value = "发起人单位名称")
    private String createDeptName;

    @ApiModelProperty(value = "发起人")
    @ExcelProperty(value = "发起人")
    private String createBy;

    @ApiModelProperty(value = "节点名称")
    @ExcelProperty(value = "节点名称")
    private String nodeName;

    @ApiModelProperty(value = "处理人")
    @ExcelProperty(value = "处理人")
    @ColumnWidth(7)
    private String userName;

    @ApiModelProperty(value = "操作类型")
    @ExcelProperty(value = "操作类型")
    @ColumnWidth(7)
    private String nodeOperation;

    @ApiModelProperty(value = "审批意见")
    @ExcelProperty(value = "审批意见")
    @ColumnWidth(25)
    private String content;

    @ApiModelProperty(value = "到达时间")
    @ExcelProperty(value = "到达时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private Date arrivalTime;

    @ApiModelProperty(value = "处理时间")
    @ExcelProperty(value = "处理时间")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private Date time;

    @ApiModelProperty(value = "处理时长")
    @ExcelProperty(value = "处理时长")
    private Long handleTime;


}
