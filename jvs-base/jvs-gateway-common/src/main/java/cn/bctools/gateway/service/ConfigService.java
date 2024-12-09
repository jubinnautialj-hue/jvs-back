package cn.bctools.gateway.service;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.gateway.config.JvsSystemConfig;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.gateway.mapper.ConfigMapper;
import cn.bctools.gateway.mapper.TenantMapper;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xh
 */
@Slf4j
@Service
@AllArgsConstructor
public class ConfigService {

    ConfigMapper configMapper;
    RedisUtils redisUtils;
    TenantMapper tenantMapper;
    JvsSystemConfig jvsSystemConfig;

    /**
     * 根据域名获取租户基础信息
     *
     * @param host
     * @return
     */
    public String fromHost(String host) {
        //如果缓存中有
        if (ObjectNull.isNotNull(host)) {
            if (jvsSystemConfig.getMultiTenantMode()) {
                List<SysConfigs> configs = configMapper.selectList(new LambdaQueryWrapper<>(new SysConfigs()).like(SysConfigs::getContent, "\"domainName\":\"" + host.replaceAll("\\." + jvsSystemConfig.getDomain(), "")));
                if (ObjectNull.isNotNull(configs)) {
                    //只找第一条
                    SysConfigs config = configs.get(0);
                    return config.getTenantId();
                }
            }
        }
        return tenantMapper.selectList(new LambdaQueryWrapper<TenantPo>().isNull(TenantPo::getParentId)).get(0).getId();
    }
}
