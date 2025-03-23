package cn.bctools.data.factory.source.data.mongodb;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.data.sync.plugin.po.CreateDataXJsonParameterPo;
import cn.bctools.data.factory.source.dto.MongoDbConnectDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author admin
 */
@Component(value = "mongoDataSource")
@Slf4j
public class MongoDataSourceExecuteImpl implements DataSourceExecuteInterface {

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, long size, long current) {
        MongoDbConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(MongoDbConnectDto.class);
        MongoClient client = MongoDbConnectSingleton.getClient(publicConnectDto, 10000, 20000);
        //获取数据库对象
        MongoDatabase database = client.getDatabase(publicConnectDto.getSourceLibraryName());
        //获取所有集合
        MongoCollection<Document> collection = database.getCollection(dataSourceStructure.getExecuteName());
        FindIterable<Document> iterable = collection.find();
        //是否需要查询前多少条
        if (size > BigDecimal.ROUND_UP) {
            long skip = size * (current - 1);
            iterable.skip((int) skip).limit((int) size);
        }
        MongoCursor<Document> mongoCursor = iterable.iterator();
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            while (mongoCursor.hasNext()) {
                Document next = mongoCursor.next();
                Map<String, Object> map = new HashMap<>(next.size());
                //数据过滤防止存在关键字
                next.keySet().forEach(e -> {
                    String key = e;
                    Object value = next.get(e);
                    if ("_id".equals(e)) {
                        value = String.valueOf(value);
                    }
                    map.put(key, value);
                });
                list.add(map);
            }
            return new Page<Map<String, Object>>()
                    .setTotal(this.getCount(dataSource, dataSourceStructure))
                    .setSize(size)
                    .setCurrent(current)
                    .setRecords(list);
        } catch (Exception e) {
            log.info("获取mongo数据错误", e);
            throw new BusinessException("mongo获取数据错误!");
        } finally {
            mongoCursor.close();
            client.close();
        }
    }

    @Override
    public Function<? extends CreateDataXJsonParameterPo, com.alibaba.fastjson2.JSONObject> createDataxFileJsonFunction() {
        return (dataSource) -> {
            //需要读取的列
            List<Map<String, String>> column = dataSource.getDataSourceStructure().getStructure().stream().map(e -> {
                HashMap<String, String> map = new HashMap<>();
                map.put("name", e.getOriginalColumnName());
                map.put("type", "");
                if (e.getDataType().equals("date")) {
                    map.put("type", "date");
                }
                return map;
            }).collect(Collectors.toList());
            JSONObject jsonObject = new JSONObject();
            //读取的数据源类型
            jsonObject.put("name", "mongodbreader");
            JSONObject parameter = new JSONObject();
            parameter.put("column", column);
            //连接信息
            MongoDbConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(MongoDbConnectDto.class);
            parameter.put("address", Collections.singletonList(publicConnectDto.getSourceHost() + ":" + publicConnectDto.getSourcePort()));
            parameter.put("dbName", publicConnectDto.getSourceLibraryName());
            //判断是否存在限制的条数 如果存在就生成sql执行
            if (dataSource.getSize() > 0L) {
                parameter.put("pageSize", dataSource.getSize());
            }
            parameter.put("collectionName", dataSource.getDataSourceStructure().getExecuteName());
            parameter.put("username", publicConnectDto.getSourceUserName());
            parameter.put("userPassword", publicConnectDto.getSourcePwd());
            if (StrUtil.isNotBlank(publicConnectDto.getAuthenticationDatabase())) {
                parameter.put("authDb", publicConnectDto.getAuthenticationDatabase());
            }
            if (dataSource.getIncrementalMode()) {
                Document document = new Document();
                String incrementalModeKey = dataSource.getIncrementalModeKey();
                Optional<DataSourceStructure.Structure> first = dataSource.getDataSourceStructure().getStructure().stream().filter(e -> e.getOriginalColumnName().equals(incrementalModeKey)).findFirst();
                if (!first.isPresent()) {
                    throw new BusinessException("没有获取到mongodb对应的key值");
                }
                String queryJson;
                Object incrementalModeKeyValue = dataSource.getIncrementalModeKeyValue();
                document.append(incrementalModeKey, new Document("$gt", incrementalModeKeyValue));
                queryJson = document.toJson();
                parameter.put("query", queryJson);
            }
            jsonObject.put("parameter", parameter);
            return jsonObject;
        };
    }

    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        MongoDbConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(MongoDbConnectDto.class);
        MongoClient client = MongoDbConnectSingleton.getClient(publicConnectDto, 10000, 20000);
        //获取数据库对象
        MongoDatabase database = client.getDatabase(publicConnectDto.getSourceLibraryName());
        MongoCollection<Document> collection = database.getCollection(dataSourceStructure.getExecuteName());
        long countDocuments = collection.countDocuments();
        //关闭客户端
        client.close();
        return countDocuments;
    }


    @Override
    public Boolean init(DataSource dataSource) {
        return DataSourceExecuteInterface.super.init(dataSource);
    }

    @Override
    public void check(String json) {
        DataSource dataSource = JSONObject.parseObject(json, DataSource.class);
        MongoDbConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(MongoDbConnectDto.class);
        MongoClient client = MongoDbConnectSingleton.getClient(publicConnectDto, 10000, 10000);
        //获取数据库对象
        MongoDatabase database = client.getDatabase(publicConnectDto.getSourceLibraryName());
        //获取所有集合
        ListCollectionsIterable<Document> documents = database.listCollections();
        try {
            documents.first();
        } catch (Exception exception) {
            log.error("连接失败!", exception);
            throw new BusinessException(exception.getMessage());
        } finally {
            client.close();
        }
    }

    /**
     * 验证用户提供的ip是否争取
     *
     * @param ipAddress ip地址
     */
    public static boolean isIpReachable(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            // 设置超时时间为 5000 毫秒
            return inetAddress.isReachable(5000);
        } catch (IOException e) {
            log.info("验证ip是否正确,发生了错误", e);
            return false;
        }
    }

    /**
     * 验证用户提供的端口号是否可以连接成功
     *
     * @param ipAddress ip地址
     * @param port      端口
     */
    public static boolean isPortOpen(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            // 设置超时时间为 5000 毫秒
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);
            return true;
        } catch (IOException e) {
            log.info("连接ip与端口错误", e);
            return false;
        }
    }


    @Override
    public void fieldTypeEnum(Object value, DataSourceStructure.Structure structure) {
        if (ObjectUtil.isNull(value)) {
            structure.setDataType(DataFieldTypeEnum.STRING.getCreateTable());
        } else if (value instanceof String) {
            structure.setDataType(DataFieldTypeEnum.STRING.getCreateTable());
        } else if (value instanceof List) {
            structure.setDataType("list");
        } else if (value instanceof Date) {
            //mongodb的日期是通过ISODate来表示的 格式统一为 yyyy-MM-dd HH:mm:ss.SSS
            structure.setFormat("yyyy-MM-dd HH:mm:ss")
                    .setDataType("date")
                    .setLength(0);
        } else if (value instanceof Boolean) {
            structure.setDataType("boolean");
        } else if (value instanceof Map) {
            structure.setDataType("map");
        } else if (value instanceof org.bson.types.Decimal128) {
            //高精度数字
            structure.setDataType("number")
                    .setLength(18)
                    .setPrecision(5);
        } else if (value instanceof Double) {
            //double
            structure.setDataType("double");
        } else if (value instanceof Long) {
            structure.setDataType("number")
                    .setLength(18)
                    .setPrecision(5);
        } else if (NumberUtil.isNumber(value.toString())) {
            //如果以上都不是就默认为高精度类型
            structure.setDataType("number")
                    .setLength(18)
                    .setPrecision(5);
        } else {
            structure.setDataType("STRING");
        }
    }

    @Override
    public List<DataSourceStructure> syncTableStructure(DataSource dataSource) {
        MongoDbConnectDto publicConnectDto = dataSource.getCustomJson().toJavaObject(MongoDbConnectDto.class);
        List<DataSourceStructure> dataSourceStructures = new ArrayList<>();
        MongoClient client = MongoDbConnectSingleton.getClient(publicConnectDto, 10000, 20000);
        //获取数据库对象
        MongoDatabase database = client.getDatabase(publicConnectDto.getSourceLibraryName());
        //获取所有集合
        ListCollectionsIterable<Document> documents = database.listCollections();
        //组装表结构数据
        documents.forEach(document -> {
            String tableName = document.get("name").toString();
            log.debug("同步mongodb结构{}", tableName);
            //查询数据 注意如果这里查不到数据就获取不到集合结构
            //获取表对象
            MongoCursor<Document> iterator = database.getCollection(tableName).find().iterator();
            List<DataSourceStructure.Structure> list = new ArrayList<>();
            try {
                if (iterator.hasNext()) {
                    Document next = iterator.next();
                    list = next.keySet().parallelStream()
                            .map(e -> {
                                String e1 = e;
                                DataSourceStructure.Structure structure = new DataSourceStructure
                                        .Structure()
                                        .setOriginalColumnName(e1)
                                        .setColumnCount(e1);
                                fieldTypeEnum(next.get(e), structure);
                                if ("_id".equals(e1)) {
                                    structure.setDataType("String");
                                }
                                return structure;
                            })
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.info("获取mongo数据错误!", e);
                throw new BusinessException(e.getMessage());
            } finally {
                iterator.close();
            }
            DataSourceStructure dataSourceStructure = new DataSourceStructure()
                    .setName(tableName)
                    .setExecuteName(tableName)
                    .setCheckIs(Boolean.TRUE)
                    .setTableNameDesc(tableName)
                    .setStructure(list)
                    .setFieldCount(list.size());
            dataSourceStructures.add(dataSourceStructure);
        });
        client.close();
        return dataSourceStructures;
    }

}
