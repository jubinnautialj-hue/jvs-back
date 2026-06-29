package cn.bctools.design.data.source.impl.mongo;

import cn.bctools.design.data.source.service.DynamicDMLService;
import cn.bctools.design.data.source.impl.sql.DynamicQuery;
import cn.bctools.design.data.source.impl.sql.DynamicUpdate;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: MongoDB DML
 */
@ConditionalOnProperty(name = "dynamic.data-source", havingValue = "mongodb")
@Service
@AllArgsConstructor
public class MongoDMLServiceImpl implements DynamicDMLService {
    private final MongoTemplate mongoTemplate;

    @Override
    public <T> T save(T data, String collectionName) {
        return mongoTemplate.save(data, collectionName);
    }

    @Override
    public <T> T insert(T data, String collectionName) {
        return mongoTemplate.insert(data, collectionName);
    }

    @Override
    public <T> Collection<T> insertBatch(Collection<? extends T> data, String collectionName) {
        return mongoTemplate.insert(data, collectionName);
    }

    @Override
    public Long remove(DynamicQuery dynamicQuery, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.remove(query, collectionName).getDeletedCount();
    }

    @Override
    public <T> T findAndRemove(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.findAndRemove(query, entityClass, collectionName);
    }

    @Override
    public <T> List<T> findAllAndRemove(DynamicQuery dynamicQuery, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.findAllAndRemove(query, collectionName);
    }

    @Override
    public Long updateMulti(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        Update update = new Update();
        for (Map.Entry<String, Object> entry : data.getUpdateData().entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        return mongoTemplate.updateMulti(query, update, collectionName).getMatchedCount();
    }

    @Override
    public Long updateFirst(DynamicQuery dynamicQuery, DynamicUpdate data, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        Update update = new Update();
        for (Map.Entry<String, Object> entry : data.getUpdateData().entrySet()) {
            update.set(entry.getKey(), entry.getValue());
        }
        return mongoTemplate.updateFirst(query, update, collectionName).getMatchedCount();
    }


    @Override
    public <T> T findOne(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.findOne(query, entityClass, collectionName);
    }

    @Override
    public <T> List<T> findList(DynamicQuery dynamicQuery, Class<T> entityClass, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.find(query, entityClass, collectionName);
    }

    @Override
    public Long count(DynamicQuery dynamicQuery, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.count(query, collectionName);
    }

    @Override
    public Long count(DynamicQuery dynamicQuery, Class<?> entityClass, String collectionName) {
        Query query = MongoCriteriaUtil.convert(dynamicQuery);
        return mongoTemplate.count(query, entityClass, collectionName);
    }

    @Override
    public Long estimatedCount(String collectionName) {
        return mongoTemplate.estimatedCount(collectionName);
    }

    @Override
    public Long tableDataSize(String collectionName) {
        return mongoTemplate.executeCommand(new Document("collStats", collectionName)).getLong("size");
    }
}
