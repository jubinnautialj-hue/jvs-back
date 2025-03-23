package cn.bctools.data.factory.source.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum  ExcelUpdateType {

    append("append","追加"),
    overwrite("overwrite","覆盖"),
    ;

    private final String code;
    private final String desc;

}
