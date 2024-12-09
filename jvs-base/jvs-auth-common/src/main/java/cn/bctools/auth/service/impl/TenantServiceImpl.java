package cn.bctools.auth.service.impl;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.service.TenantService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.gateway.mapper.TenantMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 公司租户管理
 *
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantServiceImpl extends ServiceImpl<TenantMapper, TenantPo> implements TenantService {

    TenantMapper tenantMapper;
    SysConfigsService configsService;

    @Override
    public List<TenantPo> getChild() {
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            return Collections.emptyList();
        }
        List<TenantPo> tenantList = this.list();
        return TreeUtils.getListPassingBy(tenantList, tenantId, TenantPo::getId, TenantPo::getParentId);
    }


    /**
     * 根据域名和租户id获取基础信息
     *
     * @param loginDomain
     * @param tenantIds   登录用户匹配的租户id集合
     * @param appKey
     * @return
     */
    @Override
    public Optional<SysApplyConfig> getTenantIdFromHost(String loginDomain, Set<String> tenantIds, String appKey) {
        if (StringUtils.isBlank(loginDomain)) {
            return Optional.empty();
        }
        Optional<ConfigsTypeEnum> first = Arrays.stream(ConfigsTypeEnum.values()).filter(e -> ObjectNull.isNotNull(e.clientId)).filter(e -> e.clientId.equals(appKey)).findFirst();
        if (!first.isPresent()) {
            return Optional.empty();
        }
        return tenantIds.stream().map(e -> {
            TenantContextHolder.setTenantId(e);
            SysApplyConfig config = configsService.getConfig(first.get());
            if (ObjectNull.isNotNull(config.getDomainName()) && config.getDomainName().contains(loginDomain)) {
                return config;
            } else {
                return null;
            }
        }).filter(Objects::nonNull).findFirst();
    }


}
