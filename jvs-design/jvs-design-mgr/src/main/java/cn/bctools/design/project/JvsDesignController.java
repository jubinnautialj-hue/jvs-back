package cn.bctools.design.project;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.database.entity.po.BasePo;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.AppUrlService;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.fields.dto.FormSelectItemDto;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.h5.service.H5DesignService;
import cn.bctools.design.menu.component.AppMenuHandler;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.project.dto.*;
import cn.bctools.design.project.handler.DesignHandler;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.screen.service.ScreenService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用设计管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[design]设计管理")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/JvsApp")
public class JvsDesignController {

    JvsAppService appService;
    DesignHandler designHandler;
    CrudPageService pageService;
    FormService formService;
    ScreenService screenService;
    AppUrlService appUrlService;
    H5DesignService h5DesignService;
    AppMenuService appMenuService;
    AppMenuHandler appMenuHandler;

    /**
     * 新增应用目录
     *
     * @param typeDto 应用分组数据
     * @return 操作结果
     */
    @Log
    @ApiOperation("应用目录-新增")
    @PostMapping("/add/type")
    public R<Boolean> saveType(@Validated @RequestBody AppTypeDto typeDto, @PathVariable String appId) {
        typeDto.setAppId(appId);
        appService.addType(typeDto.getAppId(), typeDto.getType(), typeDto.getIcon(), typeDto.getParentId());
        return R.ok(true);
    }

    /**
     * 删除应用目录
     *
     * @return 操作结果
     */
    @Log
    @ApiOperation("应用目录-删除")
    @DeleteMapping("/del/type/{id}")
    public R<Boolean> removeType(@PathVariable String id, @PathVariable String appId) {
        appService.removeType(appId, id);
        return R.ok(true);
    }

    /**
     * 新增应用目录
     *
     * @param typeDto 应用分组数据
     * @return 操作结果
     */
    @Log
    @ApiOperation("应用目录-修改")
    @PostMapping("/update/type")
    public R<Boolean> updateType(@Validated @RequestBody AppTypeUpdateDto typeDto, @PathVariable String appId) {
        typeDto.setAppId(appId);

        appService.updateType(typeDto.getId(), typeDto.getAppId(), typeDto.getType(), typeDto.getNewType(), typeDto.getIcon(), typeDto.getParentId());
        return R.ok(true);
    }

    /**
     * 删除应用设计
     *
     * @param designDto 应用设计数据
     * @return 操作结果
     */
    @Log
    @ApiOperation("设计-删除")
    @DeleteMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> removeDesign(@Validated AppDesignDto designDto, @PathVariable String appId) {
        designDto.setAppId(appId);
        appMenuService.remove(designDto.getAppId(), designDto.getDesignId());
        designHandler.removeDesign(designDto.getAppId(), designDto.getDesignId(), designDto.getDesignType());
        return R.ok(true);
    }

    /**
     * 修改应用设计的名称或应用目录
     *
     * @param updateDto 应用设计修改数据
     * @return 操作结果
     */
    @Log
    @ApiOperation("设计-修改名称或目录")
    @PutMapping("/update")
    public R<Boolean> updateDesign(@Validated DesignUpdateDto updateDto, @PathVariable String appId) {
        String type = updateDto.getType();
        updateDto.setAppId(appId);
        if (StringUtils.isNotBlank(type) && Boolean.FALSE.equals(appId.equals(type))) {
            boolean exist = appService.existType(appId, type);
            if (!exist) {
                return R.failed("选择的目录不存在请重试");
            }
        }

        appMenuService.updateType(updateDto.getAppId(), updateDto.getDesignId(), updateDto.getType(), updateDto.getName());
        designHandler.updateDesign(updateDto);
        return R.ok(true);
    }

    @Log
    @ApiOperation("设计-显示隐藏")
    @PutMapping("/display")
    public R<Boolean> display(@Validated AppDesignDto updateDto, @PathVariable String appId) {
        appMenuService.display(appId, updateDto.getDesignId(), updateDto.getMobileDisplay(), updateDto.getPcDisplay());
        return R.ok();
    }

    @Log
    @GetMapping("/design/type/page")
    @ApiOperation("设计按类型分页")
    @Transactional(rollbackFor = Exception.class)
    public R<Page<DesignVo>> designTypePage(Page<DesignVo> page, @PathVariable String appId, DesignVo designVo) {
        if (DesignType.form.equals(designVo.getDesignType())) {
            Page<FormPo> formPage = new Page<>(page.getCurrent(), page.getSize());
            formService.page(formPage, Wrappers.<FormPo>lambdaQuery()
                    .select(FormPo::getId, FormPo::getName, FormPo::getCreateTime, FormPo::getUpdateTime)
                    .isNotNull(FormPo::getName)
                    .eq(FormPo::getJvsAppId, appId)
                    .like(ObjectNull.isNotNull(designVo.getName()), FormPo::getName, designVo.getName()));
            List<DesignVo> list = BeanCopyUtil.copys(formPage.getRecords(), DesignVo.class);
            page.setRecords(list);
            page.setTotal(formPage.getTotal());
            page.setPages(formPage.getPages());
        }

        if (DesignType.page.equals(designVo.getDesignType())) {
            Page<CrudPage> pagePoPage = new Page<>(page.getCurrent(), page.getSize());
            pageService.page(pagePoPage, Wrappers.<CrudPage>lambdaQuery()
                    .select(CrudPage::getId, CrudPage::getName, CrudPage::getCreateTime, CrudPage::getUpdateTime, CrudPage::getViewJson)
                    .isNotNull(CrudPage::getName)
                    .eq(CrudPage::getJvsAppId, appId)
                    .like(ObjectNull.isNotNull(designVo.getName()), CrudPage::getName, designVo.getName()));
            List<DesignVo> list = BeanCopyUtil.copys(pagePoPage.getRecords(), DesignVo.class);
            page.setRecords(list);
            page.setTotal(pagePoPage.getTotal());
            page.setPages(pagePoPage.getPages());
        }
        return R.ok(page);
    }

    @Log
    @GetMapping("/design/all")
    @ApiOperation("获取所有设计数据")
    @Transactional(rollbackFor = Exception.class)
    public R<List<DesignVo>> all(@PathVariable String appId) {
        List<DesignVo> result = new ArrayList<>();
        List<FormPo> formList = formService.list(Wrappers.<FormPo>lambdaQuery()
                .select(FormPo::getId, FormPo::getName, FormPo::getCreateTime, FormPo::getUpdateTime)
                .isNotNull(FormPo::getName)
                .eq(ObjectNull.isNotNull(appId), FormPo::getJvsAppId, appId));
        List<CrudPage> pageList = pageService.list(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getId, CrudPage::getName, CrudPage::getCreateTime, CrudPage::getUpdateTime, CrudPage::getViewJson)
                .isNotNull(CrudPage::getName)
                .eq(ObjectNull.isNotNull(appId), CrudPage::getJvsAppId, appId));

        // 数据处理
        this.handleFormInPage(formList, pageList);
        result.addAll(this.parse(formList, DesignType.form));
        result.addAll(this.parse(pageList, DesignType.page));
        // 按创建事件升序排序
        Collections.sort(result);
        return R.ok(result);
    }

    @Log
    @ApiOperation("设计-排序")
    @PostMapping("/sort")
    public R<String> sort(@PathVariable("appId") String appId, @Validated @RequestBody SortDto dto) {
        appMenuHandler.sort(appId, dto.getToMenuCode(), dto.getSortList());
        return R.ok();
    }

    /**
     * 处理列表页中的表单名称
     *
     * @param formList 表单集合
     * @param pageList 列表页集合
     */
    private void handleFormInPage(List<FormPo> formList, List<CrudPage> pageList) {
        List<FormSelectItemDto> forms = DesignUtils.getFormsFromPage(pageList);
        Map<String, String> nameMap = forms.stream().collect(Collectors.toMap(FormSelectItemDto::getId, FormSelectItemDto::getName));
        for (FormPo formPo : formList) {
            String formName = nameMap.get(formPo.getId());
            if (StringUtils.isNotBlank(formName)) {
                formPo.setName(formName);
            }
        }
    }

    /**
     * 转换为设计的基础数据对象
     *
     * @param designList 各设计数据
     * @param designType 设计类型
     * @param <T>        原设计数据对象
     * @return 基础数据对象集合
     */
    private <T extends BasePo> List<DesignVo> parse(List<T> designList, DesignType designType) {
        List<DesignVo> list = BeanCopyUtil.copys(designList, DesignVo.class);
        for (DesignVo designVo : list) {
            designVo.setId(designType.name() + "-" + designVo.getId());
            designVo.setDesignTypeName(designType.getDesc());
        }
        return list;
    }


}
