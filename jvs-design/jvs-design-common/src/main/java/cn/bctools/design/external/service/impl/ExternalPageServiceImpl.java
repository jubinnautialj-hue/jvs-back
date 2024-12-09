package cn.bctools.design.external.service.impl;

import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.external.mapper.ExternalPageMapper;
import cn.bctools.design.external.service.ExternalPageService;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 * 外部页面配置 服务实现类
 */
@Service
@AllArgsConstructor
public class ExternalPageServiceImpl extends ServiceImpl<ExternalPageMapper, ExternalPage> implements ExternalPageService, IJvsDesigner {
    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<ExternalPage>lambdaQuery().eq(ExternalPage::getJvsAppId, appId));
    }
}
