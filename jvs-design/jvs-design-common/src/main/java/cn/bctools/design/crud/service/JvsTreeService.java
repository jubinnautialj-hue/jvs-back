package cn.bctools.design.crud.service;

import cn.bctools.design.crud.entity.JvsTree;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author : GaoZeXi
 */
public interface JvsTreeService extends IService<JvsTree> {

    /**
     * 删除树形字典
     *
     * @param id 字典id
     */
    void deleteTree(String id);

    /**
     * 字典名称查重校验
     *
     * @param name     字典名称
     * @param parentId 上级节点id
     */
    void checkName(String name, String parentId);

    /**
     * 根据字典标识获取字典树
     *
     * @param uniqueName 字典唯一标识
     * @return 字典树
     */
    Map<String, Object> getByUniqueName(String uniqueName);

    Map<String, Object> getByUniqueName(String uniqueName, String rootId);

    List<String> getByUniqueNameIds(String uniqueName, Object rootId);

    /**
     * 根据名称返回次名称的所有树字段
     *
     * @param name
     * @return
     */
    List<Tree<Object>> tree(String name);
}
