package cn.bctools.data.factory.html.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankRuleEnum {
    continuousAndRepetition("continuousAndRepetition","连续且重复的","dense_rank()"),
    continuousAndWithoutRepetition("continuousAndWithoutRepetition","连续且不重复","row_number()"),
    discontinuous("discontinuous","非连续的","rank()");
    private final String value;
    private final String desc;
    private final String dorisFunction;
}
