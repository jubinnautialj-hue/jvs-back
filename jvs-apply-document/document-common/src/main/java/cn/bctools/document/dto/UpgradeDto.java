package cn.bctools.document.dto;


import com.alibaba.fastjson2.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 版本升级时历史数据库的数据结构
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("获取目录树包括权限")
@AllArgsConstructor
@NoArgsConstructor
public class UpgradeDto {
    /**
     * 角色id
     */
    @JSONField(name = "role_id")
    private String roleId;
    /**
     * 用户id
     */
    @JSONField(name = "user_id")
    private String userId;
    /**
     * 文档id
     */
    @JSONField(name = "dc_id")
    private String dcId;
}
