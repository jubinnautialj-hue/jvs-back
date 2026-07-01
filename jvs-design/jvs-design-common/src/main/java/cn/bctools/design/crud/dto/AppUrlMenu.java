package cn.bctools.design.crud.dto;

import cn.bctools.design.menu.entity.AppMenu;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AppUrlMenu extends AppMenu {
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("页面链接")
    private String url;
}
