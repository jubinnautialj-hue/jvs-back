package cn.bctools.chart.controller;

import cn.bctools.chart.chart.util.AuthUtil;
import cn.bctools.chart.dto.ChartMoveDto;
import cn.bctools.chart.entity.ChartPage;
import cn.bctools.chart.entity.SysMenu;
import cn.bctools.chart.enums.ChartPageSource;
import cn.bctools.chart.enums.MenuOperationEnum;
import cn.bctools.chart.enums.OperationEnum;
import cn.bctools.chart.service.ChartPageService;
import cn.bctools.chart.service.SysMenuService;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Api(tags = "图表-菜单")
@RestController
@AllArgsConstructor
@RequestMapping
@Slf4j
public class ChartMenuController {
    private final ChartPageService chartPageService;
    private final SysMenuService sysMenuService;
    private final AuthUtil<OperationEnum, ChartPage> authUtil;
    private final AuthUtil<MenuOperationEnum, SysMenu> sysMenuAuthUtil;


    @Log
    @ApiOperation("菜单")
    @GetMapping("/menu")
    public R<List<SysMenu>> menu(ChartPage dto) {
        //先获取一级
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        //菜单权限
        list = sysMenuAuthUtil.auth(list, null, Arrays.asList(MenuOperationEnum.values()));
        if (list.isEmpty()) {
            return R.ok();
        }
        //先判断
        boolean notBlank = StrUtil.isNotBlank(dto.getName());
        List<ChartPage> chartPages = chartPageService.list(new LambdaQueryWrapper<ChartPage>()
                .select(ChartPage::getId,ChartPage::getRoleType, ChartPage::getName, ChartPage::getSort, ChartPage::getRole, ChartPage::getCreateById, ChartPage::getType)
                .in(ChartPage::getType, list.stream().map(SysMenu::getId).collect(Collectors.toList()))
                .like(notBlank, ChartPage::getName, dto.getName())
                .eq(ChartPage::getSource, ChartPageSource.chart)
                .orderByAsc(ChartPage::getSort));
        List<ChartPage> auth = authUtil.auth(chartPages, null, Arrays.asList(OperationEnum.values()));
        list.forEach(e -> {
            List<ChartPage> sonList = auth.stream().filter(v -> v.getType().equals(e.getId())).collect(Collectors.toList());
            long count = chartPages.stream().filter(v -> v.getType().equals(e.getId())).count();
            e.setCount(count);
            e.setChildren(sonList);
        });
        //判断是否存在搜索条件 如果存在需要过滤掉目录为空的数据
        if (notBlank) {
            list = list.stream().filter(e -> !e.getChildren().isEmpty()).collect(Collectors.toList());
        }
        //条件过滤
        return R.ok(list);
    }

    @Log
    @ApiOperation("排序")
    @PutMapping("/move")
    public R<List<SysMenu>> move(@RequestBody ChartMoveDto chartMoveDto) {
        //获取数据
        SysMenu byId = sysMenuService.getById(chartMoveDto.getId());
        //获取当前目录下面的所有数据
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().ne(SysMenu::getId, chartMoveDto.getId()).orderByAsc(SysMenu::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(chartMoveDto.getFrontId())) {
            indexOf = list.stream().map(SysMenu::getId).collect(Collectors.toList()).indexOf(chartMoveDto.getFrontId()) + 1;
        }
        //插入数据
        list.add(indexOf, byId);
        //截取数据 只需要从当前下标到最后的位置需要变更 避免浪费
        list = list.subList(indexOf, list.size());
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            SysMenu sysMenu = list.get(i);
            sysMenu.setSort((long) indexOf + i);
        }
        sysMenuService.updateBatchById(list);
        return R.ok(list);
    }


    @Log
    @ApiOperation("获取一级菜单")
    @GetMapping("/first/menu")
    public R<List<SysMenu>> firstMenu() {
        List<SysMenu> list = sysMenuService.list();
        return R.ok(list);
    }

    @Log
    @ApiOperation("添加一级菜单")
    @PostMapping("/add/menu")
    public R<SysMenu> add(@RequestBody SysMenu sysMenu) {
        //判断名称是否存在
        Long count = sysMenuService.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getName, sysMenu.getName()));
        if (count > 0) {
            return R.failed("菜单名称重复!");
        }
        //获取数量
        Long i = sysMenuService.count() + 1;
        sysMenu.setSort(i);
        sysMenuService.save(sysMenu);
        return R.ok(sysMenu);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/delete/menu/{id}")
    public R deleteMenu(@PathVariable String id) {
        //判断下面是否存在其他资源
        long count = chartPageService.count(new LambdaQueryWrapper<ChartPage>().eq(ChartPage::getType, id));
        if (count > BigDecimal.ROUND_UP) {
            return R.failed("此目前存在其它资源无法删除");
        }
        sysMenuService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("修改名称")
    @PostMapping("/update/menu")
    public R<SysMenu> update(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateById(sysMenu);
        sysMenuAuthUtil.auth(sysMenu, null, Arrays.asList(MenuOperationEnum.values()));
        return R.ok(sysMenu);
    }
}
