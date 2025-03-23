package cn.bctools.data.factory.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author admin
 * 系统设置的类型
 */

@Getter
@AllArgsConstructor
public enum SysSettingDataTypeEnums {

    consanguinityAnalyseJob("consanguinityAnalyseJob", "血缘视图定时任务"),
    checkSeaTunnelTaskJob("checkSeaTunnelTaskJob", "seaTunnel实时任务校验");
    @EnumValue
    private final String value;
    private final String desc;

}
