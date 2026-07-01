package cn.bctools.design.identification.service;

import cn.bctools.design.identification.entity.Identification;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * The interface Identification service.
 *
 * @author zhuxiaokang  自定义设计标识 <p> 自定义页面开发，可根据自定义标识符访问设计|数据； 租户级全局唯一标识
 */
public interface IdentificationService extends IService<Identification> {


    /**
     * 租户下是否已存在指定标识符
     *
     * @param whetherInsert true-新增操作，false-修改操作
     * @param appId         应用id
     * @param identifier    标识符
     * @param designId      设计id
     */
    void checkExistsIdentifier(boolean whetherInsert, String appId, String identifier, String designId);

    /**
     * 查询指定标识符配置
     *
     * @param identifiers 标识符集合
     * @return true -有标识已存在，false-所有标识都不存在
     */
    List<Identification> findByIdentifiers(List<String> identifiers);

    /**
     * 修改设计名称
     *
     * @param designId   设计id
     * @param designName 设计名称
     */
    void updateDesignName(String designId, String designName);

    /**
     * 获取逻辑的标识，匹配url地址
     *
     * @param ruleIdentification the rule identification
     * @param appId              the app id
     * @param map                the map
     * @return by identifier rule
     */
    Identification getByIdentifierRule(String ruleIdentification, String appId, Map<String, Object> map);

    /**
     * 获取应用标识
     *
     * @param appIdentification the app identification
     * @return by identifier app
     */
    Identification getByIdentifierApp(String appIdentification);

    /**
     * 获取当前模式下所有的标识,不同模式下标识是不一样的
     *
     * @param identifiers 所有的标识
     * @return the identification model
     */
    List<Identification> getIdentificationModel(String... identifiers);

}
