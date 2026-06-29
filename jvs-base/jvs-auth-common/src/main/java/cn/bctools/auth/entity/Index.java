package cn.bctools.auth.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs  The type Index.
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_index", autoResultMap = true)
public class Index implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty("用户Id")
    private String userd;
    @ApiModelProperty("设计")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private String viewJson;
}
