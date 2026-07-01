package cn.bctools.design.project.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author zhuxiaokang
 * 应用操作的类型日志
 */
@Getter
@AllArgsConstructor
public enum AppLogTypeEnum {

    /**
     * 新增
     */
    add("新增"),
    /**
     * 修改
     */
    update("修改"),
    /**
     * 删除
     */
    remove("删除"),
    /**
     * 其它
     */
    other("其它"),
    ;

    @EnumValue
    @JsonValue
    private String value;

}
