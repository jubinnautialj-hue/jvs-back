package cn.bctools.chart.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CopyDto {

    private String menuId;

    private String name;

    private String reportId;
}
