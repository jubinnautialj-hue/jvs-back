package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.dto.DataDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.service.impl.FlowDynamicDataServiceImpl;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 工作流数据模型交互
 */
public interface FlowDynamicDataService {

    /**
     * 将数据保存到数据模型
     *
     * @param appId       应用id
     * @param dataModelId 数据模型id
     * @param data        数据
     * @return 数据对象
     */
    DataDto saveModelData(String appId, String dataModelId, JSONObject data);

    /**
     * 保存数据
     * <p>
     * 只保存数据，不做其它额外处理
     *
     * @param modelId 数据模型id
     * @param data    数据内容
     * @return 新增后的数据id
     */
    String onlySave(String modelId, Map<String, Object> data);

    /**
     * 修改数据模型的数据
     *
     * @param appId       应用id
     * @param data        数据
     * @param dataModelId 数据模型id
     * @param dataId      数据id
     * @return 数据对象
     */
    DataDto updateModelData(String appId, JSONObject data, String dataModelId, String dataId);

    /**
     * 修改数据
     * <p>
     * 只保存数据，不做其它额外处理
     *
     * @param modelId 模型id
     * @param dataId  数据id
     * @param data    变更数据值
     */
    void onlyUpdate(String modelId, String dataId, Map<String, Object> data);

    /**
     * 批量修改数据
     *
     * @param modelId  模型id
     * @param dataList 数据集合
     */
    void updateBatchById(String modelId, List<Map<String, Object>> dataList);

    /**
     * 查询单条数据
     *
     * @param dataModelId 数据模型id
     * @param dataId      数据id
     * @return 数据
     */
    Map<String, Object> querySingle(String dataModelId, String dataId);

    /**
     * 获取数据触发的流程任务信息
     *
     * @param dataId 数据id
     * @return 工作流任务信息
     */
    FlowDynamicDataServiceImpl.FlowTaskModelData getFlowTaskData(String dataId);


    /**
     * 获取多个数据触发的流程任务信息
     *
     * @param dataId 数据id集合
     * @return 工作流任务信息
     */
    JSONObject getFlowTaskDataObj(String dataId);


    /**
     * 获取多个数据触发的流程任务信息
     *
     * @param dataIds 数据id集合
     * @return Map<数据id, 工作流任务信息>
     */
    Map<String, JSONObject> getMltipleFlowTaskData(List<String> dataIds);


    /**
     * 保存流程任务信息到模型数据
     *
     * @param flowTasks 流程任务集合
     */
    void saveTaskToModel(List<FlowTask> flowTasks);

    /**
     * 数据对象转Map结构数据
     * <p>
     * 并处理回显内容
     * 此操作，如果存在表单或选项卡数据，需要进行递归解析
     * 默认都要进行脱敏处理
     *
     * @param data    数据对象
     * @param appId   应用 id
     * @param modelId 设计id
     * @return Map数据
     */
    Map<String, Object> paresMapWithEcho(String appId, JSONObject data, String modelId);

}
