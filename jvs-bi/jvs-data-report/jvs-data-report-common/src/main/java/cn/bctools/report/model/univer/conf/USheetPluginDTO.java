package cn.bctools.report.model.univer.conf;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class USheetPluginDTO {

    private String name;

    private String data;
}
