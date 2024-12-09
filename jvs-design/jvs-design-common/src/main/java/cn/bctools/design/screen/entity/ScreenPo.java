package cn.bctools.design.screen.entity;


import cn.bctools.database.entity.po.BasePo;
import com.baomidou.mybatisplus.annotation.*;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_screen", autoResultMap = true)
@ApiModel
public class ScreenPo extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty("数据id")
    private String id;

    @ApiModelProperty("应用id")
    private String jvsAppId;

    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("部署的IP地址")
    private String ip;
    @ApiModelProperty("部署的code码")
    private String code;


    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

    /**
     * width - 大屏宽度
     */

    private Integer width;

    /**
     * height - 大屏高度
     */

    private Integer height;

    /**
     * img_url - 背景图
     */

    private String imgUrl;

    /**
     * background - 背景色
     */

    private String background;

    /**
     * 组件数据信息
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List components;


    @TableField(exist = false)
    @ApiModelProperty("大屏部署的随机请求头key和value，避免冲突和破解")
    private Map<String, String> randomRequestHeader;

    @TableField(exist = false)
    @ApiModelProperty("分类")
    private String type;
}
