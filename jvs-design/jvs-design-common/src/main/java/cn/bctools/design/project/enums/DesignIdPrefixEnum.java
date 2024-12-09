package cn.bctools.design.project.enums;

import cn.bctools.design.data.fields.enums.DesignType;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ReUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhuxiaokang
 * 设计id前缀
 */
@Getter
@AllArgsConstructor
public enum DesignIdPrefixEnum {
    /**
     * 表单id前缀
     */
    FORM(HexUtil.encodeHexStr("F_"), DesignType.form),

    /**
     * 列表id前缀
     */
    PAGE(HexUtil.encodeHexStr("P_"), DesignType.page),

    /**
     * 工作流id前缀
     */
    FLOW(HexUtil.encodeHexStr("WF_"), DesignType.workflow),

    /**
     * 逻辑引擎id前缀
     */
    RULE(HexUtil.encodeHexStr("R_"), DesignType.rule),

    /**
     * 数据模型id前缀
     */
    DATA_MODEL(HexUtil.encodeHexStr("DM_"), DesignType.data),
    ;

    /**
     * 前缀（16进制字符串）
     */
    private final String prefix;

    /**
     * 设计类型
     */
    private final DesignType designType;

    /**
     * 解析字符串得到设计id
     *
     * @param prefixEnum 设计id前缀枚举
     * @param design     设计
     * @return 匹配的设计id集合
     */
    public static List<String> findAll(DesignIdPrefixEnum prefixEnum, String design) {
        String regex = "(?<=\")" + prefixEnum.getPrefix() + ".*?(?=\")";
        return ReUtil.findAll(regex, design, 0, new ArrayList<>());
    }

    /**
     * 解析设计id，获取设计类型
     *
     * @param designId 设计id
     * @return 设计类型
     */
    public static DesignType getDesignTypeById(String designId) {
        return Arrays.stream(DesignIdPrefixEnum.values())
                .filter(e -> designId.startsWith(e.getPrefix()))
                .findAny()
                .map(DesignIdPrefixEnum::getDesignType)
                .orElse(DesignType.other);
    }

    /**
     * 根据设计类型获取id前缀
     *
     * @param designType 设计类型
     * @return 设计id前缀
     */
    public static String getPrefix(DesignType designType) {
        return Arrays.stream(DesignIdPrefixEnum.values())
                .filter(e -> e.getDesignType().equals(designType))
                .findAny()
                .map(DesignIdPrefixEnum::getPrefix)
                .orElseGet(() -> "");
    }
}
