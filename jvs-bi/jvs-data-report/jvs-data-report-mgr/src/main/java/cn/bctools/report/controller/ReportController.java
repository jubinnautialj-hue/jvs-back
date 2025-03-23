package cn.bctools.report.controller;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.log.annotation.Log;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.report.dto.CopyDto;
import cn.bctools.report.dto.MoveDTO;
import cn.bctools.report.dto.SearchDTO;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.entity.SysMenu;
import cn.bctools.report.enums.EDataGrowthPlan;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.ReportCustom;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.UWorkbook;
import cn.bctools.report.model.univer.conf.USearch;
import cn.bctools.report.render.RenderFactory;
import cn.bctools.report.service.JvsDataReportService;
import cn.bctools.report.service.SysMenuService;
import cn.bctools.report.utils.AuthUtils;
import cn.bctools.report.utils.UWorkBookUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Api(tags = "报表接口")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final JvsDataReportService jvsDataReportService;
    private final RenderFactory renderFactory;
    private final SysMenuService sysMenuService;

    @Log(back = false)
    @ApiOperation("保存设计")
    @PostMapping("/save")
    public R<JvsDataReport> save(@RequestBody JvsDataReport reportInfo) {
        long count = jvsDataReportService.count(Wrappers.lambdaQuery(JvsDataReport.class).eq(JvsDataReport::getMenuId, reportInfo.getMenuId()));
        reportInfo.setSort((int) (count+1));
        jvsDataReportService.save(reportInfo);
        return R.ok(reportInfo);
    }

    @Log(back = false)
    @ApiOperation("修改设计")
    @PutMapping("/edit/design")
    public R<Boolean> editDesign(@RequestBody JvsDataReport reportInfo) {
        UWorkbook reportDesign = reportInfo.getReportDesign();
        reportDesign.getSheets().forEach((sheetId,sheet) -> {
            Map<Integer, Map<Integer, UCell>> cellData = sheet.getCellData();
            cellData.forEach((rowNum,columnData) -> columnData.forEach((columnNum, cell)->{
                //如果是交叉拓展 则为动态属性
                if (EDataGrowthPlan.CROSS_TAB.equals(cell.getCustom().getDataGrowthPlan())) {
                    cell.getCustom().setDynamic(Boolean.TRUE);
                }
                if(cell.getV()!=null || cell.getP()!=null){
                    if(Optional.ofNullable(cell).map(UCell::getCustom).map(UCellExpand::getField).filter(e -> StrUtil.isAllNotBlank(e.getFieldKey(),e.getExecuteName())).isPresent()){
                        cell.getCustom().setValueType(EValueType.数据集);
                        cell.setT(cell.getCustom().getField().getUCellT());
                        return;
                    }
                    if(cell.getF()!=null || cell.getSi()!=null){
                        cell.getCustom().setValueType(EValueType.公式);
                        return;
                    }
                    cell.setCustom(new UCellExpand().setValueType(EValueType.固定值));
                }else{
                    cell.setCustom(new UCellExpand().setValueType(EValueType.空));
                }
            }));
        });
        boolean b = jvsDataReportService.updateById(new JvsDataReport()
                .setId(reportInfo.getId())
                .setReportType(reportInfo.getReportType())
                .setReportDesign(reportDesign));
        return R.ok(b);
    }

    @Log(back = false)
    @ApiOperation("修改基础信息")
    @PutMapping("/edit/base")
    public R<Boolean> editBase(@RequestBody JvsDataReport reportInfo) {
        boolean b = jvsDataReportService.updateById(new JvsDataReport()
                .setId(reportInfo.getId())
                .setReportName(reportInfo.getReportName())
                .setReportDesc(reportInfo.getReportDesc()));
        return R.ok(b);
    }

    @Log(back = false)
    @ApiOperation("修改权限")
    @PutMapping("/edit/permission")
    public R<Boolean> editRole(@RequestBody JvsDataReport reportInfo) {
        JvsDataReport report = new JvsDataReport().setId(reportInfo.getId());
        report.setRoleType(reportInfo.getRoleType()).setRole(reportInfo.getRole());
        boolean b = jvsDataReportService.updateById(report);
        return R.ok(b);
    }

    @Log(back = false)
    @ApiOperation("获取详情")
    @GetMapping("/detail/{id}")
    public R<JvsDataReport> detail(@ApiParam("报表id")@PathVariable("id")String id) {
        JvsDataReport jvsDataReport = jvsDataReportService.getById(id);
        AuthUtils.setOperation(jvsDataReport);
        return R.ok(jvsDataReport);
    }

    @Log(back = false)
    @ApiOperation("复制报表")
    @PostMapping("/duplicate")
    public R<JvsDataReport> duplicate(@RequestBody @Validated CopyDto copyDto) {
        String menuId = copyDto.getMenuId();
        SysMenu menu = sysMenuService.getById(menuId);
        if(menu==null){
            return R.failed("报表不存在");
        }
        if(!AuthUtils.verify(menu,OperationType.新增)){
            return R.failed("指定目录权限不足，无新增权限");
        }
        JvsDataReport jvsDataReport = jvsDataReportService.getById(copyDto.getReportId());
        jvsDataReport.clear();
        //copyId
        String newId = IdGenerator.get32UUID();
        jvsDataReport.setId(newId).setMenuId(copyDto.getMenuId()).setReportName(copyDto.getName());
        //修改 workBook sheet id
        UWorkbook uWorkbook = UWorkBookUtils.replaceId(jvsDataReport.getReportDesign(), newId);
        jvsDataReport.setReportDesign(uWorkbook);
        jvsDataReportService.save(jvsDataReport);
        return R.ok(jvsDataReport);
    }

    @Log(back = false)
    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R<Boolean> del(@ApiParam("报表id")@PathVariable("id")String id) {
        return R.ok(jvsDataReportService.removeById(id));
    }

    @Log(back = false)
    @ApiOperation("获取权限集")
    @GetMapping("/permissions")
    public R<List<OperationType>> getAllPermissions() {
        return R.ok(AuthUtils.getReport());
    }

    @Log(back = false)
    @ApiOperation("移动")
    @PutMapping("/move")
    public R<List<JvsDataReport>> move(@RequestBody MoveDTO moveDTO) {
        //获取数据
        JvsDataReport byId = jvsDataReportService.getById(moveDTO.getId());
        //获取当前目录下面的所有文档
        List<JvsDataReport> list = jvsDataReportService.list(new LambdaQueryWrapper<JvsDataReport>().eq(JvsDataReport::getMenuId, moveDTO.getParentId())
                .orderByAsc(JvsDataReport::getSort)).stream().filter(e -> !e.getId().equals(moveDTO.getId())).collect(Collectors.toList());
        int indexOf = 0;
        if (StrUtil.isNotBlank(moveDTO.getNextId())) {
            indexOf = list.stream().map(JvsDataReport::getId).collect(Collectors.toList()).indexOf(moveDTO.getNextId());
        }
        Boolean aBoolean = Optional.ofNullable(moveDTO.getIsFront()).orElse(false);
        if (aBoolean) {
            indexOf += 1;
        }
        //插入数据
        byId.setMenuId(moveDTO.getParentId());
        list.add(indexOf, byId);
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort(i);
        }
        jvsDataReportService.updateBatchById(list);
        return R.ok(list);
    }

    @Log(back = false)
    @ApiOperation("获取报表数据")
    @GetMapping("/get/render/data")
    public R<UWorkbook> getData(@RequestParam(name = "id") String reportId){
        JvsDataReport report = jvsDataReportService.getById(reportId);
        renderFactory.compute(report);
        return R.ok(report.getReportDesign());
    }

    @Log(back = false)
    @ApiOperation("传入查询条件并获取报表数据")
    @PostMapping("/search/render/data")
    public R<UWorkbook> searchData(@Validated @RequestBody SearchDTO searchDTO){
        JvsDataReport report = jvsDataReportService.getById(searchDTO.getReportId());
        Assert.notNull(report,() -> new BusinessException("报表不存在"));
        //设置查询条件
        Map<String, Map<String, Object>> settingMap = searchDTO
                .getSearchFields()
                .stream()
                .filter(e -> {
                    boolean notNull = ObjectUtil.isNotNull(e.getValue());
                    if(notNull && e.getValue() instanceof String){
                        return StrUtil.isNotBlank(e.getValue().toString());
                    }
                    return notNull;
                })
                .collect(Collectors.groupingBy(SearchDTO.FieldItem::getExecuteName, Collectors.toMap(SearchDTO.FieldItem::getFieldKey, SearchDTO.FieldItem::getValue)));

        report.getReportDesign().getSheets().forEach((key, value) -> {
            ReportCustom custom = value.getCustom();
            List<USearch> queryConf = custom.getQueryConf();
            if (CollectionUtil.isNotEmpty(queryConf)) {
                queryConf.forEach(e -> {
                    if (settingMap.containsKey(e.getExecuteName())) {
                        Map<String, Object> paramsMap = settingMap.get(e.getExecuteName());
                        e.getFields().forEach(field -> field.setQueryValue(paramsMap.get(field.getFieldKey())));
                    }
                });
            }
        });
        renderFactory.compute(report);
        return R.ok(report.getReportDesign());
    }

}
