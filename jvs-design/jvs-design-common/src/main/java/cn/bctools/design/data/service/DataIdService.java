package cn.bctools.design.data.service;

import cn.bctools.design.data.entity.DataIdPo;
import cn.bctools.design.data.fields.enums.DesignType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

/**
 * 数据自增序号
 *
 * @Author: GuoZi
 */
public interface DataIdService extends IService<DataIdPo> {

    /**
     * 获取递增序号
     *
     * @param designType 设计套件类型
     * @param designId 设计id
     * @param size    序号数量
     * @return 当前最新序号
     */
    DataIdPo nextId(DesignType designType, String designId, int size);

    /**
     * 重置序号
     *
     * @param designType 设计套件类型
     * @param designId 设计id
     * @param newId   重置后的序号
     * @return 当前最新序号
     */
    DataIdPo resetId(DesignType designType, String designId, int newId);

    /**
     * 同步数据序号
     *
     * @param designType 设计套件类型
     * @param designIds 设计id集合
     */
    void syncUpdateDataId(DesignType designType, Collection<String> designIds);

    /**
     * 删除递增序号
     *
     * @param designType 设计套件类型
     * @param designIds 设计id集合
     */
    void removeId(DesignType designType, Collection<String> designIds);
}

