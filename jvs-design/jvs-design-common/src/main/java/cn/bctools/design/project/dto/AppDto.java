package cn.bctools.design.project.dto;

import cn.bctools.design.project.entity.JvsApp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 应用信息dto
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("应用信息")
public class AppDto extends JvsApp {

    @ApiModelProperty(value = "应用权限名集合", notes = "用户在当前应用中的角色")
    private List<String> appRoles = new ArrayList<>();

}
