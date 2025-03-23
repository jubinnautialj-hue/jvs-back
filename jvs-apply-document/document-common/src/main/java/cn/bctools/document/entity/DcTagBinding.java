package cn.bctools.document.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("dc_tag_binding")
public class DcTagBinding implements Serializable {

    @ApiModelProperty("标签名_id")
    @TableField("tag_id")
    private String tagId;

    @ApiModelProperty("文库id")
    @TableField("dc_id")
    private String dcId;

}
