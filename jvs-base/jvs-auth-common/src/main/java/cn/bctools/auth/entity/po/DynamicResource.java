package cn.bctools.auth.entity.po;

import cn.bctools.common.enums.ConfigsTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DynamicResource implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 类型
     */
    ConfigsTypeEnum type;
    /**
     * 名称
     */
    String name;
    /**
     * 图标地址
     */
    String iconUrl;
    /**
     * 终端id
     */
    String clientId;
    /**
     * 跳转地址，默认根据资源获取的配置地址
     */
    String url;
    /**
     * 描述
     */
    String desc;
}
