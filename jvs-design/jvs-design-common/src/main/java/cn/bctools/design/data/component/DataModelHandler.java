package cn.bctools.design.data.component;

import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.aspect.ModelCollectionName;
import cn.bctools.design.data.entity.DynamicDataPo;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.util.ModeUtils;
import com.mongodb.MongoNamespace;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 模型数据操作
 */
@Component
public class DataModelHandler {
    @Resource(name = "devMongoTemplate")
    private MongoTemplate devMongoTemplate;
    @Resource(name = "betaMongoTemplate")
    private MongoTemplate betaMongoTemplate;
    @Resource(name = "gaMongoTemplate")
    private MongoTemplate gaMongoTemplate;

    /**
     * 获取模式对应的mongodbTemplate
     *
     * @return mongodbTemplate
     */
    private MongoTemplate getMongoTemplate() {
        // 得到当前模式，若当前模式为空，默认为正式模式
        AppVersionTypeEnum currentMode = Optional.ofNullable(ModeUtils.getMode()).orElse(AppVersionTypeEnum.GA);
        switch (currentMode) {
            case DEV:
                return devMongoTemplate;
            case BETA:
                return betaMongoTemplate;
            case GA:
            default:
                // 不指定模式，默认正式模式
                return gaMongoTemplate;
        }
    }

    /**
     * 创建默认索引
     *
     * @param collectionName 数据集名称
     */
    public void createDefaultIndex(String collectionName) {
        // 给数据集添加默认索引
        Index idIndex = new Index();
        idIndex.on("id", Sort.Direction.DESC);
        Index createTimeAndDataIdIndex = new Index();
        createTimeAndDataIdIndex.on(Get.name(DynamicDataPo::getCreateTime), Sort.Direction.DESC).on("dataId", Sort.Direction.ASC);
        getMongoTemplate().indexOps(collectionName).ensureIndex(idIndex);
        getMongoTemplate().indexOps(collectionName).ensureIndex(createTimeAndDataIdIndex);
        // 给数据集日志表添加默认索引
        getMongoTemplate().indexOps(DataModelUtil.buildLogCollectionName(collectionName)).ensureIndex(idIndex);
    }

    /**
     * 模型id转数据集名
     *
     * @param modelId 模型id
     * @return 数据集名
     */
    @ModelCollectionName(modelId = "modelId")
    public String convertCollectionName(String modelId) {
        return modelId;
    }

    @ModelCollectionName(modelId = "collectionName")
    public MongoCollection<Document> getCollection(String collectionName) {
        return getMongoTemplate().getCollection(collectionName);
    }

    public boolean collectionExists(String collectionName) {
        return getMongoTemplate().collectionExists(collectionName);
    }

    public void renameCollection(String oldCollectionName, String collectionName) {
        String dbName = getMongoTemplate().getDb().getName();
        if (getMongoTemplate().collectionExists(oldCollectionName)) {
            getMongoTemplate().getCollection(oldCollectionName).renameCollection(new MongoNamespace(dbName, collectionName));
        }
    }

    public Document executeCommand(Document command) {
        return getMongoTemplate().executeCommand(command);
    }

    @ModelCollectionName(modelId = "collectionName")
    public long count(Query query, String collectionName) {
        return getMongoTemplate().count(query, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public long count(Query query, @Nullable Class<?> entityClass, String collectionName) {
        return getMongoTemplate().count(query, entityClass, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public long estimatedCount(String collectionName) {
        return getMongoTemplate().estimatedCount(collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        return getMongoTemplate().find(query, entityClass, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
        return getMongoTemplate().findOne(query, entityClass, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> List<T> findAllAndRemove(Query query, String collectionName) {
        return getMongoTemplate().findAllAndRemove(query, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> T findAndRemove(Query query, Class<T> entityClass, String collectionName) {
        return getMongoTemplate().findAndRemove(query, entityClass, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> T save(T objectToSave, String collectionName) {
        return getMongoTemplate().save(objectToSave, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public UpdateResult updateMulti(Query query, UpdateDefinition update, String collectionName) {
        return getMongoTemplate().updateMulti(query, update, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public UpdateResult updateFirst(Query query, UpdateDefinition update, String collectionName) {
        return getMongoTemplate().updateFirst(query, update, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public DeleteResult remove(Query query, String collectionName) {
        return getMongoTemplate().remove(query, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public DeleteResult removeAll(String collectionName) {
        return getMongoTemplate().remove(new Query(), collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> T insert(T objectToSave, String collectionName) {
        return getMongoTemplate().insert(objectToSave, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public <T> Collection<T> insertBatch(Collection<? extends T> batchToSave, String collectionName) {
        return getMongoTemplate().insert(batchToSave, collectionName);
    }

    @ModelCollectionName(modelId = "collectionName")
    public BulkWriteResult updateBatch(List<Pair<Query, Update>> updates, String collectionName) {
        return getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, collectionName).updateOne(updates).execute();
    }

    @ModelCollectionName(modelId = "collectionName")
    public void dropCollection(String collectionName) {
        if (collectionExists(collectionName)) {
            getMongoTemplate().dropCollection(collectionName);
        }
    }

    @ModelCollectionName(modelId = "collectionName")
    public List<Map> aggregate(Aggregation aggregation, String collectionName) {
        return getMongoTemplate().aggregate(aggregation, collectionName, Map.class).getMappedResults();
    }
}
