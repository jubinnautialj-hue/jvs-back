package cn.bctools.design.data.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.data.entity.DataLogPo;
import cn.bctools.design.data.fields.enums.DataEventType;

import java.util.List;
import java.util.Map;

/**
 * 数据变更记录
 *
 * @Author: GuoZi
 */
public interface DataLogService {

    /**
     * 保存数据变更记录
     *
     * @param dataId      数据id
     * @param data        数据内容
     * @param eventType   数据变更事件
     * @param dataModelId 模型Id
     * @return 当前数据版本
     */
    String saveLog(String dataModelId, String dataId, Map<String, Object> data, DataEventType eventType);

    /**
     * 保存数据变更记录
     *
     * @param dataId      数据id
     * @param data        数据内容
     * @param eventType   数据变更事件
     * @param userDto     用户信息
     * @param dataModelId 模型id
     * @return 当前数据版本
     */
    String saveLog(String dataModelId, String dataId, Map<String, Object> data, DataEventType eventType, UserDto userDto);

    /**
     * 保存数据变更记录
     *
     * @param dataId      数据id
     * @param data        数据内容
     * @param dataChange  变更记录
     * @param eventType   数据变更事件
     * @param dataModelId 模型ID
     * @return 当前数据版本
     */
    String saveLog(String dataModelId, String dataId, Map<String, Object> data, List<Object> dataChange, DataEventType eventType);

    /**
     * 保存数据变更记录
     *
     * @param dataId      数据id
     * @param data        数据内容
     * @param dataChange  变更记录
     * @param eventType   数据变更事件
     * @param userDto     用户信息
     * @param dataModelId 模型ID
     * @return 当前数据版本
     */
    String saveLog(String dataModelId, String dataId, Map<String, Object> data, List<Object> dataChange, DataEventType eventType, UserDto userDto);

    String saveLogBatch(String dataModelId, List<Object> data, DataEventType eventType, UserDto userDto);

    /**
     * 记录跟进记录
     *
     * @param dataModelId 模型 id
     * @param dataId      数据 id
     * @param data        提交的数据
     * @param userDto
     * @return
     */
    String follow(String dataModelId, String dataId, Map<String, Object> data, UserDto userDto);

    /**
     * 查询所有历史数据
     *
     * @param dataModelId 模型ID
     * @param dataId      数据id
     * @return 数据
     */
    List<DataLogPo> getLog(String dataModelId, String dataId);

    List<DataLogPo> follow(String dataModelId, String dataId);

    /**
     * 查询指定版本的历史数据
     *
     * @param dataModelId 模型ID
     * @param dataId      数据id
     * @param version     数据版本
     * @return 数据
     */
    DataLogPo getLog(String dataModelId, String dataId, String version);
}

