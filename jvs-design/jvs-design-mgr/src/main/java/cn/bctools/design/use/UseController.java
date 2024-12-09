package cn.bctools.design.use;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.tree.Tree;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Api(tags = "获取设计渲染结构")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/base/use")
public class UseController {

    UseComponent useComponent;
    JvsAppService jvsAppService;

    /**
     * 获取轻应用菜单,返回创建人为自己的应用.并有权限的应用,不管是不是超级管理员.
     *
     * @return
     */
    @GetMapping("/menu")
    @ApiOperation("获取轻应用菜单")
    public R<List<Tree<Object>>> menu() {
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), ModeUtils.getMode());
        //树结构二次处理
        //获取应用
        if (ObjectNull.isNotNull(tree)) {
            tree = tree.stream()
                    .peek(e -> {
                        //获取第一级菜单,为了兼容多级菜单，这里处理判断不管是哪一层都直接判断是否是菜单
                        List<Tree<Object>> menu = e.getChildren();
                        treeType(menu, Boolean.valueOf(((HashMap) e.get("extend")).get("designRole").toString()));
                    })
                    .filter(e -> {
                        switch (ModeUtils.getMode()) {
                            case DEV:
                                //如果是开发，返回所有
                                return true;
                            default:
                                //如果是其它模式， 判断是否有下级，是否返回应用
                                return ObjectNull.isNotNull(e.getChildren());
                        }
                    })
                    .collect(Collectors.toList());
        }
        return R.ok(Optional.ofNullable(tree).orElseGet(ArrayList::new));
    }

    /**
     * 需要递归处理，如果下级是空，则将下级删除
     *
     * @param menu
     * @param designRole
     */
    private void treeType(List<Tree<Object>> menu, Boolean designRole) {
        if (ObjectNull.isNotNull(menu)) {
            for (Tree<Object> objectTree : menu) {
                objectTree.put("designRole", designRole);
                List<Tree<Object>> children = objectTree.getChildren();
                treeType(children, designRole);
            }
            //如果是目录的情况下，判断下级是否是空，如果是空的，则不返回此节点
            menu.removeIf(e -> (!designRole) && ObjectNull.isNull(e.getChildren()) && "directory".equals(((HashMap<?, ?>) e.get("extend")).get("type")));
        }
    }

}
