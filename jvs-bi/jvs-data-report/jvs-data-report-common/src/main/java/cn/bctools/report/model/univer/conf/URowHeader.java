package cn.bctools.report.model.univer.conf;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("行标题配置")
public class URowHeader {

    private Integer width;

    private Boolean hidden;
}
