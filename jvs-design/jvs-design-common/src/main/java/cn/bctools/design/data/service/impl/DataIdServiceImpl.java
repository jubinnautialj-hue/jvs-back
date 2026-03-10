package cn.bctools.design.data.service.impl;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataIdPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.data.mapper.DataIdMapper;
import cn.bctools.design.data.service.DataIdService;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据自增序号
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
public class DataIdServiceImpl extends ServiceImpl<DataIdMapper, DataIdPo> implements DataIdService {

    RedisUtils redisUtils;

    /**
     * 存储活跃的（最近用到的）模型id
     * 用来定时同步模型的数据序号
     */
    private static final Map<String, LocalDateTime> ACTIVE_MODEL = new HashMap<>(8);
    /**
     * 存储活跃的（最近用到的）流程设计id
     * 用来定时同步流程的数据序号
     */
    private static final Map<String, LocalDateTime> ACTIVE_FLOW = new HashMap<>(8);

    private static final String MODE_NEXT_ID_CACHE = "model:nextId:";
    private static final String MODE_YEAR_NEXT_ID_CACHE = MODE_NEXT_ID_CACHE + "year:";
    private static final String MODE_MONTH_NEXT_ID_CACHE = MODE_NEXT_ID_CACHE + "month:";
    private static final String MODE_DAY_NEXT_ID_CACHE = MODE_NEXT_ID_CACHE + "day:";
    private static final String MODE_HOUR_NEXT_ID_CACHE = MODE_NEXT_ID_CACHE + "hour:";
    private static final String MODE_NEXT_ID_LAST_TIME_CACHE = MODE_NEXT_ID_CACHE + "lt:";

    /**
     * 重置id锁后缀
     */
    private static final String RESET_ID_LOCK_SUFFIX = ":lock";

    public DataIdServiceImpl(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
        // 定时同步数据序号
        timingSaveDataId();
    }

    @Override
    public DataIdPo nextId(DesignType designType, String designId, final int size) {
        if (StringUtils.isBlank(designId)) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        String currentIdKey = MODE_NEXT_ID_CACHE + designId;
        String yearIdKey = MODE_YEAR_NEXT_ID_CACHE + designId;
        String monthIdKey = MODE_MONTH_NEXT_ID_CACHE + designId;
        String dayIdKey = MODE_DAY_NEXT_ID_CACHE + designId;
        String hourIdKey = MODE_HOUR_NEXT_ID_CACHE + designId;
        String lastTimeKey = MODE_NEXT_ID_LAST_TIME_CACHE + designId;
        int currentId = 0;
        int yearId = 0;
        int monthId = 0;
        int dayId = 0;
        int hourId = 0;
        DataIdPo dataIdPo = null;
        LocalDateTime last = getLastCacheTime(lastTimeKey);
        // 缓存不存在时先初始缓存
        if (last == null) {
            String lockKey = MODE_NEXT_ID_CACHE + designId + RESET_ID_LOCK_SUFFIX;
            boolean lock = redisUtils.tryLock(lockKey, 10 * 1000);
            try {
                while (Boolean.FALSE.equals(lock)) {
                    Thread.sleep(10);
                    lock = redisUtils.tryLock(lockKey, 10 * 1000);
                }
                last = getLastCacheTime(lastTimeKey);
                if (last != null) {
                    return nextIdByCache(designType, designId, now, last, size);
                }
                LambdaQueryWrapper<DataIdPo> wrapper = Wrappers.<DataIdPo>lambdaQuery();
                if(DesignType.data.equals(designType)) {
                    wrapper.eq(DataIdPo::getModelId, designId);
                }
                if(DesignType.workflow.equals(designType)) {
                    wrapper.eq(DataIdPo::getFlowDesignId, designId);
                }

                dataIdPo = this.getOne(wrapper);
                if (Objects.isNull(dataIdPo)) {
                    // 新增序号数据
                    dataIdPo = new DataIdPo();
                    if(DesignType.data.equals(designType)) {
                        dataIdPo.setModelId(designId);
                    }
                    if(DesignType.workflow.equals(designType)) {
                        dataIdPo.setFlowDesignId(designId);
                    }
                    dataIdPo.setUpdateTime(now);
                    this.handleId(dataIdPo, id -> size);
                    currentId = dataIdPo.getCurrentId();
                    yearId = dataIdPo.getCurrentYearId();
                    monthId = dataIdPo.getCurrentMonthId();
                    dayId = dataIdPo.getCurrentDayId();
                    hourId = dataIdPo.getCurrentHourId();
                    this.save(dataIdPo);
                    // 初始序号为1
                    this.handleId(dataIdPo, id -> 1);
                } else {
                    currentId = dataIdPo.getCurrentId() + size;
                    yearId = dataIdPo.getCurrentYearId() + size;
                    monthId = dataIdPo.getCurrentMonthId() + size;
                    dayId = dataIdPo.getCurrentDayId() + size;
                    hourId = dataIdPo.getCurrentHourId() + size;
                    dataIdPo
                            .setCurrentId(currentId)
                            .setCurrentYearId(yearId)
                            .setCurrentMonthId(monthId)
                            .setCurrentDayId(dayId)
                            .setCurrentHourId(hourId);
                    // 计算返回时的序号
                    this.handleId(dataIdPo, id -> id + 1 - size);
                }
                // 保存缓存
                redisUtils.set(currentIdKey, currentId);
                redisUtils.set(yearIdKey, yearId);
                redisUtils.set(monthIdKey, monthId);
                redisUtils.set(dayIdKey, dayId);
                redisUtils.set(hourIdKey, hourId);
                redisUtils.set(lastTimeKey, LocalDateTimeUtil.formatNormal(now));
                return dataIdPo;
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            } finally {
                redisUtils.unLock(lockKey);
            }
        }

        return nextIdByCache(designType, designId, now, last, size);
    }

    /**
     * 从缓存中得到自增序号
     *
     * @param designType 设计套件类型
     * @param designId 设计id
     * @param now 当前时间
     * @param last 最后一次缓存时间
     * @param size 序号数量
     * @return 自增序号
     */
    private DataIdPo nextIdByCache(DesignType designType, String designId, LocalDateTime now, LocalDateTime last, int size) {
        String currentIdKey = MODE_NEXT_ID_CACHE + designId;
        String yearIdKey = MODE_YEAR_NEXT_ID_CACHE + designId;
        String monthIdKey = MODE_MONTH_NEXT_ID_CACHE + designId;
        String dayIdKey = MODE_DAY_NEXT_ID_CACHE + designId;
        String hourIdKey = MODE_HOUR_NEXT_ID_CACHE + designId;
        String lastTimeKey = MODE_NEXT_ID_LAST_TIME_CACHE + designId;
        // 处理序号的递增与重置
        DataIdPo dataIdPo = new DataIdPo()
                .setUpdateTime(now)
                .setCurrentId(getId(currentIdKey, 0, 0, size))
                .setCurrentYearId(getId(yearIdKey, now.getYear(), last.getYear(), size))
                .setCurrentMonthId(getId(monthIdKey, now.getMonthValue(), last.getMonthValue(), size))
                .setCurrentDayId(getId(dayIdKey, now.getDayOfYear(), last.getDayOfYear(), size))
                .setCurrentHourId(getId(hourIdKey, now.getHour(), last.getHour(), size));
        if(DesignType.data.equals(designType)) {
            dataIdPo.setModelId(designId);
        }
        if(DesignType.workflow.equals(designType)) {
            dataIdPo.setFlowDesignId(designId);
        }
        redisUtils.set(lastTimeKey, LocalDateTimeUtil.formatNormal(now));
        // 将当前模型设置为活跃的模型，用以定时同步缓存到数据库
        setActiveModel(designType, designId);
        // 计算返回时的序号
        this.handleId(dataIdPo, id -> id + 1 - size);
        return dataIdPo;
    }

    private LocalDateTime getLastCacheTime(String lastTimeKey) {
        return Optional.ofNullable(redisUtils.get(lastTimeKey)).map(t -> LocalDateTimeUtil.parse(String.valueOf(t), DatePattern.NORM_DATETIME_PATTERN)).orElse(null);
    }

    /**
     * 获取序号
     *
     * @param key  序号缓存key
     * @param v1   重置条件1
     * @param v2   重置条件2
     * @param size 增加的数量
     * @return 序号
     */
    private int getId(String key, int v1, int v2, int size) {
        if (v1 == v2) {
            return incrId(key, size);
        } else {
            return resetIdCache(key, size);
        }
    }

    /**
     * 自增序号
     *
     * @param key  序号缓存key
     * @param size 增加的数量
     * @return 序号
     */
    private Integer incrId(String key, int size) {
        return redisUtils.incr(key, (long) size).intValue();
    }

    /**
     * 重置序号
     *
     * @param key  待重置序号的缓存key
     * @param size 重置数量
     * @return 序号
     */
    private Integer resetIdCache(String key, int size) {
        String lockKey = key + RESET_ID_LOCK_SUFFIX;
        boolean lock = redisUtils.tryLock(lockKey, 10 * 1000);
        int resSize = size;
        try {
            if (lock) {
                redisUtils.set(key, size);
            } else {
                // 没拿到锁，等待一段时间，获取自增值
                Thread.sleep(100);
                resSize = incrId(key, size);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }
        return resSize;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataIdPo resetId(DesignType designType, String designId, int newId) {
        if (StringUtils.isBlank(designId)) {
            return null;
        }
        final int size = Math.max(newId, 1);
        LambdaQueryWrapper<DataIdPo> wrapper = Wrappers.<DataIdPo>lambdaQuery();
        if(DesignType.data.equals(designType)) {
            wrapper.eq(DataIdPo::getModelId, designId);
        }
        if(DesignType.workflow.equals(designType)) {
            wrapper.eq(DataIdPo::getFlowDesignId, designId);
        }
        DataIdPo dataIdPo = this.getOne(wrapper.last(SysConstant.FOR_UPDATE));
        if (Objects.nonNull(dataIdPo)) {
            this.removeById(designId);
        }
        // 新增序号数据
        dataIdPo = new DataIdPo();
        if(DesignType.data.equals(designType)) {
            dataIdPo.setModelId(designId);
        }
        if(DesignType.workflow.equals(designType)) {
            dataIdPo.setFlowDesignId(designId);
        }
        dataIdPo.setUpdateTime(LocalDateTime.now());
        this.handleId(dataIdPo, id -> size);
        this.save(dataIdPo);
        // 初始序号为1
        this.handleId(dataIdPo, id -> 1);
        return dataIdPo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUpdateDataId(DesignType designType, Collection<String> designIds) {
        List<DataIdPo> dataIdPos = designIds.stream().map(designId -> {
            String lastTimeKey = MODE_NEXT_ID_LAST_TIME_CACHE + designId;
            LocalDateTime last = Optional.ofNullable(redisUtils.get(lastTimeKey)).map(t -> LocalDateTimeUtil.parse(String.valueOf(t), DatePattern.NORM_DATETIME_PATTERN)).orElse(null);
            if (ObjectNull.isNull(last)) {
                return null;
            }
            String currentIdKey = MODE_NEXT_ID_CACHE + designId;
            String yearIdKey = MODE_YEAR_NEXT_ID_CACHE + designId;
            String monthIdKey = MODE_MONTH_NEXT_ID_CACHE + designId;
            String dayIdKey = MODE_DAY_NEXT_ID_CACHE + designId;
            String hourIdKey = MODE_HOUR_NEXT_ID_CACHE + designId;
            LambdaQueryWrapper<DataIdPo> wrapper = Wrappers.<DataIdPo>lambdaQuery();
            if(DesignType.data.equals(designType)) {
                wrapper.eq(DataIdPo::getModelId, designId);
            }
            if(DesignType.workflow.equals(designType)) {
                wrapper.eq(DataIdPo::getFlowDesignId, designId);
            }
            DataIdPo dataIdPo = this.getOne(wrapper);
            if (ObjectNull.isNull(dataIdPo)) {
                return null;
            }
            dataIdPo.setCurrentId((int) redisUtils.get(currentIdKey))
                    .setCurrentYearId((int) redisUtils.get(yearIdKey))
                    .setCurrentMonthId((int) redisUtils.get(monthIdKey))
                    .setCurrentDayId((int) redisUtils.get(dayIdKey))
                    .setCurrentHourId((int) redisUtils.get(hourIdKey))
                    .setUpdateTime(last);
            if(DesignType.data.equals(designType)) {
                dataIdPo.setModelId(designId);
            }
            if(DesignType.workflow.equals(designType)) {
                dataIdPo.setFlowDesignId(designId);
            }
            return dataIdPo;
        }).filter(ObjectNull::isNotNull).collect(Collectors.toList());
        if (ObjectNull.isNull(dataIdPos)) {
            return;
        }
        // 保存数据
        updateBatchById(dataIdPos);
    }

    /**
     * 定时保存数据序号任务
     */
    private void timingSaveDataId() {
        // 每30秒同步一次
        CronUtil.schedule("*/30 * * * * *", (Task) () -> {
            try {
                // 同步数据序号
                syncUpdateDataId();
            } catch (Exception e) {
                log.error("定时同步数据序号到数据库异常：{}", e.getMessage());
            }
        });
    }

    /**
     * 同步数据序号
     */
    private void syncUpdateDataId() {
        // 移除不活跃的设计id
        removeActiveModel();
        // 得到需要同步数据序号的模型id
        Set<String> modelIds = ACTIVE_MODEL.keySet();
        if (ObjectNull.isNotNull(modelIds)) {
            syncUpdateDataId(DesignType.data, modelIds);
        }

        // 得到需要同步数据序号的流程设计id
        Set<String> flowDesignIds = ACTIVE_FLOW.keySet();
        if (ObjectNull.isNotNull(flowDesignIds)) {
            syncUpdateDataId(DesignType.workflow, flowDesignIds);
        }
    }


    /**
     * 设置为活跃的模型
     *
     * @param designId 设计id
     */
    private void setActiveModel(DesignType designType, String designId) {
        if(DesignType.data.equals(designType)) {
            ACTIVE_MODEL.put(designId, LocalDateTime.now());
        }
        if(DesignType.workflow.equals(designType)) {
            ACTIVE_FLOW.put(designId, LocalDateTime.now());
        }
    }

    /**
     * 移除不活跃的模型
     */
    private static void removeActiveModel() {
        LocalDateTime now = LocalDateTime.now();
        List<String> removeKey = ACTIVE_MODEL.entrySet()
                .stream()
                .filter(e -> ChronoUnit.HOURS.between(e.getValue(), now) != 0)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        removeKey.forEach(ACTIVE_MODEL::remove);

        List<String> removeFlowKey = ACTIVE_FLOW.entrySet()
                .stream()
                .filter(e -> ChronoUnit.HOURS.between(e.getValue(), now) != 0)
                .map(Map.Entry::getKey).collect(Collectors.toList());
        removeFlowKey.forEach(ACTIVE_FLOW::remove);
    }


    /**
     * 批量设置序号
     *
     * @param dataIdPo    序号存储对象
     * @param idConverter 序号处理
     */
    private void handleId(DataIdPo dataIdPo, Function<Integer, Integer> idConverter) {
        dataIdPo.setCurrentId(idConverter.apply(dataIdPo.getCurrentId()));
        dataIdPo.setCurrentYearId(idConverter.apply(dataIdPo.getCurrentYearId()));
        dataIdPo.setCurrentMonthId(idConverter.apply(dataIdPo.getCurrentMonthId()));
        dataIdPo.setCurrentDayId(idConverter.apply(dataIdPo.getCurrentDayId()));
        dataIdPo.setCurrentHourId(idConverter.apply(dataIdPo.getCurrentHourId()));
    }

    @Override
    public void removeId(DesignType designType, Collection<String> designIds) {
        if (ObjectNull.isNull(designIds)) {
            return;
        }
        // 删除数据
        remove(Wrappers.<DataIdPo>lambdaQuery()
                .in(DesignType.data.equals(designType), DataIdPo::getModelId, designIds)
                .in(DesignType.workflow.equals(designType), DataIdPo::getFlowDesignId, designIds));
        // 删除缓存
        Set<String> lastTimeKeys = designIds.stream()
                .map(designId -> MODE_NEXT_ID_LAST_TIME_CACHE + designId)
                .collect(Collectors.toSet());
        redisUtils.del(lastTimeKeys);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataIdPo syncIdByMaxValue(String modelId, int maxValue) {
        if (StringUtils.isBlank(modelId)) {
            return null;
        }
        final int size = Math.max(maxValue, 1);
        LambdaQueryWrapper<DataIdPo> wrapper = Wrappers.<DataIdPo>lambdaQuery()
                .eq(DataIdPo::getModelId, modelId);
        DataIdPo dataIdPo = this.getOne(wrapper);
        if (Objects.isNull(dataIdPo)) {
            dataIdPo = new DataIdPo();
            dataIdPo.setModelId(modelId);
        }
        // 仅修正全局不重置序号，按年/月/天/小时的计数器保持原值不变
        dataIdPo.setCurrentId(size)
                .setUpdateTime(LocalDateTime.now());
        this.saveOrUpdate(dataIdPo);
        // 清除 Redis 缓存，确保下次 nextId 从数据库重新加载
        String currentIdKey = MODE_NEXT_ID_CACHE + modelId;
        String yearIdKey = MODE_YEAR_NEXT_ID_CACHE + modelId;
        String monthIdKey = MODE_MONTH_NEXT_ID_CACHE + modelId;
        String dayIdKey = MODE_DAY_NEXT_ID_CACHE + modelId;
        String hourIdKey = MODE_HOUR_NEXT_ID_CACHE + modelId;
        String lastTimeKey = MODE_NEXT_ID_LAST_TIME_CACHE + modelId;
        redisUtils.del(currentIdKey, yearIdKey, monthIdKey, dayIdKey, hourIdKey, lastTimeKey);
        log.info("同步数据序号完成, modelId={}, maxValue={}", modelId, size);
        return dataIdPo;
    }
}