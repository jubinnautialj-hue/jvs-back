package cn.bctools.design.h5.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.h5.entity.H5Design;
import cn.bctools.design.h5.mapper.H5DesignMapper;
import cn.bctools.design.h5.service.H5DesignService;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * @author Auto Generator
 */
@Service
@Design(DesignType.h5)
@AllArgsConstructor
public class H5DesignServiceImpl extends ServiceImpl<H5DesignMapper, H5Design> implements H5DesignService, IJvsDesigner {
    private final MapperMethodHandler mapperMethodHandler;

    @Override
    public void delete(String appId, String designId) {
        this.remove(Wrappers.<H5Design>lambdaQuery().eq(H5Design::getId, designId));
    }

    @Override
    public void beforeAppDeleted(String appId) {
        mapperMethodHandler.deletePhysical(this, Wrappers.<H5Design>lambdaQuery().eq(H5Design::getJvsAppId, appId));
    }

    @Override
    public void updateName(String appId, @Nullable String designId, @Nullable String name) {
        if (ObjectNull.isNull(name)) {
            return;
        }
        this.update(Wrappers.<H5Design>lambdaUpdate()
                .set(H5Design::getName, name)
                .eq(H5Design::getJvsAppId, appId)
                .eq(StringUtils.isNotBlank(designId), H5Design::getId, designId));
    }
}
