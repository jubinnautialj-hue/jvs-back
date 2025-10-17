package cn.bctools.design.crud.service.impl;


import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.crud.entity.AppUrlPo;
import cn.bctools.design.crud.mapper.AppUrlMapper;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author guojing
 */
@Slf4j
@Service
@Design(DesignType.URL)
@AllArgsConstructor
public class AppUrlServiceImpl extends ServiceImpl<AppUrlMapper, AppUrlPo> implements AppUrlService, IJvsDesigner {
    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void delete(String appId, String designId) {
        remove(Wrappers.query(new AppUrlPo().setJvsAppId(appId).setId(designId)));
    }

    @Override
    public void updateName(String appId, @Nullable String designId, @Nullable String name) {
        if (ObjectNull.isNull(name)) {
            return;
        }
        this.update(Wrappers.<AppUrlPo>lambdaUpdate()
                .set(AppUrlPo::getName, name)
                .eq(AppUrlPo::getJvsAppId, appId)
                .eq(StringUtils.isNotBlank(designId), AppUrlPo::getId, designId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<AppUrlPo>lambdaQuery().eq(AppUrlPo::getJvsAppId, appId));
    }

}
