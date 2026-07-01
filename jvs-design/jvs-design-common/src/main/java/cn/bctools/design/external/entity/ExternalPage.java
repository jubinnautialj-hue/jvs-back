package cn.bctools.design.external.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhuxiaokang
 * 外部页面配置
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_external_page", autoResultMap = true)
public class ExternalPage extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("唯一标识")
    private String id;

    @ApiModelProperty("页面名")
    @TableField("name")
    private String name;

    @ApiModelProperty("外部页面地址")
    @TableField("url")
    private String url;

    @ApiModelProperty("资源集")
    @TableField(value = "resources", typeHandler = Fastjson2TypeHandler.class)
    private List<JSONObject> resources;

    @ApiModelProperty("权限集")
    @TableField(value = "permissions", typeHandler = Fastjson2TypeHandler.class)
    private List<JSONObject> permissions;

    @ApiModelProperty("应用id")
    @TableField("jvs_app_id")
    private String jvsAppId;
}
