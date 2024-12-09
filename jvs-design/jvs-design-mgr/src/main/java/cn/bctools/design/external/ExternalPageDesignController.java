package cn.bctools.design.external;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.external.dto.CreateExternalReqDto;
import cn.bctools.design.external.dto.GrantExternalReqDto;
import cn.bctools.design.external.dto.QueryExternalReqDto;
import cn.bctools.design.external.dto.UpdateExternalReqDto;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.external.service.ExternalPageService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Deprecated
@Api(tags = "[external]外部页面接入 设计")
@RestController
@RequestMapping("/app/design/{appId}/external/page")
@AllArgsConstructor
public class ExternalPageDesignController {

    private final ExternalPageService service;

    @ApiOperation("新增外部页面")
    @Log(callBackClass = JvsLogServiceImpl.class)
    @PostMapping
    public R<String> create(@PathVariable String appId, @Validated @RequestBody CreateExternalReqDto dto) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        // 校验是否已存在
        ExternalPage external = service.getOne(Wrappers.<ExternalPage>lambdaQuery()
                .eq(ExternalPage::getJvsAppId, appId)
                .eq(ExternalPage::getUrl, dto.getUrl()).select(ExternalPage::getId));
        if (ObjectNull.isNotNull(external)) {
            throw new BusinessException("页面地址已存在");
        }
        ExternalPage externalPage = BeanCopyUtil.copy(dto, ExternalPage.class);
        externalPage.setJvsAppId(appId);
        service.save(externalPage);
        return R.ok();
    }

    @ApiOperation("修改外部页面")
    @Log(callBackClass = JvsLogServiceImpl.class)
    @PutMapping
    public R<String> edit(@PathVariable String appId, @Validated @RequestBody UpdateExternalReqDto dto) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        // 校验是否已存在
        ExternalPage external = service.getOne(Wrappers.<ExternalPage>lambdaQuery()
                .eq(ExternalPage::getJvsAppId, appId)
                .eq(ExternalPage::getUrl, dto.getUrl()).select(ExternalPage::getId));
        if (ObjectNull.isNotNull(external) && Boolean.FALSE.equals(external.getId().equals(dto.getId()))) {
            throw new BusinessException("页面地址已存在");
        }
        ExternalPage externalPage = BeanCopyUtil.copy(dto, ExternalPage.class);
        service.updateById(externalPage);
        return R.ok();
    }

    @ApiOperation("删除外部页面")
    @Log(callBackClass = JvsLogServiceImpl.class)
    @DeleteMapping("/{id}")
    public R<String> del(@PathVariable String appId, @PathVariable String id) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        service.removeById(id);
        return R.ok();
    }

    @ApiOperation("外部页面列表")
    @GetMapping("/list")
    public R<List<ExternalPage>> list(@PathVariable String appId, QueryExternalReqDto dto) {
        return R.ok(service.list(Wrappers.<ExternalPage>lambdaQuery()
                .like(StringUtils.isNotBlank(dto.getName()), ExternalPage::getName, dto.getName())
                .eq(StringUtils.isNotBlank(dto.getUrl()), ExternalPage::getUrl, dto.getUrl())
                .eq(ExternalPage::getJvsAppId, appId)
                .select(ExternalPage.class, external -> Boolean.FALSE.equals(external.getColumn().equals(Get.name(ExternalPage::getResources)))
                        && Boolean.FALSE.equals(external.getColumn().equals(Get.name(ExternalPage::getPermissions))))
                .orderByDesc(ExternalPage::getCreateTime))
        );
    }

    @ApiOperation("外部页面详情")
    @GetMapping("/{id}")
    public R<ExternalPage> detail(@PathVariable String appId, @PathVariable String id) {
        return R.ok(service.getById(id));
    }

    @ApiOperation("外部页面授权")
    @Log(callBackClass = JvsLogServiceImpl.class)
    @PostMapping("/grant")
    public R<String> grant(@PathVariable String appId, @RequestBody GrantExternalReqDto dto) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        ExternalPage externalPage = new ExternalPage();
        externalPage.setResources(dto.getResources());
        externalPage.setPermissions(dto.getPermissions());
        service.update(externalPage, new LambdaUpdateWrapper<ExternalPage>().eq(ExternalPage::getId, dto.getId()));
        return R.ok();
    }
}
