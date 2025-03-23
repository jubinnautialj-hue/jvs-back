package cn.bctools.data.factory.service;

import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryQueue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 数据etl 队列记录
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
public interface JvsDataFactoryQueueService extends IService<JvsDataFactoryQueue> {

    /**
     * 检查某一个数据集是否有任务正在执行
     *
     * @param dataFactory 数据集
     * @return 错误信息
     */
    String isTaskExec(JvsDataFactory dataFactory);

}
