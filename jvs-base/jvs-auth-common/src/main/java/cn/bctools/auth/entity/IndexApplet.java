package cn.bctools.auth.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author czy
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_index_applet", autoResultMap = true)
public class IndexApplet implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List appletJson;

}
