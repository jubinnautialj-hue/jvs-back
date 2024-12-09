package cn.bctools.auth.entity;

import cn.bctools.auth.api.enums.EnvironmentVariableEnum;
import cn.bctools.auth.api.enums.ModeTypeEnum;
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
 * @author jvs 环境变量
 * 在轻应用中存在开发模式、测试模式、正式模式，切换不同模式时获取的环境变量将不一样。
 * 环境变量通常在公式中获取变量时使用
 */
@Data
@Accessors(chain = true)
@TableName(value = "sys_environment_variable", autoResultMap = true)
public class EnvironmentVariable implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键")
    private String id;
    @ApiModelProperty(value = "模式", notes = "默认获取时只获取正式的配置，其它情况获取模式相关的配置信息,当执行时需要判断是哪一个模式，再去获取对应的数据信息")
    private ModeTypeEnum mode;
    @ApiModelProperty("类型")
    private EnvironmentVariableEnum type;
    @ApiModelProperty("键")
    private String label;
    @ApiModelProperty("值,有可能是文件，也有可能是map对象")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private Object value;
    @ApiModelProperty("说明")
    private String remark;

}
