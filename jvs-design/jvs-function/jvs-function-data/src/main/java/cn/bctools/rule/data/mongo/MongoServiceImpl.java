package cn.bctools.rule.data.mongo;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.enums.OperationEnum;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zengzhi
 */
@Slf4j
@Service
@Order(20)
@Rule(
        value = "Mongo",
        group = RuleGroup.数据插件,
        test = true,
        enable = false,
        testShowEnum = TestShowEnum.JSON,
        returnType = ClassType.数组,
        order = 21,
//        iconUrl = "rule-mongodbyunshujukuMongoDB",
        explain = "是非关系数据库当中功能最丰富，最像关系数据库的。它支持的数据结构非常松散，是类似json的bson格式，因此可以存储比较复杂的数据类型。"

)
public class MongoServiceImpl implements BaseCustomFunctionInterface<MongoFunctionDto> {

    @Autowired
    private Gson gson;

    private ConcurrentHashMap<String, MongoTemplateCache> mongoTemplateCacheGlobal = new ConcurrentHashMap<>();
    /**
     * 当前线程使用的实例
     */
    private ThreadLocal<MongoTemplate> mongoTemplateLocal = new ThreadLocal<>();


    @Override
    public Object execute(MongoFunctionDto mongoFunctionDto, Map<String, Object> params) {
        try {
            if (!JSONUtil.isTypeJSONObject(mongoFunctionDto.getExecJson())) {
                throw new BusinessException("执行语句格式错误");
            }
            createMongodbTemplate(mongoFunctionDto);
            return operationMongodb(mongoFunctionDto);
        } finally {
            removeMongoTemplate();
        }
    }

    private void createMongodbTemplate(MongoFunctionDto dto) {
        MongodbAutoConfig autoConfig = new MongodbAutoConfig();
        MongoProperties mongoProperties = autoConfig.initMongoProperties(dto);
        String cacheKey = mongoProperties.getHost();
        MongoTemplateCache mongoTemplateCache = mongoTemplateCacheGlobal.get(cacheKey);
        String configMd5Str = MD5.create().digestHex(JSON.toJSONString(mongoProperties));
        if (mongoTemplateCache != null && mongoTemplateCache.getMongoConfigMd5().equals(configMd5Str)) {
            setMongoTemplate(mongoTemplateCache.getMongoTemplate());
            return;
        }
        MongoTemplate mongoTemplate = autoConfig.connectionMongo(mongoProperties);
        mongoTemplateCacheGlobal.put(cacheKey, new MongoTemplateCache(configMd5Str, mongoTemplate));
        setMongoTemplate(mongoTemplate);
    }

    private class MongodbAutoConfig {
        private MongoProperties initMongoProperties(MongoFunctionDto dto) {
            String dbName = dto.getDbName();
            String decodeDbName = PasswordUtil.decodedPassword(dbName, SpringContextUtil.getKey());
            MongodbDataSourceSelectedOption selectedOption = gson.fromJson(decodeDbName, MongodbDataSourceSelectedOption.class);
            MongoProperties mongoProperties = new MongoProperties();
            mongoProperties.setHost(selectedOption.getHost());
            mongoProperties.setPort(Optional.ofNullable(selectedOption.getPort()).orElse(27017));
            mongoProperties.setDatabase(Optional.ofNullable(selectedOption.getDatabaseName()).orElse("admin"));
            String pwdName = "";
            if (StrUtil.isNotBlank(selectedOption.getUserName())) {
                mongoProperties.setUsername(selectedOption.getUserName());
                pwdName = pwdName.concat(selectedOption.getUserName() + ":");
            }
            if (StrUtil.isNotBlank(selectedOption.getPassWord())) {
                mongoProperties.setPassword(selectedOption.getPassWord().toCharArray());
                pwdName = pwdName.concat(selectedOption.getPassWord());
            }
            //uri: mongodb://username:password@host:port/database?authSource=admin
            String uri;
            if (StrUtil.isNotBlank(pwdName)) {
                uri = "mongodb://" + pwdName + "@" + selectedOption.getHost() + ":" + selectedOption.getPort() + "/" + selectedOption.getDatabaseName() + "?authSource=admin&authMechanism=SCRAM-SHA-1";
            } else {
                uri = "mongodb://" + selectedOption.getHost() + ":" + selectedOption.getPort() + "/" + selectedOption.getDatabaseName() + "?authSource=admin&authMechanism=SCRAM-SHA-1";
            }
            mongoProperties.setUri(uri);
            return mongoProperties;
        }

        private MongoTemplate connectionMongo(MongoProperties mongoProperties) {
            SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoProperties.getUri());
            return new MongoTemplate(factory);
        }
    }

    private static class MongoTemplateCache {

        private String mongoConfigMd5;

        private MongoTemplate mongoTemplate;

        MongoTemplateCache(String mongoConfigMd5, MongoTemplate mongoTemplate) {
            this.mongoConfigMd5 = mongoConfigMd5;
            this.mongoTemplate = mongoTemplate;
        }

        String getMongoConfigMd5() {
            return mongoConfigMd5;
        }

        MongoTemplate getMongoTemplate() {
            return mongoTemplate;
        }
    }

    private void setMongoTemplate(MongoTemplate mongoTemplate) {
        mongoTemplateLocal.set(mongoTemplate);
    }

    private void removeMongoTemplate() {
        mongoTemplateLocal.remove();
    }

    /**
     * 操作mongodb数据
     *
     * @param mongoFunctionDto 入参
     * @return
     */
    private Object operationMongodb(MongoFunctionDto mongoFunctionDto) {
        OperationEnum operationType = mongoFunctionDto.getOperation();
        String dbName = mongoFunctionDto.getDbName();
        String decodeDbName = PasswordUtil.decodedPassword(dbName, SpringContextUtil.getKey());
        mongoFunctionDto.setDbName(decodeDbName);
        switch (operationType) {
            //根据操作类型来判断是执行什么操作
            case SAVE:
                //增
                return mongoTemplateSave(mongoFunctionDto);
            case DELETE:
                //删
                return mongoTemplateDelete(mongoFunctionDto);
            case QUERY:
                //查
                return mongoTemplateQuery(mongoFunctionDto);
            default:
                throw new BusinessException("不支持的操作类型");
        }
    }

    private Object mongoTemplateQuery(MongoFunctionDto mongoFunctionDto) {
        MongodbDataSourceSelectedOption dataSource = gson.fromJson(mongoFunctionDto.getDbName(), MongodbDataSourceSelectedOption.class);
        Map<String, Object> map = gson.fromJson(mongoFunctionDto.getExecJson(), Map.class);
        Query query = new Query();
        for (Map.Entry<String, Object> one : map.entrySet()) {
            query.addCriteria(Criteria.where(one.getKey()).is(one.getValue()));
        }
        return mongoTemplateLocal.get().find(query, Object.class, dataSource.getCollections());
    }

    private Object mongoTemplateDelete(MongoFunctionDto mongoFunctionDto) {
        Map<String, Object> map = gson.fromJson(mongoFunctionDto.getExecJson(), Map.class);
        MongodbDataSourceSelectedOption option = gson.fromJson(mongoFunctionDto.getDbName(), MongodbDataSourceSelectedOption.class);
        Query query = new Query();
        for (Map.Entry<String, Object> one : map.entrySet()) {
            query.addCriteria(Criteria.where(one.getKey()).is(one.getValue()));
        }
        return mongoTemplateLocal.get().remove(query, option.getCollections());
    }

    private Object mongoTemplateSave(MongoFunctionDto mongoFunctionDto) {
        MongodbDataSourceSelectedOption dataSource = gson.fromJson(mongoFunctionDto.getDbName(), MongodbDataSourceSelectedOption.class);
        mongoTemplateLocal.get().save(mongoFunctionDto.getExecJson(), dataSource.getCollections());
        return mongoFunctionDto;
    }
}
