package cn.bctools.design.project.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum App template task progress detail enum.
 *
 * @author hrl
 */
@Getter
@AllArgsConstructor
public enum AppTemplateTaskProgressDetailEnum {
    /**
     * Summary app template task progress detail enum.
     */
    SUMMARY("", 10, false),
    /**
     * Check app template task progress detail enum.
     */
    CHECK("校验", 11, false),
    /**
     * Unified permission app template task progress detail enum.
     */
    UNIFIED_PERMISSION("权限升级", 12, false),
    /**
     * Pack app template task progress detail enum.
     */
    PACK("打包", 20, false),
    /**
     * Prepare app template task progress detail enum.
     */
    PREPARE("准备创建应用或版本迭代", 30, false),
    /**
     * Decrypt data app template task progress detail enum.
     */
    DECRYPT_DATA("解密模板内容", 100, true),
    /**
     * Analysis app app template task progress detail enum.
     */
    ANALYSIS_APP("解析应用信息", 110, true),
    /**
     * Analysis data app template task progress detail enum.
     */
    ANALYSIS_DATA("解析模板数据", 120, true),
    /**
     * Form app template task progress detail enum.
     */
    FORM("表单", 130, true),
    /**
     * Page app template task progress detail enum.
     */
    PAGE("列表", 140, true),
    /**
     * Data model app template task progress detail enum.
     */
    DATA_MODEL("数据模型", 150, true),
    /**
     * Data model field app template task progress detail enum.
     */
    DATA_MODEL_FIELD("模型字段", 160, true),
    /**
     * Rule app template task progress detail enum.
     */
    RULE("逻辑设计", 170, true),
    /**
     * Flow app template task progress detail enum.
     */
    FLOW("流程设计", 180, true),
    /**
     * App url app template task progress detail enum.
     */
    APP_URL("自定义页面", 190, true),
    /**
     * Data notice app template task progress detail enum.
     */
    DATA_NOTICE("消息通知", 200, true),
    /**
     * Menu app template task progress detail enum.
     */
    MENU("菜单", 210, true),
    /**
     * Data app template task progress detail enum.
     */
    DATA("模型数据", 220, true),
    /**
     * App version app template task progress detail enum.
     */
    APP_VERSION("应用版本", 240, true),
    /**
     * End app template task progress detail enum.
     */
    END("完成", 1000, false),
    ;

    /**
     * 默认内容
     */
    private final String defaultContent;
    /**
     * 默认序号
     */
    private final Integer serialNumber;
    /**
     * 模板创建应用或版本迭代初始化步骤：true-初始化，false-非初始化
     */
    private final Boolean createOrIterationInit;

    /**
     * 获取用于模板创建应用或版本迭代初始化步骤的枚举
     *
     * @return 用于初始化的枚举集合 create or iteration init enum
     */
    public static List<AppTemplateTaskProgressDetailEnum> getCreateOrIterationInitEnum() {
        return Arrays.stream(AppTemplateTaskProgressDetailEnum.values())
                .filter(AppTemplateTaskProgressDetailEnum::getCreateOrIterationInit)
                .collect(Collectors.toList());
    }
}
