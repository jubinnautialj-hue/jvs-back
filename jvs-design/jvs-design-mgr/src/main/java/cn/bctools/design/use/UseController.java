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
        log.info("[轻应用菜单接口] 开始获取轻应用菜单，host={}, mode={}", host, mode);
        
        //获取域名匹配的应用
        JvsApp jvsApp = null;
        if (ObjectNull.isNotNull(host)) {
            log.info("[轻应用菜单接口] 开始匹配域名，host={}", host);
            List<String> identificationDomain = jvsSystemConfig.getIdentificationDomain();
            log.info("[轻应用菜单接口] 系统配置的域名列表，identificationDomain={}", identificationDomain);
            
            if (ObjectNull.isNotNull(identificationDomain)) {
                for (String e : identificationDomain) {
                    if (e.equals(host)) {
                        //匹配域名,如果匹配到查询应用是哪一个
                        String key = host.replaceAll(jvsSystemConfig.getDomain(), "").replaceAll("\\.", "");
                        log.info("[轻应用菜单接口] 域名匹配成功，host={}, authorizationKey={}", host, key);
                        jvsApp = jvsAppService.getOne(Wrappers.query(new JvsApp().setAuthorizationKey(key)));
                        log.info("[轻应用菜单接口] 查询应用结果，jvsApp={}", jvsApp != null ? jvsApp.getId() + "-" + jvsApp.getName() : "null");
                    }
                }
            }
        }
        
        //模式判断
        if (ObjectNull.isNull(mode)) {
            mode = ModeUtils.getMode();
            log.info("[轻应用菜单接口] 从上下文获取模式，mode={}", mode);
        }
        
        String userId = ModeUtils.getRealUser().getId();
        boolean isMobile = IpUtil.isMobile();
        log.info("[轻应用菜单接口] 用户信息，userId={}, isMobile={}, mode={}, jvsAppId={}", 
                userId, isMobile, mode, jvsApp != null ? jvsApp.getId() : "null");
        
        AppVersionTypeEnum finalMode = mode;
        List<Tree<Object>> tree = useComponent.menu("", userId, isMobile, finalMode, jvsApp).getKey();
        log.info("[轻应用菜单接口] useComponent.menu返回结果，tree数量={}", tree != null ? tree.size() : 0);
        
        if (tree == null || tree.isEmpty()) {
            log.warn("[轻应用菜单接口] useComponent.menu返回的tree为空，userId={}, mode={}", userId, finalMode);
        }
        //树结构二次处理
        //获取应用
        String finalAppid = Optional.ofNullable(jvsApp).map(JvsApp::getId).orElseGet(() -> "");
        log.info("[轻应用菜单接口] 开始树结构二次处理，finalAppid={}", finalAppid);
        
        if (ObjectNull.isNull(tree)) {
            log.warn("[轻应用菜单接口] tree为null，初始化为空列表");
            tree = new ArrayList<>();
        }
        
        log.info("[轻应用菜单接口] 开始过滤和处理菜单树，处理前数量={}", tree.size());
        
        tree = tree.stream()
                .filter(e -> {
                    if (ObjectNull.isNotNull(finalAppid)) {
                        //过滤有只要这一条数据
                        boolean match = e.getId().equals(finalAppid);
                        log.debug("[轻应用菜单接口] 过滤应用，treeId={}, finalAppid={}, match={}", e.getId(), finalAppid, match);
                        return match;
                    } else {
                        //返回所有
                        return true;
                    }
                })
                .peek(e -> {
                    //获取第一级菜单,为了兼容多级菜单，这里处理判断不管是哪一层都直接判断是否是菜单
                    List<Tree<Object>> menu = e.getChildren();
                    log.debug("[轻应用菜单接口] 处理应用菜单，appId={}, children数量={}", e.getId(), menu != null ? menu.size() : 0);
                    
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
                    boolean hasChildren = ObjectNull.isNotNull(e.getChildren());
                    if (!hasChildren) {
                        log.debug("[轻应用菜单接口] 非开发模式下过滤无下级菜单的应用，appId={}, mode={}", e.getId(), finalMode);
                    }
                    return hasChildren;
                })
                .collect(Collectors.toList());
        
        log.info("[轻应用菜单接口] 菜单树处理完成，最终数量={}", tree.size());
        
        if (tree.isEmpty()) {
            log.warn("[轻应用菜单接口] 最终返回的tree为空，userId={}, mode={}, finalAppid={}", userId, finalMode, finalAppid);
        }
        
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
