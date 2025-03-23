package cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobInfoStatusEnums {
    RUNNING("RUNNING", "运行中"),
    FINISHED("FINISHED", "完成"),
    CANCEL("CANCEL", "取消");
    @EnumValue
    private final String value;
    private final String desc;
}
