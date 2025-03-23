package cn.bctools.data.factory.source.data.service.impl;

import cn.bctools.common.utils.IdGenerator;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.DataModelDataSourceDto;
import cn.bctools.data.factory.source.dto.MongoDbConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.design.use.api.AppApi;
import cn.bctools.design.use.api.DataModelApi;
import cn.bctools.design.use.api.dto.DataFiledDto;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Component(value = "dataModel")
@Slf4j
public class JvsDataModelDataSourceExecuteImpl implements DataSourceExecuteInterface {

    @Autowired
    DataModelApi dataModelApi;
    @Autowired
    AppApi appApi;

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        DataModelDataSourceDto dataModelDataSourceDto = dataSource.getCustomJson().toJavaObject(DataModelDataSourceDto.class);
        log.info("入参:{}", JSONObject.toJSONString(dataSource.getCustomJson()));
        //根据应用ID查询模型集
        List<DataModelDto> dataModelDtos = dataModelApi.dataModelList(dataModelDataSourceDto.getSourceLibraryName(), dataModelDataSourceDto.getMode()).getData();
        log.info("查询的模型{}", JSONObject.toJSONString(dataModelDtos));
        if (dataModelDtos.isEmpty()) {
            return new ArrayList<>();
        }
        List<DataSourceStructure> tableNames = dataModelDtos
                .stream()
                .map(e -> {
                    e.setAppCode(dataModelDataSourceDto.getSourceLibraryName());
                    return new DataSourceStructure()
                            .setId(IdGenerator.getIdStr())
                            .setCustomJson(JSONObject.toJSONString(e))
                            .setExecuteName(e.getTableCode())
                            .setDataSourceId(dataSource.getId())
                            .setName(e.getTableNameDesc())
                            .setTableNameDesc(e.getTableNameDesc());
                })
                .collect(Collectors.toList());
        //获取表结构
        tableNames.forEach(e -> {
            DataModelDto dataModelDto = JSONObject.parseObject(e.getCustomJson(), DataModelDto.class);
            log.info("查询字段传入的值:{},{},{}", dataModelDataSourceDto.getSourceLibraryName(), dataModelDto.getTableCode(), dataModelDataSourceDto.getMode());
            List<DataFiledDto> map = dataModelApi.dataFieldMap(dataModelDto.getTableCode(), dataModelDto.getAppCode(), dataModelDataSourceDto.getMode()).getData();
            log.info("查询的字段详细为:{}", JSONObject.toJSONString(map));
            List<DataSourceStructure.Structure> collect = map
                    .stream()
                    .map(s -> new DataSourceStructure.Structure()
                            .setOriginalColumnName(s.getColumnName())
                            .setColumnName(IdGenerator.getIdStr())
                            .setColumnCount(s.getColumnCount())
                            .setFormat(s.getFormat())
                            .setLength(s.getLength())
                            .setDorisType(s.getDataType().getCreateTable())
                            .setPrecision(s.getPrecision())
                            .setDataFieldTypeEnum(DataFieldTypeEnum.valueOf(s.getDataType().name()))
                            .setDataFieldTypeClassify(DataFieldTypeClassifyEnum.valueOf(s.getDataType().getClassifyEnum().name()))
                            .setDataType(s.getDataType().getValue())
                    ).collect(Collectors.toList());
            e.setStructure(collect)
                    .setCheckIs(Boolean.TRUE)
                    .setFieldCount(collect.size());
        });
        return tableNames;
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            DataModelDataSourceDto dataModelDataSourceDto = dataSource.getCustomJson().toJavaObject(DataModelDataSourceDto.class);
            DataSourceModel parseObject = JSONObject.parseObject(dataModelDataSourceDto.getDatasource(), DataSourceModel.class);
            //需要读取的列
            List<Map<String, String>> column = dataSource.getDataSourceStructure().getStructure().stream().map(e -> {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", e.getOriginalColumnName());
                map.put("type", "");
                return map;
            }).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "mongodbreader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            MongoDbConnectDto publicConnectDto = new MongoDbConnectDto().setAuthenticationDatabase(parseObject.getAuthenticationDatabase());
            publicConnectDto.setSourceHost(parseObject.getHost())
                    .setSourcePort(Integer.valueOf(parseObject.getPort()))
                    .setSourcePwd(parseObject.getPassword())
                    .setSourceUserName(parseObject.getUsername())
                    .setSourceLibraryName(parseObject.getMongoClientDatabase());
            parameter.put("address", Collections.singletonList(publicConnectDto.getSourceHost() + ":" + publicConnectDto.getSourcePort()));
            parameter.put("dbName", publicConnectDto.getSourceLibraryName());
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                parameter.put("pageSize", dataSource.getSize());
            }
            DataModelDto dataModelDto = JSONObject.parseObject(dataSource.getDataSourceStructure().getCustomJson(), DataModelDto.class);
            parameter.put("collectionName", dataModelDto.getTableName());
            parameter.put("username", publicConnectDto.getSourceUserName());
            parameter.put("userPassword", publicConnectDto.getSourcePwd());
            if (StrUtil.isNotBlank(publicConnectDto.getAuthenticationDatabase())) {
                parameter.put("authDb", publicConnectDto.getAuthenticationDatabase());
            }
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    /**
     * 获取数据
     *
     * @param dataSource          数据源
     * @param dataSourceStructure 表名称
     * @param size                获取条数 如果为0就是获取全部
     * @param current             当前页码
     */
    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        DataModelDataSourceDto dataModelDataSourceDto = dataSource.getCustomJson().toJavaObject(DataModelDataSourceDto.class);
        List mapList = dataModelApi.list(dataSourceStructure.getExecuteName(), size, current, dataModelDataSourceDto.getMode()).getData();
        Long total = dataModelApi.getCount(dataSourceStructure.getExecuteName(), dataModelDataSourceDto.getMode()).getData();
        return new Page<Map<String, Object>>()
                .setTotal(total)
                .setCurrent(current)
                .setSize(size)
                .setRecords(mapList);
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        DataModelDataSourceDto dataModelDataSourceDto = dataSource.getCustomJson().toJavaObject(DataModelDataSourceDto.class);
        return dataModelApi.getCount(dataSourceStructure.getId(), dataModelDataSourceDto.getMode()).getData();
    }

    @Override
    public void check(String json) {
    }

    @Data
    private static class DataSourceModel {
        private String authenticationDatabase;
        private String database;
        private String host;
        private String mongoClientDatabase;
        private String password;
        private String port;
        private String username;
    }

}
