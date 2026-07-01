package cn.bctools.rule.data.mongo.mongodb;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.data.mongo.MongoDBOption;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author admin
 */
@Component
@Slf4j
public class MongoCustomTemplate {

    /**
     * 插入
     * @param option
     * @param tableName
     * @param parameters
     */
    public void insert(MongoDBOption option,String tableName,Map<String,Object> parameters){
        this.exec(option,tableName,collection -> {
            if(MapUtil.isNotEmpty(parameters)){
                Document document = new Document();
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    document.append(entry.getKey(),entry.getValue());
                }
                InsertOneResult insertOneResult = collection.insertOne(document);
                return insertOneResult.getInsertedId();
            }
            return null;
        });
    }

    /**
     * 修改
     * @param option
     * @param tableName
     * @param queryArgs
     * @param updateArgs
     */
    public void update(MongoDBOption option, String tableName, Map<String,Object> queryArgs, Map<String,Object> updateArgs){
        this.exec(option,tableName,collection -> {
            Query query = new Query();
            if(CollectionUtil.isNotEmpty(queryArgs)){
                List<Criteria> collect = queryArgs.entrySet().stream().map(e -> Criteria.where(e.getKey()).is(e.getValue())).collect(Collectors.toList());
                Criteria criteria = new Criteria();
                criteria.andOperator(collect);
                query.addCriteria(criteria);
            }
            if(MapUtil.isNotEmpty(updateArgs)){
                Document document = new Document(updateArgs);
                Document set = new Document("$set", document);

                UpdateResult updateResult = collection.updateMany(query.getQueryObject(), set);
                long modifiedCount = updateResult.getModifiedCount();
                log.info("------本次修改：{} 行",modifiedCount);
                return modifiedCount;
            }
            return 0;
        });
    }

    /**
     *
     * @param option
     * @param tableName
     * @param parameters
     * @return
     */
    public List<Map<String, Object>> list(MongoDBOption option,String tableName,Map<String,Object> parameters) {
        List<Map<String, Object>> list = new ArrayList<>();
        this.exec(option, tableName, collection -> {
            Query query = new Query();
            if (CollectionUtil.isNotEmpty(parameters)) {
                List<Criteria> collect = parameters.entrySet().stream().map(e -> Criteria.where(e.getKey()).is(e.getValue())).collect(Collectors.toList());
                Criteria criteria = new Criteria();
                criteria.andOperator(collect);
                query.addCriteria(criteria);
            }

            FindIterable<Document> iterable = collection.find(query.getQueryObject());

            try (MongoCursor<Document> mongoCursor = iterable.iterator()) {
                while (mongoCursor.hasNext()) {
                    Document next = mongoCursor.next();
                    Map<String, Object> map = new HashMap<>(next.size());
                    //数据过滤防止存在关键字
                    next.keySet().forEach(e -> {
                        Object value = next.get(e);
                        map.put(e, value);
                    });
                    list.add(map);
                }
                return list;
            } catch (Exception e) {
                log.info("获取mongo数据错误", e);
                throw new BusinessException("mongo获取数据错误!");
            }
        });
        return list;
    }

    /**
     * 删除
     * @param option
     * @param tableName
     * @param args
     */
    public void del(MongoDBOption option,String tableName,Map<String,Object> args){
        this.exec(option,tableName,collection ->{
            Query query = new Query();
            if (CollectionUtil.isNotEmpty(args)) {
                List<Criteria> collect = args.entrySet().stream().map(e -> Criteria.where(e.getKey()).is(e.getValue())).collect(Collectors.toList());
                Criteria criteria = new Criteria();
                criteria.andOperator(collect);
                query.addCriteria(criteria);
            }
            DeleteResult deleteResult = collection.deleteMany(query.getQueryObject());
            return deleteResult.getDeletedCount();
        });
    }

    /**
     * 获取mongo客户端
     * @param option
     * @return
     */
    public MongoClient getMongoClient(MongoDBOption option){
        return MongoDbConnectSingleton.getClient(option, 10000, 20000);
    }

    /**
     * 获取集合
     * @param option
     * @param tableName
     * @return
     */
    private void exec(MongoDBOption option, String tableName, Function<MongoCollection<Document>,Object> function){
        if(option==null){
            throw new BusinessException("未选择数据库");
        }
        if(StrUtil.isBlank(tableName)){
            throw new BusinessException("未填写表名称");
        }
        MongoClient client = MongoDbConnectSingleton.getClient(option, 10000, 20000);
        try {
            //获取数据库对象
            MongoDatabase database = client.getDatabase(option.getLibraryName());
            //获取所有集合
            MongoCollection<Document> collection = database.getCollection(tableName);
            function.apply(collection);
        } finally {
            client.close();
        }
    }
}
