package cn.bctools.report.model.univer;

import cn.bctools.report.model.univer.conf.USheetPluginDTO;
import cn.bctools.report.model.univer.style.UStyle;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@ApiModel("univer 工作簿")
public class UWorkbook implements Serializable {

    private static final long serialVersionUID = 4912892412268469277L;

    @ApiModelProperty("Univer Sheets 的唯一标识符")
    private String id;

    @ApiModelProperty("Univer Sheets 的名称")
    private String name;

    @ApiModelProperty("Univer 模型定义的版本")
    private String appVersion;

    @ApiModelProperty("文档的语言环境")
    private LocaleType locale;

    @ApiModelProperty("工作簿的样式引用")
    private Map<String, UStyle> styles;

    @ApiModelProperty("表示工作表顺序的工作表 ID 数组")
    private List<String> sheetOrder;

    @ApiModelProperty(value = "包含每个工作表数据的记录",notes = "Map<String,USheet> ")
    private Map<String,USheet> sheets;

    @ApiModelProperty(value = "高亮设置",notes = "Map<String,Object>")
    private Map<String,Object> highlightConf;

    @ApiModelProperty("其他插件的可选资源")
    private List<USheetPluginDTO> resources;

    public enum LocaleType{
        zhCN,
        enUS,
        ruRU,
        zhTW,
        viVN,
        faIR,
        ;
    }

}
