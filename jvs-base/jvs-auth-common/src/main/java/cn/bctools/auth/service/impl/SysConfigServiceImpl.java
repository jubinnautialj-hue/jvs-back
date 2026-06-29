package cn.bctools.auth.service.impl;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.service.TenantService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.enums.SysFrameApplyConfig;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.function.Get;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.gateway.mapper.ConfigMapper;
import cn.bctools.gateway.mapper.TenantMapper;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

/**
 * @author Auto Generator
 */
@Service
@AllArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<ConfigMapper, SysConfigs> implements SysConfigsService {

    DiscoveryClient discoveryClient;
    TenantMapper tenantMapper;

    @Override
    public SysConfigBase getConfig(ConfigsTypeEnum typeEnum) {
        String tenantId = TenantContextHolder.getTenantId();
        SysConfigs entity = new SysConfigs().setType(typeEnum);
        if (typeEnum.equals(ConfigsTypeEnum.BASE)) {
            entity.setConfigLevel(TypeEnum.platform);
            TenantContextHolder.clear();
        }
        SysConfigs one = getOne(Wrappers.query(entity));
        SysConfigBase base = new SysConfigBase().setEnable(false);
        if (ObjectNull.isNotNull(one)) {
            base = BeanCopyUtil.copy(one.getContent(), typeEnum.cls).setEnable(true);
        } else {
            String s = JSONObject.toJSONString(base);
            base = JSONObject.parseObject(s, typeEnum.cls);
        }
        if (typeEnum.equals(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION)) {
            //判断密码是否为空如果为空，获取租户设置的数据
            String name = Get.name(SysFrameApplyConfig::getDefaultPassword);
            Object read = "";
            if (ObjectNull.isNotNull(one)) {
                read = JvsJsonPath.read(one.getContent(), name);
            }
            if (ObjectNull.isNull(read)) {
                String defaultPassword = tenantMapper.selectById(tenantId).getDefaultPassword();
                JvsJsonPath.set(base, name, defaultPassword);
            }
        }
        TenantContextHolder.setTenantId(tenantId);
        return base;
    }
}
