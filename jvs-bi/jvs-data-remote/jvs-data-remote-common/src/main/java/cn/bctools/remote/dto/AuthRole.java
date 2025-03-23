package cn.bctools.remote.dto;


import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.remote.enums.AuthRolePersonType;
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
public class AuthRole<T> {
    /**
     * 权限标识
     */
    private AuthRolePersonType personType;
    /**
     * 按钮 按钮目前就 两个 编辑(移动，设计) 删除
     */
    private List<T> operation;
    /**
     * 人员
     */
    private List<PersonnelDto> personnels;
}
