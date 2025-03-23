package cn.bctools.rule.po;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 自定义选择项的设置数据
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@TableName(value = "jvs_rule_option", autoResultMap = true)
public class RuleOptionPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    public String id;
    @ApiModelProperty("别名")
    public String name;
    @ApiModelProperty("字段名-后台自动设置，(方法名+参数名+字段名进行拼接)")
    public String field;
    @ApiModelProperty("传递对象值")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    public Map<String, Object> map;

}
