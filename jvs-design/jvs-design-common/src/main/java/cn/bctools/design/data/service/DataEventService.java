package cn.bctools.design.data.service;

import cn.bctools.design.data.entity.DataEventPo;
import cn.bctools.design.data.fields.dto.DataEventSettingDto;
import cn.bctools.design.data.fields.enums.DataEventType;
import cn.bctools.rule.utils.html.RuleExecuteDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 数据事件
 *
 * @Author: GuoZi
 */
public interface DataEventService extends IService<DataEventPo> {

    /**
     * 查询指定设计的数据事件配置
     *
     *
     * @param appId
     * @param modelId  模型id
     * @param designId 设计id
     * @return 事件配置信息
     */
    DataEventSettingDto getEventList(String appId, String modelId, String designId);

    /**
     * 修改指定设计的数据事件配置
     *
     *
     * @param appId
     * @param modelId    模型id
     * @param designId   设计id
     * @param settingDto 配置信息
     * @return 修改后的配置信息
     */
    DataEventSettingDto updateEventList(String appId, String modelId, String designId, DataEventSettingDto settingDto);

    /**
     * 请求回调地址
     *
     * @param modelId  模型id
     * @param dataId   数据id
     * @param type     操作类型
     * @param data     数据内容
     * @param isBefore 是否为前置事件
     * @return 回调返回值数据
     */
    RuleExecuteDto callback(String modelId, String dataId, DataEventType type, Map<String, Object> data, boolean isBefore);

}

