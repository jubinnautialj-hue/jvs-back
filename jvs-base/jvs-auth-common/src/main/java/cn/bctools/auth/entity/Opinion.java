package cn.bctools.auth.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 意见反馈
 * </p>
 *
 * @author guojing
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "sys_opinion", autoResultMap = true)
@ApiModel("意见反馈")
public class Opinion implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String text;
    @TableField(value = "img", typeHandler = Fastjson2TypeHandler.class)
    private List<String> img;
    private String createBy;
    private String createById;
    private String createByHeadImg;
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;


}
