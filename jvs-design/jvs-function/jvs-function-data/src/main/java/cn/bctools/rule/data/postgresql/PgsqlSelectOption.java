package cn.bctools.rule.data.postgresql;


import cn.bctools.rule.annotations.SelectOptionField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 公共的连接信息 mysql,mongodb
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class PgsqlSelectOption {

    @ApiModelProperty("ip")
    @SelectOptionField("ip")
    public String sourceHost;

    @SelectOptionField("端口")
    public Integer sourcePort;

    @SelectOptionField("数据库")
    public String sourceLibraryName;

    @SelectOptionField("schema")
    public String schema;

    @SelectOptionField("登录用户名")
    public String sourceUserName;

    @SelectOptionField("登录密码")
    public String sourcePwd;
}
