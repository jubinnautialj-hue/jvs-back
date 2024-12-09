package cn.bctools.design.rule.impl.resource;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.constant.RuleConstant;
import cn.bctools.rule.mapper.RuleOptionDao;
import cn.bctools.rule.po.RuleOptionPo;
import cn.bctools.rule.service.ResourceManagementInterface;
import cn.bctools.rule.service.ResourceType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * The type Resource management.
 *
 * @author jvs 保存节点操作后的资源
 */
@Slf4j
@Service
@AllArgsConstructor
public class ResourceManagementImpl implements ResourceManagementInterface {

    /**
     * The Redis utils.
     */
    RedisUtils redisUtils;
    /**
     * The Option dao.
     */
    RuleOptionDao optionDao;

    @Override
    public void saveNodeResource(String name, ResourceType type, String resourceId, Object map) {
        if (ObjectNull.isNotNull(name)) {
            optionDao.insert(new RuleOptionPo().setField(RuleConstant.DINGDINGRESOURCE_OPTION).setName(name).setMap(BeanCopyUtil.copy(map, HashMap.class)));
        }
    }
}
