package cn.bctools.report.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel("排序方式")
@Getter
@AllArgsConstructor
public enum ESortType {
    NONE("默认"),
    ASC("升序"),
    DESC("降序"),
    ;
    private final String desc;
}
