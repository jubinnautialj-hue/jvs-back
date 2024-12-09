package cn.bctools.design.identification;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.identification.dto.CreateIdentificationReqDto;
import cn.bctools.design.identification.dto.PageIdentificationReqDto;
import cn.bctools.design.identification.dto.UpdateIdentificationReqDto;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.bctools.design.data.fields.DataFieldHandler.SEPARATOR_PATH;

/**
 * @author zhuxiaokang
 */
@Api(tags = "[标识]管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/identification")
public class IdentificationController {

    private final IdentificationService service;
    private final RuleDesignService ruleDesignService;
    private final SwaggerRuleApiCacheService swaggerRuleApiCacheService;

    @Log
    @ApiOperation("添加标识")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public R<Identification> create(@Validated @RequestBody CreateIdentificationReqDto dto, @PathVariable String appId) {
        switch (dto.getDesignType()) {
            case rule:
                // 如果是逻辑的标示，需要以/开关
                if (!dto.getIdentifier().startsWith(SEPARATOR_PATH)) {
                    throw new BusinessException("逻辑标识必须以/开头");
                }
            default:
        }
        // 校验租户下是否已存在指定标识符
        service.checkExistsIdentifier(true, appId, dto.getIdentifier(), dto.getDesignId());
        // 新增标识符
        Identification identification = BeanCopyUtil.copy(dto, Identification.class);
        identification.setJvsAppId(appId);
        service.save(identification);
        return R.ok(identification);
    }

    @Log
    @ApiOperation("修改标识")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping
    public R<Identification> update(@Validated @RequestBody UpdateIdentificationReqDto dto, @PathVariable String appId) {
        switch (dto.getDesignType()) {
            case rule:
                // 如果是逻辑的标示，需要以/开关
                if (!dto.getIdentifier().startsWith(SEPARATOR_PATH)) {
                    throw new BusinessException("逻辑标识必须以/开头");
                }
            default:
        }
        Identification identification = service.getById(dto.getId());
        if (ObjectNull.isNull(identification)) {
            R.failed("标识不存在");
        }
        // 新标识与旧标识不同，需要校验新标识是否已存在
        if (Boolean.FALSE.equals(identification.getIdentifier().equals(dto.getIdentifier()))) {
            service.checkExistsIdentifier(false, appId, dto.getIdentifier(), dto.getDesignId());
        }
        // 修改标识符
        Identification updateIdentification = BeanCopyUtil.copy(dto, Identification.class);
        updateIdentification.setJvsAppId(appId);
        service.updateById(updateIdentification);

        // 若修改应用标识，则发布逻辑API swagger缓存变更事件
        if (appId.equals(dto.getDesignId())) {
            swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(false, appId);
        }
        return R.ok(updateIdentification);
    }

    @Log
    @ApiOperation("删除标识")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "标识id", required = true)
    })
    @DeleteMapping("/{id}")
    public R<String> del(@PathVariable String id, @PathVariable String appId) {
        service.removeById(id);
        return R.ok();
    }

    @ApiOperation(value = "应用级标识列表", notes = "查询指定应用已设置的标识列表")
    @GetMapping("/page")
    public R<Page<Identification>> page(Page<Identification> page, PageIdentificationReqDto dto, @PathVariable String appId) {
        Page<Identification> page1 = service.page(page, Wrappers.<Identification>lambdaQuery()
                .eq(Identification::getJvsAppId, appId)
                .eq(ObjectNull.isNotNull(dto.getDesignType()), Identification::getDesignType, dto.getDesignType())
                .like(ObjectNull.isNotNull(dto.getDesignName()), Identification::getDesignName, dto.getDesignName())
                .like(ObjectNull.isNotNull(dto.getIdentifier()), Identification::getIdentifier, dto.getIdentifier())
                .orderByDesc(Identification::getCreateTime));
        for (Identification record : page1.getRecords()) {
            if (record.getDesignType().equals(DesignType.rule)) {
                RuleDesignPo one = ruleDesignService.getOne(new LambdaQueryWrapper<RuleDesignPo>().eq(RuleDesignPo::getSecret, record.getDesignId()).select(RuleDesignPo::getReqType));
                if (ObjectNull.isNotNull(one)) {
                    record.setReqType(one.getReqType());
                }
            }
        }
        return R.ok(page1);
    }
}
