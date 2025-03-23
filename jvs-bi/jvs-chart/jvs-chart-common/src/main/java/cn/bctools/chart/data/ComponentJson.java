package cn.bctools.chart.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ComponentJson {
    private String type;
    private String filterJson;
    private String xKey;

}
