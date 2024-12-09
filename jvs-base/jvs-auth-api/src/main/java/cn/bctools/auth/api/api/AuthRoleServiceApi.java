package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 角色信息查询
 *
 * @author: GuoZi
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "role")
public interface AuthRoleServiceApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/role";

    /**
     * 查询所有角色
     *
     * @return 角色信息集合 all
     */
    @GetMapping(PREFIX + "/query/all")
    R<List<SysRoleDto>> getAll();

    /**
     * 根据角色id查询单个角色
     *
     * @param roleId 角色id
     * @return 角色信息 by id
     */
    @GetMapping(PREFIX + "/query/by/id/{roleId}")
    R<SysRoleDto> getById(@ApiParam("角色id") @PathVariable("roleId") String roleId);

    /**
     * 组某一个角色设置一批用户
     *
     * @param roleId  角色id
     * @param userIds 用户数组id
     * @return 默认返回为成功状态, 异常由feign接口
     * @throws Exception 保存角色时,可能会存在异常
     */
    @PostMapping(PREFIX + "/set/role/users/{roleId}")
    R setUser(@ApiParam("角色id") @PathVariable("roleId") String roleId, @RequestBody List<String> userIds) throws Exception;

    /**
     * 根据角色id集合查询角色集合
     * <p>
     * 返回集合的数量、顺序不能保证与入参一致
     *
     * @param roleIds 角色id集合
     * @return 角色信息集合 by ids
     */
    @PostMapping(PREFIX + "/query/by/ids")
    R<List<SysRoleDto>> getByIds(@ApiParam("角色id集合") @RequestBody List<String> roleIds);

}
