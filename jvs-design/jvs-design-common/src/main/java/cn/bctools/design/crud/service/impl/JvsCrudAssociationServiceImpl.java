package cn.bctools.design.crud.service.impl;

import cn.bctools.design.crud.entity.CrudAssociationPo;
import cn.bctools.design.crud.mapper.JvsCrudAssociationMapper;
import cn.bctools.design.crud.service.JvsCrudAssociationService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Auto Generator
 */
@Service
@AllArgsConstructor
public class JvsCrudAssociationServiceImpl extends ServiceImpl<JvsCrudAssociationMapper, CrudAssociationPo> implements JvsCrudAssociationService, IJvsDesigner {
    private final MapperMethodHandler mapperMethodHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<CrudAssociationPo>lambdaQuery().eq(CrudAssociationPo::getJvsAppId, appId));
    }
}
