package cn.bctools.design.project.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author hrl
 */
@Getter
@Setter
@ApiModel("应用模板创建或迭代应用任务进度")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("jvs_app_template_task_progress")
public class JvsAppTemplateTaskProgress extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("任务概要")
    private String summary;
    @ApiModelProperty("任务状态")
    private AppTemplateTaskProgressEnum progress;
    @ApiModelProperty("应用id")
    private String jvsAppId;
    
    private String tenantId;
}
