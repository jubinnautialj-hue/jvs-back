package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Administrator
 */

@Getter
@AllArgsConstructor
public enum SourceFileUdOperateTypeEnums {
    down("down", "下载"),
    up("up", "上传");

    @EnumValue
    String value;
    String desc;
}
