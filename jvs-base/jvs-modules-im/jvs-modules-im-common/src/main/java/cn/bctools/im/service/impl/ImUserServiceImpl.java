package cn.bctools.im.service.impl;

import com.arangodb.springframework.core.ArangoOperations;
import cn.bctools.im.entity.arangodb.ImUser;
import cn.bctools.im.repository.ImUserRepository;
import cn.bctools.im.service.ImUserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description:  IM用户服务实现
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
@Service
@AllArgsConstructor
public class ImUserServiceImpl implements ImUserService {

    private final ImUserRepository imUserRepository;
    private final ArangoOperations arangoTemplate;

    @Override
    public List<ImUser> getAllByTenantId(String tenantId) {
        // 如果集合不存在，则自动创建。
        arangoTemplate.collection(ImUser.class);
        // 获取租户所有用户
        return imUserRepository.findByTenantId(tenantId);
    }

    @Override
    public void saveAll(List<ImUser> imUsers) {
        imUserRepository.saveAll(imUsers);
    }
}
