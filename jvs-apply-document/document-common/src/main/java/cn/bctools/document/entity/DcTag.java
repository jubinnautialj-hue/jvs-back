package cn.bctools.document.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author:
 * @Description: 标签
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("知识库-标签")
@EqualsAndHashCode(callSuper = false)
@TableName("dc_tag")
public class DcTag extends BasalPo implements Serializable {

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("标签名")
    @TableField("tag_name")
    private String tagName;

    @ApiModelProperty("标签颜色")
    private String colour;


    @ApiModelProperty("知识库id")
    @TableField(exist = false)
    private String dcId;

    @ApiModelProperty("是否选中")
    @TableField(exist = false)
    private Boolean isSelect;

}
