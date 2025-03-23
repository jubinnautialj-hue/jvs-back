package cn.bctools.remote.po;

import cn.bctools.database.entity.po.BasalPo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@Slf4j
public class RolePo extends BasalPo implements Serializable {
    @ApiModelProperty("权限设置")
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<JSONObject> role;
    @ApiModelProperty("权限类型是否为自定义权限")
    Boolean roleType;
    @TableField(exist = false)
    @ApiModelProperty("操作权限")
    List<Object> operationList;
}
