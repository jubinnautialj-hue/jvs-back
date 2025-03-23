package cn.bctools.document.dto;


import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("新增权限与返回权限的格式")
public class AddAuthConfigDto {
    @ApiModelProperty("用户信息")
    private List<UserInfo> userInfoList;
    @ApiModelProperty("权限标识")
    private List<IdentifyingAuthDto> idcIdentifying;

    @ApiModel("用户信息")
    @Data
    @Accessors(chain = true)
    public static class UserInfo {
        //这里重写 hashcode与equals 是因为部分地方用了去重操作
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UserInfo userInfo = (UserInfo) o;
            return userId.equals(userInfo.userId) && dataAuthType == userInfo.dataAuthType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, dataAuthType);
        }

        @ApiModelProperty("用户id，系统角色id，部门id，群组id，岗位id")
        private String userId;
        @ApiModelProperty("头像")
        private String headImg;
        @ApiModelProperty("名称")
        private String name;
        @ApiModelProperty("数据权限类型")
        private DataAuthTypeEnum dataAuthType;
    }

    /**
     * 用于判断是否可以入库
     */
    public boolean isNotEmpty() {
        return !userInfoList.isEmpty();
    }
}


