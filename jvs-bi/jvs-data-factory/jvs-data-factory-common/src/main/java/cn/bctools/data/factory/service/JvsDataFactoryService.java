package cn.bctools.data.factory.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.dto.*;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.util.MqttUtil;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 数据etl 服务类
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
public interface JvsDataFactoryService extends IService<JvsDataFactory> {

    /**
     * 获取是否可以执行逻辑防止 数据混乱
     *
     * @param dataFactoryId 设计id
     * @return 是否
     */
    Boolean getDataFactoryIsLock(String dataFactoryId);

    /**
     * 获取导出时的数据集
     *
     * @param figureOutList 已经处理完成的数据id
     * @param ids           需要处理的
     * @return 是否
     */
    List<DownGetDataFactoryDto> downGetDataFactoryId(List<String> ids, List<String> figureOutList);


    /**
     * 获取是否可以执行逻辑防止 数据混乱
     *
     * @param dataFactoryId 设计id
     * @param nodeId        节点id
     * @return 是否
     */
    Boolean getSyncOdsDataIsLock(String dataFactoryId, String nodeId);

    /**
     * 数据获取与数据恢复
     *
     * @param id 数据id
     * @return 数据集数据
     */
    JvsDataFactory upGetOrDataRecover(String id);


    /**
     * 解锁
     *
     * @param dataFactoryId 设计id
     */
    void unLockDataFactory(String dataFactoryId);

    /**
     * 解锁
     *
     * @param dataFactoryId 设计id
     * @param nodeId        节点id
     */
    void unLockSyncOdsLock(String dataFactoryId, String nodeId);


    /**
     * 入队列
     *
     * @param dataFactory       数据本身
     * @param queueTaskTypeEnum 需要生成的任务类型 前置后置本身
     * @param userDto           用户信息
     * @param batchId           批次id 用于确认那些任务是同一批次
     * @param operateMethod     执行模式 手动自动
     */
    void sendQueue(JvsDataFactory dataFactory, QueueTaskTypeEnum queueTaskTypeEnum, UserDto userDto, String batchId, OperateMethodEnum operateMethod);

    /**
     * 启用时通知 数据集同步结构
     *
     * @param factory 数据集主表数据
     */
    void syncTableStructure(JvsDataFactory factory);

    /**
     * 复制一个数据集
     *
     * @param copyDto 数据集复制信息
     */
    JvsDataFactory copy(CopyDto copyDto);

    /**
     * 删除一个数据集
     *
     * @param id 数据集id
     */
    JvsDataFactory delete(String id);

    /**
     * 权限获取
     *
     * @param id 数据集id
     */
    JvsDataFactory auth(String id);

    /**
     * 获取列 所有地方获取列的结构 都应该走此方法
     *
     * @param id 数据集id
     */
    List<DataSourceField> getColumn(String id);


    /**
     * 执行mqtt定时任务
     *
     * @param id 数据集id
     */
    void execMqttTimedTask(String id);

    /**
     * 执行mqtt任务
     *
     * @param dataFactoryMqttViewDto 执行数据
     * @param mqttUtil mqtt对象
     */
    BigDecimal execMqttTask(DataFactoryMqttViewDto dataFactoryMqttViewDto, MqttUtil mqttUtil);

    /**
     * 执行api定时任务
     *
     * @param id     数据集id
     */
    void execApiTimedTask(String id);


    /**
     * 执行api任务
     *
     * @param dataFactoryApiViewDto api入参
     */
    BigDecimal execApi(DataFactoryApiViewDto dataFactoryApiViewDto);

    /**
     * 获取实时任务的数据源id
     *
     * @param list 数据集数据
     * @return 数据集id
     */
    List<String> getTimeTaskDataSourceId(List<JvsDataFactory> list);

    /**
     * 获取数据获取的查询条件
     *
     * @param id 数据集id
     */
    ColumnWhereDto getRowWhere(String id);

}
