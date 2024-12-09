package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 应用模板
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("应用模板列表")
public class AppTemplateListDto {

    @ApiModelProperty("本地模板")
    private List<AppTemplateDto> localList = Collections.emptyList();

}
