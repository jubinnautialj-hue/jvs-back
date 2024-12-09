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
public class CrudFormMenu  extends AppMenu {

    @ApiModelProperty("数据模型ID")
    private String dataModelId;
}
