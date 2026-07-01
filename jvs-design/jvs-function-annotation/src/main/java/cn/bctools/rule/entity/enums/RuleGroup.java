package cn.bctools.rule.entity.enums;

import cn.hutool.core.lang.Dict;
import lombok.Getter;

/**
 * 逻辑节点系统默认分组
 * 逻辑可以通过界面的方式进行扩展分组可以自定义或选择系统的默认分组
 *
 * @author guojing
 */
@Getter
public enum RuleGroup {

    /**
     * 常用插件
     */
    常用插件("common", ""),
    /**
     * 模型插件
     */
    模型插件("dataModel", ""),
    /**
     * 数据插件
     */
    数据插件("data", ""),
    /**
     * 工具插件
     */
    工具插件("tools", ""),
    /**
     * 钉钉平台
     */
    钉钉平台("dingding", ""),
    /**
     * 识别插件
     */
    识别插件("discriminate", ""),
    /**
     * 加密插件
     */
    加密插件("encryption", ""),
    /**
     * 服务插件
     */
    服务插件("api", ""),
    /**
     * 腾讯电子签
     */
    腾讯电子签("tencent", ""),
    /**
     * 君子签
     */
    君子签("gentleman_signature", ""),
    /**
     * 机器翻译
     */
    机器翻译("tencent_translation", ""),
    /**
     * 阿里云市场
     */
    阿里云市场("aliyuncloud", "https://market.aliyun.com/", true),
    /**
     * 天眼查
     */
    天眼查("tianyancha", "https://open.tianyancha.com/", true),
    /**
     * 企查查
     */
    企查查("qcc", "https://openapi.qcc.com/", true),
    /**
     * 接口扩展
     */
    接口扩展("business", "");

    private final String msg;
    private final String url;
    /**
     * 是否是扩展  如果 为true 需要添加一个 注解为：@SelectOption 值为msg的对象属性声明
     */
    private boolean external = false;

    RuleGroup(String msg, String url) {
        this.url = url;
        this.msg = msg;
    }

    RuleGroup(String msg, String url, boolean external) {
        this.url = url;
        this.msg = msg;
        this.external = external;
    }

    public Dict getDict() {
        return new Dict()
                .set("msg", this.name())
                .set("url", this.getUrl())
                .set("name", this.name());
    }

}
