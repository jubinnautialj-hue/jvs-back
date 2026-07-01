package cn.bctools.design.rule.swagger;

import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs
 * API组名枚举
 * <p>
 *    配置swagger上展示的组名
 */
@Getter
@AllArgsConstructor
public enum SwaggerApiGroupEnum {
    DEV("开发模式", AppVersionTypeEnum.DEV.getValue()),
    BETA( "测试模式", AppVersionTypeEnum.BETA.getValue()),
    GA("正式模式", AppVersionTypeEnum.GA.getValue()),
    ;
    // API组名
    private final String groupName;
    // 模式
    private final String mode;

    public static String getGroupNameByMode(String mode) {
        for (SwaggerApiGroupEnum value : SwaggerApiGroupEnum.values()) {
            if (value.getMode().equals(mode)) {
                return value.getGroupName();
            }
        }
        return null;
    }
}
