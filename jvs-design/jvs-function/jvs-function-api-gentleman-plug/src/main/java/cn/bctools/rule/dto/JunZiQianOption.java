package cn.bctools.rule.dto;

import cn.bctools.rule.annotations.SelectOption;
import cn.bctools.rule.annotations.SelectOptionField;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The type Jun zi qian option.
 * 君子签的数据配置信息，相同的配置信息后续将移到环境变量中获取数据
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
@SelectOption("君子签")
public class JunZiQianOption {

    /**
     * The Name.
     */
    @SelectOptionField("名称")
    public String name;
    /**
     * The Service url.
     */
    @SelectOptionField("serviceUrl")
    public String serviceUrl;
    /**
     * The App key.
     */
    @SelectOptionField("appKey")
    public String appKey;
    /**
     * The App secret.
     */
    @SelectOptionField("appSecret")
    public String appSecret;

}
