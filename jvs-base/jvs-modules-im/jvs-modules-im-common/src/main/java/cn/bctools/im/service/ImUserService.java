package cn.bctools.im.service;

import cn.bctools.im.entity.arangodb.ImUser;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM用户服务
 */
public interface ImUserService {

    /**
     * 获取租户所有用户
     * @param tenantId
     * @return
     */
    List<ImUser> getAllByTenantId(String tenantId);

    /**
     * 批量保存
     * @param imUsers
     */
    void saveAll(List<ImUser> imUsers);
}
