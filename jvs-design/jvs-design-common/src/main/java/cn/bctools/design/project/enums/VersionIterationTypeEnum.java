package cn.bctools.design.project.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 应用版本迭代操作类型
 */
@Getter
@AllArgsConstructor
public enum VersionIterationTypeEnum {
    /**
     * 切换版本（版本回退）
     */
    SWITCH_VERSION,
    /**
     * 提交版本（跨版本类型。如：开发 -> 测试）
     */
    SUBMIT_VERSION,
    /**
     * 上传版本离线包
     */
    UPLOAD_VERSION,
    ;
}
