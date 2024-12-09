package cn.bctools.design.crud;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.AppUrlPo;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author guojing
 * 自定义页面支持两种 方式 ,独立项目, 和融合项目,将项目内容放一起处理
 */
@Api(tags = "自定义外部页面")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/jvsAppUrl")
public class AppUrlController {

    AppUrlService appUrlService;
    AppMenuService appMenuService;
    AppMenuHandler appMenuHandler;

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("新增")
    @PostMapping
    public R<AppUrlPo> create(@RequestBody @Valid AppUrlPo design, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        design.setJvsAppId(appId);
        appUrlService.save(design);
        appMenuHandler.addMenu(DesignType.URL, design.getId(), design.getJvsAppId(), null, design.getName(), design.getType());
        return R.ok(design);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("更新")
    @PutMapping
    public R<AppUrlPo> update(@RequestBody @Valid AppUrlPo design, @PathVariable String appId) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        design.setJvsAppId(appId);
        AppUrlPo one = appUrlService.getOne(Wrappers.query(new AppUrlPo().setId(design.getId()).setJvsAppId(appId)));
        if (ObjectNull.isNull(one)) {
            throw new BusinessException("应用错误或设计不存在");
        }
        appUrlService.updateById(design);
        appMenuService.updateType(design.getJvsAppId(), design.getId(), design.getType(), design.getName());
        return R.ok(design);
    }

}
