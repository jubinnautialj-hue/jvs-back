package cn.bctools.report.model.univer.conf;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("列标题配置")
public class UColumnHeader {

    private Integer height;

    private Boolean hidden;
}
