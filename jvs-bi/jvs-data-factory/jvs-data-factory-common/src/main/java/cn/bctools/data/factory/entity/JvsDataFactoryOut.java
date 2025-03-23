package cn.bctools.data.factory.entity;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.data.factory.dto.DataSourceField;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据工厂输出模型字段
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
@TableName(value = "jvs_data_factory_out", autoResultMap = true)
@Data
@Accessors(chain = true)
@ApiModel("数据工厂输出模型字段")
public class JvsDataFactoryOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组件
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    /**
     * 输出表名称对应mongo模型
     */
    @ApiModelProperty("输出节点的名称")
    private String name;

    @ApiModelProperty("表名称")
    private String documentName;

    @ApiModelProperty("设计id")
    private String dataId;

    @ApiModelProperty("数据")
    @TableField(exist = false)
    private Page<Map<String, Object>> pageData;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("输出字段模型的所有字段的集")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<JSONObject> fields;

    public List<DataSourceField> getFieldList() {
        if (ObjectNull.isNull(this.getFields())) {
            return new ArrayList<>();
        }
        return JSONObject.parseArray(JSONObject.toJSONString(this.getFields()), DataSourceField.class);
    }

}
