package cn.bctools.data.factory.service.impl;

import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.enums.TaskTypeEnum;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.service.MqttService;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * mqtt 实现类
 *
 * @author Administrator
 */
@Service
@AllArgsConstructor
public class MqttServiceImpl implements MqttService {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final JvsDataFactoryService jvsDataFactoryService;

    @Override
    public Boolean checkSubTopicUse(String id) {
        long count = jvsDataFactoryService.count(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getTaskType, TaskTypeEnum.mqtt)
                .eq(JvsDataFactory::getEnable, Boolean.TRUE)
                .apply("JSON_EXTRACT(view_json, '$.inputDataSource.dataSourceStructureId') = {0}", id));
        return count > 0;
    }

    @Override
    public Boolean checkDataSourceUse(String dataSourceId) {
        long count = jvsDataFactoryService.count(new LambdaQueryWrapper<JvsDataFactory>().eq(JvsDataFactory::getTaskType, TaskTypeEnum.mqtt)
                .eq(JvsDataFactory::getEnable, Boolean.TRUE)
                .apply("JSON_EXTRACT(view_json, '$.inputDataSource.dataSourceId') = {0}", dataSourceId));
        return count > 0;
    }
}
