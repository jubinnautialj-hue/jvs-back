package cn.bctools.design.crud;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * <列表设计>
 *
 * @author auto
 **/
@Api(tags = "列表设计")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/page")
public class CrudPageController {

    FormService formService;
    JvsAppService jvsAppService;
    CrudPageService crudPageService;
    DataFieldService dataFieldService;
    DataModelService dataModelService;
    AppMenuService appMenuService;

    @Log
    @ApiOperation("修改名称和分类")
    @PutMapping("/rename")
    @Transactional(rollbackFor = Exception.class)
    public R rename(@PathVariable String appId, @RequestBody @Valid CrudPage crudPage) {
        String id = crudPage.getId();
        String name = crudPage.getName();
        String type = crudPage.getType();
        // 修改设计名称
        CrudPage page = crudPageService.get(id);
        if (page.getJvsAppId().equals(appId)) {
            page.setName(name);
            page.setType(type);
            crudPageService.update(Wrappers.<CrudPage>lambdaUpdate()
                    .set(CrudPage::getName, name)
                    .set(CrudPage::getType, type)
                    .eq(CrudPage::getId, id));
            // 处理数据模型
            dataModelService.updateName(page.getDataModelId(), name);
            return R.ok(crudPage);
        }
        return R.ok();
    }

    /**
     * 根据列表页里面的设计，快速生成表单
     */
    @Log
    @GetMapping("/generateForm/{dataModelId}/{designId}/{buttonName}")
    @ApiOperation("生成表单")
    public R generateForm(@PathVariable("dataModelId") String dataModelId, @PathVariable("appId") String appId, @PathVariable("buttonName") String buttonName) {
        FormPo formPo = formService.create(dataModelId, appId, buttonName);
        return R.ok(formPo.getId());
    }


    @Log
    @GetMapping("/generateForm/{dataModelId}/{designId}")
    @ApiOperation("生成表单")
    public R generate(@PathVariable("dataModelId") String dataModelId, @PathVariable("appId") String appId, @RequestParam("buttonName") String buttonName) {
        buttonName = URLDecoder.decode(buttonName, Charset.defaultCharset());
        FormPo formPo = formService.create(dataModelId, appId, buttonName);
        return R.ok(formPo.getId());
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @PostMapping("/update/{id}")
    @ApiOperation("更新")
    @Transactional(rollbackFor = Exception.class)
    public R<PageDesignHtml> update(@PathVariable String appId, @PathVariable("id") String id, @RequestBody PageDesignHtml design) {
        CrudPage crudPage = crudPageService.getOne(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getJvsAppId, CrudPage::getDataModelId)
                .eq(CrudPage::getId, id));
        if (Objects.isNull(crudPage)) {
            log.info("列表页设计不存在, 设计id: {}", id);
            return R.failed("设计不存在");
        }
        String jvsAppId = crudPage.getJvsAppId();
        String dataModelId = crudPage.getDataModelId();
        crudPageService.initButton(jvsAppId, dataModelId, design);
        PageDesignHtml pageDesignHtml = crudPageService.updateDesign(id, design);
        // 保存字段信息
        List<DataFieldPo> fields = DesignUtils.getFields(pageDesignHtml, dataModelId, id);
        // 处理数据模型
        dataModelService.updateName(crudPage.getDataModelId(), design.getName());
        dataFieldService.updateFields(jvsAppId, id, DesignType.page, dataModelId, fields);
        return R.ok(pageDesignHtml);
    }

    @Deprecated
    @Log(callBackClass = JvsLogServiceImpl.class)
    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public R del(@PathVariable String appId, @PathVariable("id") String id) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        crudPageService.delete(appId, id);
        return R.ok();
    }

    @Log(callBackClass = JvsLogServiceImpl.class, back = false)
    @PostMapping("/create")
    @ApiOperation("创建")
    public R<CrudPage> create(@PathVariable String appId, @RequestBody CrudPage crudPage) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(appId));
        crudPage.setJvsAppId(appId);
        if (ObjectNull.isNull(crudPage.getName())) {
            crudPage.setName("未命名列表");
        }
        CrudPage page = crudPageService.create(crudPage);
        return R.ok(page);
    }

    @Log
    @ApiOperation("根据权限获取预览和使用的结果")
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable String appId, @PathVariable("id") String id) {
        CrudPage po = crudPageService.getById(id);
        AppMenu appMenu = appMenuService.getDesignMenu(po.getId(), po.getJvsAppId());
        po.setAppMenu(appMenu);
        if (ObjectNull.isNull(po.getViewJson())) {
            return R.ok(po);
        }
        PageDesignHtml design = DesignUtils.parsePage(po.getViewJson());
        crudPageService.convertDesign(po, design);
        return R.ok(po.setViewJson(JSONObject.toJSONString(design)));
    }

    @Log
    @GetMapping("/all")
    @ApiOperation("获取应用所有列表设计")
    @Transactional(rollbackFor = Exception.class)
    public R<List<CrudPage>> page(@PathVariable String appId) {
        List<CrudPage> pageList = crudPageService.list(Wrappers.<CrudPage>lambdaQuery()
                .select(CrudPage::getId, CrudPage::getDataModelId, CrudPage::getName, CrudPage::getJvsAppId)
                .isNotNull(CrudPage::getName)
                .eq(StringUtils.isNotBlank(appId), CrudPage::getJvsAppId, appId)
                .orderByAsc(CrudPage::getCreateTime));
        return R.ok(pageList);
    }

    @Log
    @ApiOperation(value = "列表-分页")
    @GetMapping("/list")
    public R<Page<CrudPage>> page(Page<CrudPage> page, String name, @PathVariable String appId) {
        crudPageService.page(page, Wrappers.<CrudPage>lambdaQuery()
                //查询非view_json的字段
                .select(CrudPage.class, tableFieldInfo -> !tableFieldInfo.getProperty().equalsIgnoreCase(Get.name(CrudPage::getViewJson)))
                //应用表单只能引用当前应用下的单表
                .eq(CrudPage::getJvsAppId, appId)
                //按名称
                .like(ObjectNull.isNotNull(name), CrudPage::getName, name));
        return R.ok(page);
    }
}
