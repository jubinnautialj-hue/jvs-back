package cn.bctools.data.factory.source.service;

import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author admin
 * @description 数据源配置信息
 */
public interface DataSourceStructureService extends IService<DataSourceStructure> {


    /**
     * 单个模型
     *
     * @param dataSource          数据源
     * @param datasourceStructure 数据表结构
     * @return 表结构 {@link DataSourceStructure.Structure}
     */
    List<DataSourceStructure.Structure> getDataSourceStructure(DataSource dataSource, DataSourceStructure datasourceStructure);


    /**
     * 保存当个api接口
     *
     * @param datasourceStructure 数据表结构
     */
    void saveOrUpdateStructure(DataSourceStructure datasourceStructure);


}
