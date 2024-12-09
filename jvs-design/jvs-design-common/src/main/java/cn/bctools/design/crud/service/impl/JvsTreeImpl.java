package cn.bctools.design.crud.service.impl;

import cn.bctools.common.entity.po.TreePo;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.design.crud.entity.JvsTree;
import cn.bctools.design.crud.mapper.JvsTreeMapper;
import cn.bctools.design.crud.service.JvsTreeService;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : GaoZeXi
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsTreeImpl extends ServiceImpl<JvsTreeMapper, JvsTree> implements JvsTreeService {

    /**
     * 删除分类以及子集
     *
     * @param id 字典id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTree(String id) {
        JvsTree sysTree = this.getOne(Wrappers.query(new JvsTree().setUniqueName(id)));
        String parentId = sysTree.getParentId();
        String groupId = sysTree.getGroupId();
        // 删除当前节点以及子节点
        if (JvsTree.DICT_ID_ROOT.equals(parentId)) {
            this.remove(Wrappers.<JvsTree>lambdaQuery().eq(JvsTree::getGroupId, groupId));
        } else {
            List<String> childIds = this.getChild(id, null);
            this.remove(Wrappers.<JvsTree>lambdaQuery().in(JvsTree::getUniqueName, childIds));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkName(String name, String parentId) {
        long count = this.count(Wrappers.<JvsTree>lambdaQuery()
                .eq(JvsTree::getName, name)
                .eq(JvsTree::getParentId, parentId));
        if (count > 0) {
            log.error("[树形字典] 同一层级的字典名称不能重复, name: {}, id: {}", name, parentId);
            throw new BusinessException("同一层级的字典名称不能重复", name);
        }
    }

    @Override
    public Map<String, Object> getByUniqueName(String uniqueName) {
        JvsTree tree = this.getOne(Wrappers.<JvsTree>lambdaQuery().select(JvsTree::getGroupId).eq(JvsTree::getUniqueName, uniqueName));
        if (Objects.isNull(tree)) {
            return new HashMap<>(1);
        }
        String groupId = tree.getGroupId();
        List<JvsTree> treeList = list(Wrappers.<JvsTree>lambdaQuery().eq(JvsTree::getGroupId, groupId).isNotNull(JvsTree::getUniqueName));
        List<TreePo> treePos = treeList.stream()
                .map(e -> BeanCopyUtil.copy(e, TreePo.class).setExtend(e).setId(e.getUniqueName()))
                .collect(Collectors.toList());
        List<Tree<Object>> result = TreeUtils.tree(treePos, JvsTree.DICT_ID_ROOT);
        return result.get(0);
    }

    @Override
    public List<Tree<Object>> tree(String name) {
        List<JvsTree> list = list(Wrappers.<JvsTree>lambdaQuery()
                .like(StringUtils.isNotBlank(name), JvsTree::getName, name));
        List<TreePo> treePos = list.stream()
                .map(e -> BeanCopyUtil.copy(e, TreePo.class).setId(Optional.ofNullable(e.getUniqueName()).orElse(e.getId())).setExtend(JSONObject.parse(JSONObject.toJSONString(e))))
                .collect(Collectors.toList());
        List<Tree<Object>> tree = TreeUtils.tree(treePos, JvsTree.DICT_ID_ROOT);
        return tree;
    }

    /**
     * 递归查询所有的子集
     *
     * @param id  当前节点id
     * @param ids 子节点id集合
     */
    private List<String> getChild(String id, List<String> ids) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        if (id == null) {
            return null;
        }
        ids.add(id);
        List<JvsTree> list = list(Wrappers.query(new JvsTree().setParentId(id)));
        if (ObjectUtil.isNotEmpty(list)) {
            for (JvsTree sysTree : list) {
                String sysTreeId = sysTree.getId();
                getChild(sysTreeId, ids);
            }
        }
        return ids;
    }

}
