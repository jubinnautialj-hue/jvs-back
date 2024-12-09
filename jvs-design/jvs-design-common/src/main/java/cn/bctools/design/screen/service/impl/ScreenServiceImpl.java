package cn.bctools.design.screen.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.handler.Design;
import cn.bctools.design.project.handler.IJvsDesigner;
import cn.bctools.design.screen.entity.ScreenPo;
import cn.bctools.design.screen.mapper.ScreenMapper;
import cn.bctools.design.screen.service.ScreenService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * @author wl
 */
@Slf4j
@Service
@Design(DesignType.screen)
public class ScreenServiceImpl extends ServiceImpl<ScreenMapper, ScreenPo> implements ScreenService, IJvsDesigner {

    @Override
    public void updateName(String appId, @Nullable String designId, @Nullable String name) {
        if (ObjectNull.isNull(name)) {
            return;
        }
        this.update(Wrappers.<ScreenPo>lambdaUpdate()
                .set(ScreenPo::getName, name)
                .eq(ScreenPo::getJvsAppId, appId)
                .eq(StringUtils.isNotBlank(designId), ScreenPo::getId, designId));
    }

    @Override
    public void delete(String appId, String designId) {
        this.removeById(designId);
    }

}
