package cn.bctools.design.menu.util;

import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wl zxk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class JvsMenuVo extends TreePo {

    @ApiModelProperty(value = "应用ID")
    private String jvsAppId;
    @ApiModelProperty(value = "类型")
    private String type;
    @ApiModelProperty(value = "数据模型id")
    private String dataModelId;
    @ApiModelProperty(value = "图标")
    private String icon;
    @ApiModelProperty(value = "权限")
    private List<JSONObject> role;
    /**
     * 该字段仅三级菜单使用
     */
    @ApiModelProperty(value = "是否为最近30天内创建", example = "true")
    private Boolean isNew;

    /**
     * 是否能设计
     */
    Boolean designRole;
    /**
     * 当前的设计类型
     */
    DesignType design;
    /**
     * 快捷创建类型,同一个模型，只能一个设计类型挂了菜单不允许有两个相同的类型挂菜单
     */
    List<DesignType> designTypes = new ArrayList<>();
    @ApiModelProperty("页面链接")
    private String url;

    @ApiModelProperty("移动端显示")
    private Boolean mobileDisplay;

    @ApiModelProperty("PC端显示")
    private Boolean pcDisplay;

    public JvsMenuVo setDesignTypess(DesignType... designTypes) {
        this.designTypes = Arrays.stream(designTypes).collect(Collectors.toList());
        return this;
    }

//    @ApiModelProperty(value = "设计类型是否需要动态 添加 ", example = "true")
//    DesignUseMap designUseMap;

    public static List<Tree<Object>> tree(List<JvsMenuVo> list) {
        String name = Get.name(TreePo::getSort);
        return TreeUtil.build(list, "-1", new TreeNodeConfig().setWeightKey(name), (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setName(treeNode.getName());
            tree.setWeight(treeNode.getSort());
            tree.putExtra("extend", treeNode.getExtend());
        });

    }


}
