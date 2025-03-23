package cn.bctools.design.project.service.impl;

import cn.bctools.design.project.entity.JvsAppVersionMapping;
import cn.bctools.design.project.mapper.JvsAppVersionMappingMapper;
import cn.bctools.design.project.service.JvsAppVersionMappingService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Service
public class JvsAppVersionMappingServiceImpl extends ServiceImpl<JvsAppVersionMappingMapper, JvsAppVersionMapping> implements JvsAppVersionMappingService {

    @Override
    public List<JvsAppVersionMapping> getIdMappings(String affiliationApp) {
        return list(Wrappers.<JvsAppVersionMapping>lambdaQuery().eq(JvsAppVersionMapping::getAffiliationApp, affiliationApp));
    }
}
