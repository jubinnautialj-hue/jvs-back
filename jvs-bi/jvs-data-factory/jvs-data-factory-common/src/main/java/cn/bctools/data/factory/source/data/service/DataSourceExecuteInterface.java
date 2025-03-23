package cn.bctools.data.factory.source.data.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.source.data.po.ReadDataPo;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateSeaTunnelJsonParameterPo;
import cn.bctools.data.factory.source.data.sync.plugin.sea.tunnel.api.dto.SeaTunnelSubmitJobDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.database.entity.po.BasalPo;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
public interface DataSourceExecuteInterface<T extends ReadDataPo> {

    /**
     * 是否可以开启实时数据
     *
     * @param dataSource 数据源信息
     * @return 是否支持开启实时同步数据
     */
    default Boolean realTimeIsOpen(DataSource dataSource) {
        return Boolean.FALSE;
    }

    /**
     * 上传数据源
     *
     * @param dataSource 数据源信息
     * @param userDto    当前操作用户
     */
    default void up(DataSource dataSource, UserDto userDto) {
        DataSourceService dataSourceService = SpringContextUtil.getBean(DataSourceService.class);
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        String tenantId = TenantContextHolder.getTenantId();
        dataSource.setCreateBy(userDto.getRealName());
        dataSource.setCreateTime(null);
        dataSource.setCreateById(userDto.getId());
        dataSource.setUpdateBy(userDto.getRealName());
        dataSource.setTenantId(tenantId);
        dataSource.setUpdateTime(null);
        TenantContextHolder.clear();
        long count = dataSourceService.upGetOrDataRecover(dataSource.getId());
        if (count > 0) {
            dataSourceService.updateById(dataSource);
        } else {
            dataSourceService.save(dataSource);
        }
        //字段信息保存
        List<DataSourceStructure> children = dataSource.getChildren();
        if (!children.isEmpty()) {
            children.stream().peek(e -> e.setTenantId(tenantId).setRealTimeIsOpen(Boolean.FALSE)).collect(Collectors.toList());
            List<String> ids = children.stream().map(DataSourceStructure::getId).collect(Collectors.toList());
            //删除原来的表结构
            dataSourceStructureService.removeByIds(ids);
            //重新保存
            dataSourceStructureService.saveBatch(children);
        }
    }

    /**
     * 获取表结构
     * 获取整个数据源的表结构
     *
     * @param dataSource 数据源
     * @return 集合 {@link  DataSourceStructure}
     */
    List<DataSourceStructure> syncTableStructure(DataSource dataSource);


    /**
     * 删除数据源
     *
     * @param id 数据源 id
     */
    default void removeDataSource(String id) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        DataSourceService dataSourceService = SpringContextUtil.getBean(DataSourceService.class);
        dataSourceStructureService.remove(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, id));
        dataSourceService.removeById(id);
    }

    /**
     * 删除数据源其中的表结构
     *
     * @param id 数据源结构id
     */
    default void removeDataSourceStructure(String id) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        dataSourceStructureService.remove(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getId, id));
    }

    /**
     * 数据读取 目前只有excel 数据源是支持数据读取的
     *
     * @param readData 数据读取的入参
     */
    default void read(T readData) {
        throw new BusinessException("此数据源,不支持数据读取");
    }

    /**
     * 获取此类型的所有可选数据源
     *
     * @param dataSourceType 数据源类型
     * @return DataSourceTableDto {@link  DataSource}
     */
    default List<DataSource> getDataBase(DataSourceTypeEnum dataSourceType) {
        DataSourceService bean = SpringContextUtil.getBean(DataSourceService.class);
        return bean.list(new LambdaQueryWrapper<DataSource>()
                .select(DataSource::getSourceType, DataSource::getRole, DataSource::getRoleType, DataSource::getSourceName, DataSource::getId, BasalPo::getCreateById)
                .eq(DataSource::getSourceType, dataSourceType));
    }


    /**
     * 获取表名称
     *
     * @param id 数据源
     * @return {@link  DataSourceStructure}
     */
    default List<DataSourceStructure> getTableNames(String id) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        List<DataSourceStructure> list = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>()
                .eq(DataSourceStructure::getCheckIs, Boolean.TRUE)
                .eq(DataSourceStructure::getDataSourceId, id)
                .orderByDesc(DataSourceStructure::getCreateTime));
        return list;
    }

    /**
     * 获取单表结构 只包含数据结构
     *
     * @param dataSourceStructure 数据表数据
     * @return DataSourceTableDto {@link  DataSourceStructure.Structure}
     */
    default List<DataSourceStructure.Structure> getTableStructure(DataSourceStructure dataSourceStructure) {
        DataSourceStructureService dataSourceStructureService = SpringContextUtil.getBean(DataSourceStructureService.class);
        DataSourceStructure one = dataSourceStructureService.getById(dataSourceStructure.getId());
        Assert.notNull(one, () -> new BusinessException("数据不存在"));
        return JSONObject.parseObject(JSONObject.toJSONString(one), DataSourceStructure.class).getStructure()
                .parallelStream()
                .peek(e -> e.setFrom(one.getId()))
                .collect(Collectors.toList());
    }


    /**
     * 生成数据源工厂 并不是 所有数据源都需要
     *
     * @param dataSource 数据源
     * @return 是否成功
     */
    default Boolean init(DataSource dataSource) {
        return true;
    }

    /**
     * 获取数据
     *
     * @param dataSource          数据源
     * @param dataSourceStructure 表对象
     * @param size                获取条数 如果为0就是获取全部
     * @param current             当前页码
     * @return 分页数据 {@link Page}
     */
    Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current);

    /**
     * 获取总数据量
     *
     * @param dataSource          数据源
     * @param dataSourceStructure 表名称
     */
  default  Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure){
      throw new BusinessException("此数据源不支持获取总条数");
  };

    /**
     * datax同步文件生成逻辑 此逻辑只需要生成job.content.reader
     *
     * @return 读取的json对象
     */
    default Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        throw new BusinessException("此数据源无法生成同步逻辑!");
    }

    /**
     * datax同步文件生成逻辑 此逻辑只需要生成job.content.reader
     *
     * @return 读取的json对象
     */
    default Function<? extends CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> createSeaTunnelJsonFunction() {
        throw new BusinessException("此数据源无法生成同步逻辑!");
    }

    /**
     * datax同步文件生成逻辑 此逻辑只需要生成job.content.reader
     *
     * @return 读取的json对象
     */
    default Function<? extends CreateSeaTunnelJsonParameterPo, SeaTunnelSubmitJobDto> createSeaTunnelJsonCDCFunction() {
        throw new BusinessException("此数据源暂不支持实时同步!");
    }

    /**
     * 校验此数据源是否正常
     *
     * @param json 入参
     */
    void check(String json);

    /**
     * 数据类型获取
     *
     * @param value 值
     */
    default void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {

    }

}
