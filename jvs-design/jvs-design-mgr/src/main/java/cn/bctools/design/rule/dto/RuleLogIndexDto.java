package cn.bctools.design.rule.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class RuleLogIndexDto {

    String name;

    List<Index> index;

    @Data
    @Accessors(chain = true)
    public static class Index {
        String x;
        Number y;
    }

}
