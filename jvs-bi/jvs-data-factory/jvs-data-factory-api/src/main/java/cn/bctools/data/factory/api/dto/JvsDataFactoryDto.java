package cn.bctools.data.factory.api.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 数据etl
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("数据etl dto")
@Accessors(chain = true)
public class JvsDataFactoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private String id;

    /**
     * 设计
     */
    @ApiModelProperty("设计")
    private String name;

    /**
     * 优先级
     */
    @ApiModelProperty("优先级")
    private Integer priority;

    /**
     * 渲染json
     */
    @ApiModelProperty("渲染json")
    private String viewJson;

    /**
     * 表单描述
     */
    @ApiModelProperty("描述")
    private String description;
    /**
     * 租户id
     */
    private String tenantId;

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("权限设置")
    private List<JSONObject> role;

    @ApiModelProperty("数据源的名称")
    private String documentName;

    @ApiModelProperty("输出的mongoId")
    private String collectionId;


}