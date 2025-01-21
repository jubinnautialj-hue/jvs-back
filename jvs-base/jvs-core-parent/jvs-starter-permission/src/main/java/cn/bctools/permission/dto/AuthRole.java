package cn.bctools.permission.dto;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.permission.enums.AuthRolePersonType;
import cn.bctools.permission.enums.OperationType;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 权限
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("权限")
public class AuthRole implements Serializable {

    private static final long serialVersionUID = 1415967031470357512L;

    /**
     * 按钮 按钮目前就 两个 编辑(移动，设计) 删除
     */
    private List<OperationType> operation;
    /**
     * 人员
     */
    private List<PersonnelDto> personnels;

    /**
     * 人员ids
     */
    private List<String> userIds;

    /**
     * 权限类型
     */
    private Object personType;

    /**
     * 权限组名称
     */
    private String permissionName;
}
