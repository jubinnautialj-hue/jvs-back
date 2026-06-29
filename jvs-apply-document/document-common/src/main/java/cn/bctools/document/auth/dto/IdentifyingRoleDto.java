package cn.bctools.document.auth.dto;


import cn.bctools.document.entity.DcIdentifying;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("知识库权限标识")
public class IdentifyingRoleDto {
    /**
     * 文档id
     */
    private String dcId;
    /**
     * 权限集合
     */
    private List<DcIdentifying> identifyingList;
}
