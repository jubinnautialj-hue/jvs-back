package cn.bctools.document.service.impl;

import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.entity.enums.IdentifyingTypeEnum;
import cn.bctools.document.mapper.DcIdentifyingMapper;
import cn.bctools.document.service.DcIdentifyingService;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Auto Generator
 */
@Service
public class DcIdentifyingServiceImpl extends ServiceImpl<DcIdentifyingMapper, DcIdentifying> implements DcIdentifyingService {

    @Override
    public List<DcIdentifying> typeGetList(IdentifyingKeyEnum... identifyingKeyEnum) {
        return this.list(new LambdaQueryWrapper<DcIdentifying>().in(DcIdentifying::getIdentifyingKey, identifyingKeyEnum));

    }

    @Override
    @Transactional
    public List<DcIdentifying> basisTypeToPossessor(IdentifyingTypeEnum identifyingType, Boolean possessorIs) {
        LambdaQueryWrapper<DcIdentifying> queryWrapper = new LambdaQueryWrapper<DcIdentifying>()
                .eq(!possessorIs, DcIdentifying::getPossessorIs, possessorIs)
                .eq(ObjUtil.isNotNull(identifyingType), DcIdentifying::getIdentifyingType, identifyingType);
        return this.list(queryWrapper);
    }
}
