package cn.bctools.design.data.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.component.DataModelHandler;
import cn.bctools.design.data.entity.DataLogPo;
import cn.bctools.design.data.fields.enums.DataEventType;
import cn.bctools.design.data.service.DataLogService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据变更记录
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
public class DataLogServiceImpl implements DataLogService {

    /**
     * 数据版本号缓存
     */
    private static final String DATA_VERSION_KEY = "data:version:";
    /**
     * 数据版本号缓存锁
     */
    private static final String DATA_VERSION_LOCK = "data:version:lock:";

    DataModelHandler dataModelHandler;
    RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveLog(String dataModelId, String dataId, Map<String, Object> data, DataEventType eventType) {
        return saveLog(dataModelId, dataId, data, eventType, UserCurrentUtils.getNullableUser());
    }

    @Override
    public String saveLog(String dataModelId, String dataId, Map<String, Object> data, DataEventType eventType, UserDto userDto) {
        return saveLog(dataModelId, dataId, data, Collections.emptyList(), eventType,UserCurrentUtils.getNullableUser());
    }

    @Override
    public String saveLog(String dataModelId, String dataId, Map<String, Object> data, List dataChange, DataEventType eventType) {
        return saveLog(dataModelId, dataId, data, dataChange, eventType,UserCurrentUtils.getNullableUser());
    }

    @Override
    public String saveLog(String dataModelId, String dataId, Map<String, Object> data, List dataChange, DataEventType eventType, UserDto userDto) {
        String collectionName = DataModelUtil.buildLogCollectionName(dataModelId);

        String version = Optional.ofNullable(DataModelUtil.getCurrentDataVersion()).orElseGet(IdGenerator::getIdStr);
        DataLogPo dataLogPo = new DataLogPo();
        dataLogPo.setDataId(dataId);
        dataLogPo.setVersion(version);
        dataLogPo.setContent(data);
        dataLogPo.setDataChange(dataChange);
        dataLogPo.setUpdateTime(LocalDateTime.now());
        if (ObjectNull.isNotNull(userDto)) {
            dataLogPo.setUpdateById(userDto.getId());
        }
        dataLogPo.setOperator(eventType.name());
        dataLogPo.setRemark(eventType.getDesc());
        dataModelHandler.insert(dataLogPo, collectionName);
        return version;
    }

    @Override
    public String follow(String dataModelId, String dataId, Map<String, Object> data, UserDto userDto) {
        String collectionName = DataModelUtil.buildFollowCollectionName(dataModelId);
        String version = Optional.ofNullable(DataModelUtil.getCurrentDataVersion()).orElseGet(IdGenerator::getIdStr);
        DataLogPo dataLogPo = new DataLogPo();
        dataLogPo.setDataId(dataId);
        dataLogPo.setVersion(version);
        dataLogPo.setContent(data);
        dataLogPo.setUpdateTime(LocalDateTime.now());
        if (ObjectNull.isNotNull(userDto)) {
            dataLogPo.setUpdateById(userDto.getId());
            dataLogPo.setRealName(userDto.getRealName());
            dataLogPo.setHeadImg(userDto.getHeadImg());
        }
        dataModelHandler.insert(dataLogPo, collectionName);
        return version;
    }

    @Override
    public List<DataLogPo> getLog(String dataModelId, String dataId) {
        return dataModelHandler.find(new Query().addCriteria(new Criteria().and("dataId").is(dataId)), DataLogPo.class, DataModelUtil.buildLogCollectionName(dataModelId));
    }
    @Override
    public List<DataLogPo> follow(String dataModelId, String dataId) {
        return dataModelHandler.find(new Query().addCriteria(new Criteria().and("dataId").is(dataId)), DataLogPo.class, DataModelUtil.buildFollowCollectionName(dataModelId));
    }

    @Override
    public DataLogPo getLog(String dataModelId, String dataId, String version) {
        return dataModelHandler.findOne(new Query().addCriteria(new Criteria().and("dataId").is(dataId).and("version").is(version)).limit(1), DataLogPo.class, DataModelUtil.buildLogCollectionName(dataModelId));
    }
}
