package cn.bctools.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ERangeType {

    ALL(3),
    COLUMN(2),
    NORMAL(0),
    ROW(1),
    ;

    private final int type;

    public boolean equals(int type){
        return this.getType()==type;
    }

}
