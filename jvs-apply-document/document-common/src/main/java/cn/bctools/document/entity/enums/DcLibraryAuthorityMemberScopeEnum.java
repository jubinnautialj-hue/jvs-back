package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库权限-用户范围
 */
@Getter
public enum DcLibraryAuthorityMemberScopeEnum {

    /**
     * 知识库权限-用户范围
     */
    registerAll("registerAll","企业所有用户"),
    user("user","用户"),
    ;
    @EnumValue
    public final String value;
    public final String desc;

    DcLibraryAuthorityMemberScopeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
