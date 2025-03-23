package cn.bctools.design.data.entity;

import cn.bctools.database.entity.po.BasePo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 动态数据
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DynamicDataPo extends BasePo {

    @ApiModelProperty("数据id")
    private String id;

    @ApiModelProperty("数据模型id")
    private String modelId;

    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("数据内容(Json格式)")
    private Map<String, Object> jsonData;

    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

}
