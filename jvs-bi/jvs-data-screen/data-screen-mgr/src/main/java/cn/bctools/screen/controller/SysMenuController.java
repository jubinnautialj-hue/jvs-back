package cn.bctools.screen.controller;

import cn.bctools.common.utils.R;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.screen.dto.ChartMoveDto;
import cn.bctools.screen.entity.ChartPage;
import cn.bctools.screen.entity.SysMenu;
import cn.bctools.screen.service.ChartPageService;
import cn.bctools.screen.service.SysMenuService;
import cn.bctools.screen.utils.AuthUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "大屏-菜单")
@RestController
@AllArgsConstructor
@RequestMapping
@Slf4j
public class SysMenuController {
    private final SysMenuService sysMenuService;
    private final ChartPageService chartPageService;


    @ApiOperation("菜单")
    @GetMapping("/menu")
    public R<List<SysMenu>> menu(ChartPage dto) {
        //先获取一级
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        //过滤没有权限的目录
        list = AuthUtils.filter(list);

        boolean notBlank = StrUtil.isNotBlank(dto.getName());
        List<ChartPage> chartPages = chartPageService.list(new LambdaQueryWrapper<ChartPage>()
                .like(notBlank, ChartPage::getName, dto.getName())
                .orderByAsc(ChartPage::getSort));

        //过滤没有权限的报表
        List<ChartPage> auth = AuthUtils.filter(chartPages);

        list.forEach(e -> {
            AuthUtils.setOperation(e);
            List<ChartPage> sonList = auth.stream().filter(v -> v.getType().equals(e.getId())).collect(Collectors.toList());
            e.setChildren(sonList);
        });
        //判断是否存在搜索条件 如果存在需要过滤掉目录为空的数据
        if (notBlank) {
            list = list.stream().filter(e -> ObjectUtil.isNotEmpty(e.getChildren())).collect(Collectors.toList());
        }
        return R.ok(list);
    }


    @ApiOperation("获取一级菜单")
    @GetMapping("/first/menu")
    public R<List<SysMenu>> firstMenu() {
        List<SysMenu> list = sysMenuService.list();
        return R.ok(list);
    }


    @ApiOperation("添加一级菜单")
    @PostMapping("/add/menu")
    public R<SysMenu> add(@RequestBody SysMenu sysMenu) {
        //获取数量
        long i = sysMenuService.count() + 1;
        sysMenu.setSort(new BigDecimal(i).intValue());
        sysMenuService.save(sysMenu);
        return R.ok(sysMenu);
    }


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


    @ApiOperation("修改名称")
    @PostMapping("/update/menu")
    public R<SysMenu> update(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateById(sysMenu);
        AuthUtils.setOperation(sysMenu);
        return R.ok(sysMenu);
    }


    @ApiOperation("排序")
    @PutMapping("/move")
    public R<List<SysMenu>> move(@RequestBody ChartMoveDto chartMoveDto) {
        //获取数据
        SysMenu byId = sysMenuService.getById(chartMoveDto.getId());
        //获取当前目录下面的所有数据
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().ne(SysMenu::getId,chartMoveDto.getId()).orderByAsc(SysMenu::getSort));
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
            sysMenu.setSort(indexOf + i);
        }
        sysMenuService.updateBatchById(list);
        return R.ok(list);
    }

    @ApiOperation("获取所有的权限")
    @GetMapping("/menu/get/operation/list")
    public R<List<OperationType>> getAllOperation() {
        return R.ok(AuthUtils.getMenu());
    }
}
