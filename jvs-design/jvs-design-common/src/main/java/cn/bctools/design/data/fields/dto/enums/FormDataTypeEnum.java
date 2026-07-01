package cn.bctools.design.data.fields.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单组件数据类型
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum FormDataTypeEnum {

    /**
     * 排序类型
     */
    option("配置数据"),
    dataModel("数据模型"),
    url("接口数据"),
    rule("逻辑引擎"),
    system("系统字典"),
    flowable("工作流"),
    dom("动态组件"),
    ;

    private final String desc;

}
