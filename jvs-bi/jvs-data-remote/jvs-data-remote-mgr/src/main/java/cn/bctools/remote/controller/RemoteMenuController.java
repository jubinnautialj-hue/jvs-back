package cn.bctools.remote.controller;

import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.remote.dto.MoveDto;
import cn.bctools.remote.enums.MenuOperationEnum;
import cn.bctools.remote.enums.RemoteOperationEnum;
import cn.bctools.remote.po.JvsDataRemoteServer;
import cn.bctools.remote.po.SysMenu;
import cn.bctools.remote.service.JvsDataRemoteServerService;
import cn.bctools.remote.service.SysMenuService;
import cn.bctools.remote.utils.RemoteAuthUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "数据服务-菜单")
@RestController
@AllArgsConstructor
@RequestMapping("/remote/sys")
@Slf4j
public class RemoteMenuController {
    private final SysMenuService sysMenuService;
    private final JvsDataRemoteServerService jvsDataRemoteServerService;
    private final RemoteAuthUtil<RemoteOperationEnum, JvsDataRemoteServer> remoteAuthUtil;
    private final RemoteAuthUtil<MenuOperationEnum, SysMenu> remoteMenuAuthUtil;

    @Log
    @ApiOperation("菜单")
    @GetMapping("/menu")
    public R<List<SysMenu>> menu(JvsDataRemoteServer dto) {
        //先获取一级
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        list = remoteMenuAuthUtil.auth(list, null, Arrays.asList(MenuOperationEnum.values()));
        if (list.isEmpty()) {
            return R.ok(list);
        }
        List<JvsDataRemoteServer> jvsDataRemoteServers = jvsDataRemoteServerService.list(new LambdaQueryWrapper<JvsDataRemoteServer>()
                .like(StrUtil.isNotBlank(dto.getName()), JvsDataRemoteServer::getName, dto.getName())
                .in(JvsDataRemoteServer::getType, list.stream().map(SysMenu::getId).collect(Collectors.toList()))
                .orderByAsc(JvsDataRemoteServer::getSort));
        List<JvsDataRemoteServer> auth = remoteAuthUtil.auth(jvsDataRemoteServers, null, Arrays.asList(RemoteOperationEnum.values()));
        list.stream().sorted(Comparator.comparing(SysMenu::getSort)).forEach(e -> {
            List<JvsDataRemoteServer> sonList = auth.stream().filter(v -> v.getType().equals(e.getId())).collect(Collectors.toList());
            long count = jvsDataRemoteServers.stream().filter(v -> v.getType().equals(e.getId())).count();
            e.setCount(count);
            e.setChildren(sonList);
        });
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
        //获取数量
        long i = sysMenuService.count() + 1;
        sysMenu.setSort(i);
        sysMenuService.save(sysMenu);
        return R.ok(sysMenu);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/delete/menu/{id}")
    public R deleteMenu(@PathVariable String id) {
        //判断下面是否存在其他资源
        long count = jvsDataRemoteServerService.count(new LambdaQueryWrapper<JvsDataRemoteServer>().eq(JvsDataRemoteServer::getType, id));
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
        return R.ok(sysMenu);
    }

    @Log
    @ApiOperation("排序")
    @PutMapping("/move")
    public R<List<SysMenu>> move(@RequestBody MoveDto moveDto) {
        //获取数据
        SysMenu byId = sysMenuService.getById(moveDto.getId());
        //获取当前目录下面的所有数据
        List<SysMenu> list = sysMenuService.list(new LambdaQueryWrapper<SysMenu>().ne(SysMenu::getId, moveDto.getId()).orderByAsc(SysMenu::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(moveDto.getFrontId())) {
            indexOf = list.stream().map(SysMenu::getId).collect(Collectors.toList()).indexOf(moveDto.getFrontId()) + 1;
        }
        //插入数据
        list.add(indexOf, byId);
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort((long) i);
        }
        sysMenuService.updateBatchById(list);
        return R.ok(list);
    }
}
