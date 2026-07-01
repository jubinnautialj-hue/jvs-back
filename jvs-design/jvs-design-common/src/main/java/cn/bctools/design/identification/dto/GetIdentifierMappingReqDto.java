package cn.bctools.design.identification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("获取标识映射入参")
public class GetIdentifierMappingReqDto {

    @ApiModelProperty("标识集合")
    private List<String> identifiers;
}
