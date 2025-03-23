package cn.bctools.data.factory.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author guojing
 * @describe 定时任务逻辑
 */
@Data
@Accessors(chain = true)
@ApiModel("定时任务逻辑")
public class TaskCronDto implements Serializable {
    /**
     * 显示忽略这个地方，是XXl-job执行的ID值
     */
    private Integer id;

    @ApiModelProperty("是否有定时任务")
    private Boolean onTask;

    @ApiModelProperty("第一次开始时间，默认为当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty("定时任务负责人邮箱")
    private String alarmEmail;

    @ApiModelProperty("定时任务负责人")
    private String author;

    /**
     * 保存定时任务的时候，自动生成表达式结果
     */
    private String cron;

}
