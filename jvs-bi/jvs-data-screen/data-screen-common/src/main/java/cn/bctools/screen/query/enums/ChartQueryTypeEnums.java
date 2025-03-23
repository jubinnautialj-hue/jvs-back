package cn.bctools.screen.query.enums;

import cn.bctools.screen.query.ChartQueryTypeValue;
import cn.bctools.screen.query.impl.ChartQueryTypeValueInput;
import cn.bctools.screen.query.impl.ChartQueryTypeValueUserId;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ChartQueryTypeEnums {
    input("input", "用户输入的值", ChartQueryTypeValueInput.class),
    userId("userId", "当前登录的用户id", ChartQueryTypeValueUserId.class),
    roleId("roleId", "当前登录的用户角色id", ChartQueryTypeValueUserId.class),
    deptId("deptId", "当前登录的用户部门id", ChartQueryTypeValueUserId.class);
    @EnumValue
    String value;
    String desc;
    Class<? extends ChartQueryTypeValue> cls;
}
