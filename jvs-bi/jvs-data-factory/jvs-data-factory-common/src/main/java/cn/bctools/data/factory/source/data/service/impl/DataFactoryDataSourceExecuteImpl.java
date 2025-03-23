package cn.bctools.data.factory.source.data.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author admin
 */
@Component(value = "dataFactoryDataSource")
@Slf4j
public class DataFactoryDataSourceExecuteImpl implements DataSourceExecuteInterface {
    @Autowired
    JvsDataFactoryOutService jvsDataFactoryOutService;
    @Autowired
    JvsDataFactoryService jvsDataFactoryService;

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        throw new BusinessException("不支持手动同步");
    }

    @Override
    public List<DataSourceStructure.Structure> getTableStructure(DataSourceStructure dataSourceStructure) {
        //获取所有列
        List<DataSourceStructure.Structure> tableStructure = DataSourceExecuteInterface.super.getTableStructure(dataSourceStructure);
        if (tableStructure.isEmpty()) {
            return tableStructure;
        }
        //需要验证列权限
        List<DataSourceField> row = jvsDataFactoryService.getColumn(dataSourceStructure.getExecuteName());
        if (row.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = row.stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        tableStructure = tableStructure.stream().filter(e -> list.contains(e.getColumnName())).collect(Collectors.toList());
        return tableStructure;
    }


    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {

        return null;
    }

    @Override
    public void check(String json) {
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        return jvsDataFactoryOutService.getData(dataSourceStructure.getExecuteName(), size, current);
    }


}
