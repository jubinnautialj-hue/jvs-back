package cn.bctools.design.rule.service.impl;

import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.mapper.RuleDesignDao;
import cn.bctools.design.rule.service.RuleDescribeService;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class RuleDesignServiceImpl extends ServiceImpl<RuleDesignDao, RuleDesignPo> implements RuleDesignService, IJvsDesigner {

    RuleDescribeService deleteRuleDescribe;
    MapperMethodHandler mapperMethodHandler;
    RedisUtils redisUtils;
    OssTemplate ossTemplate;

    @Override
    public RuleDesignPo getEnableDesign(String secret) {
        return getOne(Wrappers.query(new RuleDesignPo().setSecret(secret)));
    }

    @Override
    public Map<? extends String, ? extends List<String>> ruleFlows(String jvsAppId) {
        return list(new LambdaQueryWrapper<RuleDesignPo>()
                .select(RuleDesignPo::getSecret, RuleDesignPo::getFlowDesignIds, RuleDesignPo::getJvsAppId)
                .eq(RuleDesignPo::getJvsAppId, jvsAppId).isNotNull(RuleDesignPo::getFlowDesignIds))
                .stream()
                .collect(Collectors.toMap(RuleDesignPo::getSecret, RuleDesignPo::getFlowDesignIds));
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<RuleDesignPo>lambdaQuery().eq(RuleDesignPo::getJvsAppId, appId));
    }

    @Override
    public void delete(String appId, String designId) {
        remove(Wrappers.query(new RuleDesignPo().setJvsAppId(appId).setId(designId)));
        deleteRuleDescribe.deleteRuleDescribe(designId, appId);
    }

}
