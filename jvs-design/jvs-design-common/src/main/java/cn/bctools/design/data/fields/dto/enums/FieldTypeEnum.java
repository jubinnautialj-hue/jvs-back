package cn.bctools.design.data.fields.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static cn.bctools.design.data.fields.dto.enums.QueryTypeEnum.*;

/**
 * <DB-JAVA-类型映射关系>
 *
 * @author auto
 **/
@Getter
@AllArgsConstructor
public enum FieldTypeEnum {

    /**
     * 字段显示类型
     */
    field_text(1, "文本", "这是一段文字", new QueryTypeEnum[]{query_like, query_left_like, query_right_like, query_eq, query_ne}, false, true, false, ComponentTypeEnum.input, null, true, "varchar"),
    field_numeral(2, "数字", "整数：10,浮点数：12.88", new QueryTypeEnum[]{query_eq, query_le, query_ge, query_gt, query_lt, query_ne}, true, true, true, ComponentTypeEnum.inputNumber, null, true, "decimal"),
    field_time(3, "时间", "2020-01-01", new QueryTypeEnum[]{query_eq, query_le, query_ge, query_gt, query_lt, query_ne}, true, true, true, ComponentTypeEnum.input, null, true, "varchar"),
    //field_user(3, "用户", "", new QueryTypeEnum[]{query_eq, query_ne, query_in}, false, true, false, ComponentTypeEnum.user, null, true, "int"),
    //field_role(4, "角色", "", new QueryTypeEnum[]{query_eq, query_ne, query_in}, false, true, false, ComponentTypeEnum.role, null, true, "int"),
    //field_department(5, "部门", "", new QueryTypeEnum[]{query_eq, query_ne, query_in}, false, true, false, ComponentTypeEnum.department, null, true, "int"),
    //field_post(6, "岗位", "", new QueryTypeEnum[]{query_eq, query_ne, query_in}, false, true, false, ComponentTypeEnum.post, null, true, "int"),
    field_dictionary(9, "字典", "", new QueryTypeEnum[]{query_eq, query_ne, query_in}, false, true, false, ComponentTypeEnum.select, null, true, "varchar"),
    field_image(10, "图片", "", null, false, true, false, ComponentTypeEnum.image, null, true, "varchar"),
    field_link(11, "链接", "", null, false, true, false, ComponentTypeEnum.link, null, true, "varchar"),
    ;

    /**
     * 排序
     */
    private final int order;
    /**
     * 描述
     */
    private final String description;
    /**
     * 示例
     */
    private final String example;
    /**
     * 支持查询条件类型
     */
    private final QueryTypeEnum[] supportQueryType;
    /**
     * 是否支持排序
     */
    private final Boolean supportSort;
    /**
     * 是否支持高级设置
     */
    private final Boolean supportSettings;
    /**
     * 是否支持统计
     */
    private final Boolean supportStatistics;
    /**
     * 对应组件类型
     */
    private final ComponentTypeEnum componentType;
    /**
     * 额外补充
     */
    private final Object extra;
    /**
     * 是否支持显示
     */
    private final Boolean supportShow;
    /**
     * 对应的DB类型
     */
    private final String dbType;
}
