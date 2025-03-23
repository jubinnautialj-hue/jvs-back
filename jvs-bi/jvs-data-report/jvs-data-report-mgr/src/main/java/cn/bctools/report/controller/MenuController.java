package cn.bctools.report.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.log.annotation.Log;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.report.dto.MoveDTO;
import cn.bctools.report.dto.TreeDTO;
import cn.bctools.report.entity.JvsDataReport;
import cn.bctools.report.entity.SysMenu;
import cn.bctools.report.service.JvsDataReportService;
import cn.bctools.report.service.SysMenuService;
import cn.bctools.report.utils.AuthUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "目录")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuService sysMenuService;
    private final JvsDataReportService jvsDataReportService;

    @Log(back = false)
    @ApiOperation("保存")
    @PostMapping("/save")
    public R<SysMenu> save(@RequestBody SysMenu menu) {
        long count = sysMenuService.count();
        menu.setSort((int) count);
        sysMenuService.save(menu);
        return R.ok(menu);
    }

    @Log(back = false)
    @ApiOperation("修改")
    @PutMapping("/edit")
    public R<SysMenu> edit(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return R.ok(menu);
    }

    @Log(back = false)
    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R<Boolean> del(@ApiParam("目录id")@PathVariable("id")String id) {
        //检查是否有报表
        boolean b = jvsDataReportService.existSource(id);
        if(b){
            return R.failed("当前目录下存在资源，无法删除");
        }
        boolean b1 = sysMenuService.removeById(id);
        return R.ok(b1);
    }

    @Log(back = false)
    @ApiOperation("树形查询")
    @GetMapping("/tree")
    public R<List<TreeDTO>> tree() {
        String rootId = "1";
        List<SysMenu> sysMenus = sysMenuService.list(Wrappers.lambdaQuery(SysMenu.class).orderByAsc(SysMenu::getSort));
        List<TreeDTO> menus = sysMenus.stream()
                .peek(AuthUtils::setOperation)
                .filter(e -> CollectionUtil.isNotEmpty(e.getOperationList()))
                .map(e -> new TreeDTO()
                        .setId(e.getId())
                        .setPid(rootId)
                        .setName(e.getName())
                        .setOperationList(e.getOperationList())
                        .setRole(e.getRole())
                        .setRoleType(e.getRoleType())
                        .setIcon(e.getIcon())
                        .setType(TreeDTO.Type.menu))
                .collect(Collectors.toList());
        List<TreeDTO> reports = jvsDataReportService.list(Wrappers.lambdaQuery(JvsDataReport.class)
                        //设计过大导致查询超出内存
                        .select(JvsDataReport.class,e -> !StrUtil.equals(e.getProperty(), Get.name(JvsDataReport::getReportDesign)))
                        .orderByAsc(JvsDataReport::getSort))
                .stream()
                .peek(AuthUtils::setOperation)
                .filter(e -> CollectionUtil.isNotEmpty(e.getOperationList()))
                .map(e -> new TreeDTO()
                        .setId(e.getId())
                        .setPid(e.getMenuId())
                        .setName(e.getReportName())
                        .setOperationList(e.getOperationList())
                        .setType(TreeDTO.Type.report))
                .collect(Collectors.toList());

        Map<String, List<TreeDTO>> collect = reports.stream().collect(Collectors.groupingBy(TreeDTO::getPid));

        menus.forEach(menu -> {
            List<TreeDTO> children = collect.getOrDefault(menu.getId(), Collections.emptyList());
            menu.setChildren(children);
        });

        return R.ok(menus);
    }

    @Log(back = false)
    @ApiOperation("获取权限集")
    @GetMapping("/permissions")
    public R<List<OperationType>> getAllPermissions() {
        return R.ok(AuthUtils.getMenu());
    }

    @Log(back = false)
    @ApiOperation("移动")
    @PutMapping("/move")
    public R<List<SysMenu>> move(@RequestBody MoveDTO dto) {
        //获取数据
        SysMenu byId = sysMenuService.getById(dto.getId());
        //获取当前目录下面的所有数据
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().ne(SysMenu::getId, dto.getId()).orderByAsc(SysMenu::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(dto.getNextId())) {
            indexOf = list.stream().map(SysMenu::getId).collect(Collectors.toList()).indexOf(dto.getNextId());
        }
        Boolean aBoolean = Optional.ofNullable(dto.getIsFront()).orElse(false);
        if (aBoolean) {
            indexOf += 1;
        }
        //插入数据
        list.add(indexOf, byId);
        //只更新需要更新排序的数据 节省资源
        List<SysMenu> updateList = new ArrayList<>();
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            SysMenu jvsDataFactoryMenu = list.get(i);
            if (!NumberUtil.equals(i, jvsDataFactoryMenu.getSort().intValue())) {
                jvsDataFactoryMenu.setSort( i);
                updateList.add(jvsDataFactoryMenu);
            }
        }
        sysMenuService.updateBatchById(updateList);
        return R.ok(list);
    }
}
