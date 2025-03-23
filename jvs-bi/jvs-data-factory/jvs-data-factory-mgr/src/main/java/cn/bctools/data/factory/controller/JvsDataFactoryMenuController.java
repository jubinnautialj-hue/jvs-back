package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.data.factory.dto.FactoryMoveDto;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryMenu;
import cn.bctools.data.factory.enums.MenuOperationEnum;
import cn.bctools.data.factory.enums.OperationEnum;
import cn.bctools.data.factory.service.JvsDataFactoryMenuService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.util.AuthUtil;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "数据集-菜单")
@RestController
@AllArgsConstructor
@RequestMapping("/jvs/data/factory/type")
public class JvsDataFactoryMenuController {
    private final JvsDataFactoryMenuService jvsDataFactoryMenuService;
    private final JvsDataFactoryService jvsDataFactoryService;
    private final AuthUtil<OperationEnum, JvsDataFactory> jvsDataFactoryAuthUtil;
    private final AuthUtil<MenuOperationEnum, JvsDataFactoryMenu> jvsDataFactoryMenuAuthUtil;

    @Log
    @ApiOperation("菜单")
    @GetMapping("/menu")
    public R<List<JvsDataFactoryMenu>> menu(JvsDataFactory dto) {
        //先获取一级
        List<JvsDataFactoryMenu> list = jvsDataFactoryMenuService.list(new LambdaQueryWrapper<JvsDataFactoryMenu>().orderByAsc(JvsDataFactoryMenu::getSort));
        //过滤菜单
        list = jvsDataFactoryMenuAuthUtil.auth(list, null, Arrays.asList(MenuOperationEnum.values()));
        if (list.isEmpty()) {
            return R.ok();
        }
        boolean notBlank = StrUtil.isNotBlank(dto.getName());
        List<JvsDataFactory> jvsDataFactors = jvsDataFactoryService.list(new LambdaQueryWrapper<JvsDataFactory>()
                .select(JvsDataFactory.class, e -> !e.getProperty().contains(Get.name(JvsDataFactory::getViewJson)))
                        .in(JvsDataFactory::getType,list.stream().map(JvsDataFactoryMenu::getId).collect(Collectors.toList()))
                .like(notBlank, JvsDataFactory::getName, dto.getName())
                .orderByAsc(JvsDataFactory::getSort));
        //权限过滤
        List<JvsDataFactory> auth = jvsDataFactoryAuthUtil.auth(jvsDataFactors, null, Arrays.asList(OperationEnum.values()));
        list.forEach(e -> {
            List<JvsDataFactory> sonList = auth.stream().filter(v -> v.getType().equals(e.getId())).collect(Collectors.toList());
            e.setChildren(sonList);
        });
        //判断是否存在搜索条件 如果存在需要过滤掉目录为空的数据
        if (notBlank) {
            list = list.stream().filter(e -> !e.getChildren().isEmpty()).collect(Collectors.toList());
        }
        return R.ok(list);
    }

    @Log
    @ApiOperation("添加一级菜单")
    @PostMapping("/add/menu")
    public R<JvsDataFactoryMenu> add(@RequestBody JvsDataFactoryMenu jvsDataFactoryMenu) {
        //获取数量
        long i = jvsDataFactoryMenuService.count() + 1;
        jvsDataFactoryMenu.setSort(i);
        jvsDataFactoryMenuService.save(jvsDataFactoryMenu);
        return R.ok(jvsDataFactoryMenu);
    }

    @Log
    @ApiOperation("删除")
    @DeleteMapping("/delete/menu/{id}")
    public R deleteMenu(@PathVariable String id) {
        //判断下面是否存在其他资源
        long count = jvsDataFactoryService.count(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getType, id));
        if (count > BigDecimal.ROUND_UP) {
            return R.failed("此目前存在其它资源无法删除");
        }
        jvsDataFactoryMenuService.removeById(id);
        return R.ok();
    }

    @Log
    @ApiOperation("修改名称")
    @PostMapping("/update/menu")
    public R<JvsDataFactoryMenu> update(@RequestBody JvsDataFactoryMenu jvsDataFactoryMenu) {
        jvsDataFactoryMenuService.updateById(jvsDataFactoryMenu);
        jvsDataFactoryMenuAuthUtil.auth(jvsDataFactoryMenu, null, Arrays.asList(MenuOperationEnum.values()));
        return R.ok(jvsDataFactoryMenu);
    }

    @Log
    @ApiOperation("排序")
    @PutMapping("/move")
    public R<List<JvsDataFactoryMenu>> move(@RequestBody FactoryMoveDto dto) {
        //获取数据
        JvsDataFactoryMenu byId = jvsDataFactoryMenuService.getById(dto.getId());
        //获取当前目录下面的所有数据
        List<JvsDataFactoryMenu> list = jvsDataFactoryMenuService.list(new LambdaQueryWrapper<JvsDataFactoryMenu>().ne(JvsDataFactoryMenu::getId, dto.getId()).orderByAsc(JvsDataFactoryMenu::getSort));
        int indexOf = 0;
        if (StrUtil.isNotBlank(dto.getNextId())) {
            indexOf = list.stream().map(JvsDataFactoryMenu::getId).collect(Collectors.toList()).indexOf(dto.getNextId());
        }
        Boolean aBoolean = Optional.ofNullable(dto.getIsFront()).orElse(false);
        if (aBoolean) {
            indexOf += 1;
        }
        //插入数据
        list.add(indexOf, byId);
        //只更新需要更新排序的数据 节省资源
        List<JvsDataFactoryMenu> updateList = new ArrayList<>();
        //重新排序
        for (int i = 0; i < list.size(); i++) {
            JvsDataFactoryMenu jvsDataFactoryMenu = list.get(i);
            if (!NumberUtil.equals(i, jvsDataFactoryMenu.getSort().intValue())) {
                jvsDataFactoryMenu.setSort((long) i);
                updateList.add(jvsDataFactoryMenu);
            }
        }
        jvsDataFactoryMenuService.updateBatchById(updateList);
        return R.ok(list);
    }
}
