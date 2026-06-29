package cn.bctools.im.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import cn.bctools.im.entity.arangodb.ImUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: Arangodb数据库，user文档集数据处理
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
public interface ImUserRepository extends ArangoRepository<ImUser, String> {

    /**
     * 获取租户所有用户
     *
     * @param tenantId 租户id
     * @return
     */
    List<ImUser> findByTenantId(String tenantId);
}
