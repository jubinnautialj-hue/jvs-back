package cn.bctools.data.factory.consanguinity.view.service;

import cn.bctools.data.factory.consanguinity.view.dto.Neo4jDto;
import cn.bctools.data.factory.consanguinity.view.entity.ConsanguinityViewEntity;

import java.util.List;

/**
 * 血缘视图操作
 *
 * @author Administrator
 */
public interface ConsanguinityViewService {
    /**
     * 保存
     *
     * @param entity 血缘数据
     */
    void save(List<ConsanguinityViewEntity> entity);

    /**
     * 删除
     *
     * @param id 数据id
     */
    void delete(String id);

    /**
     * 数据集与数据源删除时校验是否存在使用方
     *
     * @param id   数据id
     * @param type 类型
     * @return 血缘视图
     */
    List<ConsanguinityViewEntity> check(String id, Integer type);


    /**
     * 刪除某个数据集的所有类型数据
     *
     * @param groupId 数据集id
     * @param type    类型
     */
    void deleteSource(String groupId, Integer type);

    /**
     * 同步血缘视图
     *
     * @param id 数据集id 同步单个
     */
    String syncConsanguinity(String id);

    /**
     * 获取
     *
     * @param groupId 数据集id
     * @return 血缘视图
     */
    Neo4jDto list(String groupId);
}
