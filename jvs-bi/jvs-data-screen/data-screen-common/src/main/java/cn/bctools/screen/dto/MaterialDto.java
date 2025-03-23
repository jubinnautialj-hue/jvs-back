package cn.bctools.screen.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
@ApiModel("素材管理")
public class MaterialDto {

    private Set<String> type;

    private List<MaterialTypeDto> materialMenu;
}
