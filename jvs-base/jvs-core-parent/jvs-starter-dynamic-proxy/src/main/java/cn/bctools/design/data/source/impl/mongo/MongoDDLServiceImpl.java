package cn.bctools.design.data.source.impl.mongo;

import cn.bctools.design.data.source.service.DynamicDDLService;
import cn.bctools.design.data.source.util.DataModelUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexOptions;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author: ZhuXiaoKang
 * @Description: MongoDB DDL
 */
@ConditionalOnProperty(name = "dynamic.data-source", havingValue = "mongodb")
@Service
@AllArgsConstructor
public class MongoDDLServiceImpl implements DynamicDDLService {
    private final MongoTemplate mongoTemplate;

    @Override
    public String tableNamePrefix() {
        return StringPool.EMPTY;
    }

    @Override
    public void createTable(String collectionName) {
        // 创建索引会自动创建数据集
        // 给数据集添加默认索引
        Index idIndex = new Index();
        idIndex.on("id", Sort.Direction.DESC);
        Index createTimeAndDataIdIndex = new Index();
        createTimeAndDataIdIndex.on("createTime", Sort.Direction.DESC).on("dataId", Sort.Direction.ASC);
        mongoTemplate.indexOps(collectionName).ensureIndex(idIndex);
        mongoTemplate.indexOps(collectionName).ensureIndex(createTimeAndDataIdIndex);
        // 给数据集日志表添加默认索引
        mongoTemplate.indexOps(DataModelUtil.buildLogCollectionName(collectionName)).ensureIndex(idIndex);
    }

    @Override
    public void renameTable(String oldCollectionName, String collectionName) {
        String dbName = mongoTemplate.getDb().getName();
        if (mongoTemplate.collectionExists(oldCollectionName)) {
            mongoTemplate.getCollection(oldCollectionName).renameCollection(new MongoNamespace(dbName, collectionName));
        }
    }

    @Override
    public void createIndex(String collectionName, String column) {
        // 创建索引 (后台创建)
        BasicDBObject index = new BasicDBObject(column, 1);
        IndexOptions options = new IndexOptions().background(true);
        mongoTemplate.getCollection(collectionName).createIndex(index, options);
    }

    @Override
    public void dropIndex(String collectionName, String column) {
        // 删除索引 (10秒超时)
        BasicDBObject index = new BasicDBObject(column, 1);
        DropIndexOptions options = new DropIndexOptions().maxTime(10, TimeUnit.SECONDS);
        mongoTemplate.getCollection(collectionName).dropIndex(index, options);
    }
}
