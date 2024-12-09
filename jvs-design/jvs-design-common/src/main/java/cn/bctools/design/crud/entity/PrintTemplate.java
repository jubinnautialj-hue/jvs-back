package cn.bctools.design.crud.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.crud.entity.enums.DesignTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 打印模板
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "jvs_print_template")
public class PrintTemplate extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "关联设计id")
    private String designId;

    @ApiModelProperty(value = "模板名称")
    private String name;

    @ApiModelProperty(value = "模板类型")
    private DesignTypeEnum designType;

    @ApiModelProperty(value = "模板设计JSON")
    private String design;

    @ApiModelProperty(value = "模板文件地址")
    private String fileUrl;

    @ApiModelProperty(value = "文件类型")
    private String fileType;

    @ApiModelProperty(value = "FALSE-禁用，TRUE-启用")
    private Boolean enableFlag;

    @ApiModelProperty("应用id")
    @TableField("jvs_app_id")
    private String jvsAppId;
}
