package cn.bctools.data.factory.source.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @description 数据源配置信息
 */
public interface DataSourceService extends IService<DataSource> {

    /**
     * 同步数据源的表结构
     *
     * @param dataSource 数据源信息
     * @param tenantId   租户id
     */
    void syncTableStructure(DataSource dataSource, String tenantId, UserDto userDto);

    /**
     * 读取数据
     * 现在只有excel 有这种情况
     *
     * @param readDataPo 数据源信息
     * @param tenantId   tenantId
     */
    void syncRead(ExcelReadDataPo readDataPo, String tenantId);

    /**
     * 添加数据集数据源 防止 不同租户  会没有默认数据
     */
    void saveDataFactory();

    /**
     * 添加数据结构 或者更改
     *
     * @param dataSourceStructure 结构数据
     */
    void updateDataFactoryStructure(DataSourceStructure dataSourceStructure);

    /**
     * 同步数据源的表结构
     *
     * @param json               数据源信息
     * @param dataSourceTypeEnum 当前数据源类型
     */
    void check(String json, DataSourceTypeEnum dataSourceTypeEnum);
    /**
     * 上传时数据获取与数据恢复
     *
     * @param id 数据id
     * @return 当前数据量
     */
    Long upGetOrDataRecover(String id);
    /**
     * 获取数据
     *
     * @param dataSource          数据源信息
     * @param dataSourceStructure 表对象
     */
    Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, Long size, Long current);

    /**
     * 获取数据条数
     *
     * @param dataSource          数据源信息
     * @param dataSourceStructure 表名称
     */
    Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure);


    /**
     * 获取表结构
     *
     * @param dataSource          数据源信息
     * @param dataSourceStructure 表对象
     */
    List<DataSourceStructure.Structure> getStructure(DataSource dataSource, DataSourceStructure dataSourceStructure);

    /**
     * 检查名称是否重复
     * @param name 名称
     * @param dataSourceId 数据源id
     */
    void duplicateName(String name,String dataSourceId);

}
