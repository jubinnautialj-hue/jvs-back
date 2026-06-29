package cn.bctools.document.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataAuthTypeEnum {
    user("user", "个人"),
    dept("dept", "部门"),
    job("job", "岗位"),
    role("role", "角色"),
    group("group", "群组");
    @EnumValue
    public final String value;
    public final String desc;
}
