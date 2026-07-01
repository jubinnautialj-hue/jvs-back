package cn.bctools.design.use;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.web.utils.IpUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    JvsSystemConfig jvsSystemConfig;

    /**
     * 获取轻应用菜单,返回创建人为自己的应用.并有权限的应用,不管是不是超级管理员.
     *
     * @return
     */
    @GetMapping("/menu")
    @ApiOperation("获取轻应用菜单")
    public R<List<Tree<Object>>> menu(@RequestHeader(value = "host", required = false) String host,
                                      @RequestParam(value = "mode", required = false) AppVersionTypeEnum mode) {
        //获取域名匹配的应用
        JvsApp jvsApp = null;
        if (ObjectNull.isNotNull(host)) {
            List<String> identificationDomain = jvsSystemConfig.getIdentificationDomain();
            if (ObjectNull.isNotNull(identificationDomain)) {
                for (String e : identificationDomain) {
                    if (e.equals(host)) {
                        //匹配域名,如果匹配到查询应用是哪一个
                        String key = host.replaceAll(jvsSystemConfig.getDomain(), "").replaceAll("\\.", "");
                        jvsApp = jvsAppService.getOne(Wrappers.query(new JvsApp().setAuthorizationKey(key)));
                    }
                }
            }
        }
        //模式判断
        if (ObjectNull.isNull(mode)) {
            mode = ModeUtils.getMode();
        }
        AppVersionTypeEnum finalMode = mode;
        List<Tree<Object>> tree = useComponent.menu("", ModeUtils.getRealUser().getId(), IpUtil.isMobile(), finalMode, jvsApp).getKey();
        //树结构二次处理
        //获取应用
        String finalAppid = Optional.ofNullable(jvsApp).map(JvsApp::getId).orElseGet(() -> "");
        if (ObjectNull.isNull(tree)) {
            tree = new ArrayList<>();
        }
        tree = tree.stream()
                .filter(e -> {
                    if (ObjectNull.isNotNull(finalAppid)) {
                        //过滤有只要这一条数据
                        return e.getId().equals(finalAppid);
                    } else {
                        //返回所有
                        return true;
                    }
                })
                .peek(e -> {
                    //获取第一级菜单,为了兼容多级菜单，这里处理判断不管是哪一层都直接判断是否是菜单
                    List<Tree<Object>> menu = e.getChildren();
                    if (ObjectNull.isNull(finalAppid)) {
                        treeType(menu, Boolean.valueOf(((HashMap) e.get("extend")).get("designRole").toString()));
                    } else {
                        treeType(menu, false);
                    }
                })
                .filter(e -> {
                    if (Objects.requireNonNull(finalMode) == AppVersionTypeEnum.DEV) {//如果是开发，返回所有
                        return true;
                    }//如果是其它模式， 判断是否有下级，是否返回应用
                    return ObjectNull.isNotNull(e.getChildren());
                })
                .collect(Collectors.toList());
        return R.ok(Optional.of(tree).orElseGet(ArrayList::new));
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
