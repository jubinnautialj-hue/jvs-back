package cn.bctools.data.factory.source.dto;


import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.data.factory.source.enums.AuthRolePersonType;
import cn.bctools.data.factory.source.enums.OperationEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 权限
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("权限")
public class AuthRole {
    /**
     * 权限标识
     */
    private AuthRolePersonType personType;
    /**
     * 按钮 按钮目前就 两个 编辑(移动，设计) 删除
     */
    private List<OperationEnum> operation;
    /**
     * 人员
     */
    private List<PersonnelDto> personnels;
}
