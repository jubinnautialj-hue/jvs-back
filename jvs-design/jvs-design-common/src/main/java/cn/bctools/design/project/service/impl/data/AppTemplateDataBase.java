package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板数据服务顶级类
 */
@Slf4j
public class AppTemplateDataBase {

    private final MapperMethodHandler mapperMethodHandler = SpringContextUtil.getBean(MapperMethodHandler.class);

    /**
     * 清空默认数据
     *
     * @param t 对象
     * @param <T>
     */
    protected <T extends BasalPo> void clearDefaultData(T t) {
        t.setCreateBy(null);
        t.setCreateById(null);
        t.setUpdateBy(null);
        t.setCreateTime(null);
        t.setUpdateTime(null);
    }

    /**
     * 物理删除设计
     *
     * @param iService  mybatis-plus#IService接口的实现类
     * @param existsIds 目标版本已存在映射的设计id集合
     * @param list      目标版本模板中具体对象的数据集
     * @param getId     获取目标对象的id
     * @param <T>       目标对象
     * @param <R>       目标对象的id类型
     */
    protected <T, R> void delete(IService<T> iService, List<R> existsIds, List<T> list, Function<? super T, ? extends R> getId) {
        if (ObjectNull.isNull(existsIds) || ObjectNull.isNull(list)) {
            return;
        }
        // 删除
        Set<R> deleteIds = list.stream()
                .map(getId)
                .filter(id -> Boolean.FALSE.equals(existsIds.contains(id)))
                .collect(Collectors.toSet());
        if (ObjectNull.isNull(deleteIds)) {
           return;
        }
        mapperMethodHandler.deletePhysicalByIds(iService, deleteIds);
    }

    /**
     * 根据应用id物理删除设计
     *
     * @param iService  mybatis-plus#IService接口的实现类
     * @param getAppId     应用id字段
     * @param appId     应用id
     * @param <T>       目标对象
     */
    protected <T> void deleteByAppId(IService<T> iService, SFunction<T, ?> getAppId, String appId) {
        if (ObjectNull.isNull(appId)) {
            return;
        }
        // 删除
        mapperMethodHandler.deletePhysical(iService, Wrappers.<T>lambdaQuery().eq(getAppId, appId));
    }

    /**
     * 新增或修改数据
     *
     * @param iService  mybatis-plus#IService接口的实现类
     * @param existsIds 目标版本已存在映射的设计id集合
     * @param list      目标版本模板中具体对象的数据集
     * @param getId     获取目标对象的id
     * @param <T>       目标对象
     * @param <R>       目标对象的id类型
     */
    protected <T, R> void saveOrUpdate(IService<T> iService, List<R> existsIds, List<T> list, Function<? super T, ? extends R> getId) {
        log.info("[AppTemplateDataBase.saveOrUpdate] 调用saveOrUpdate - list.size={}, existsIds={}",
                list != null ? list.size() : 0, existsIds != null ? existsIds.size() : 0);
        if (ObjectNull.isNull(list)) {
            log.info("[AppTemplateDataBase.saveOrUpdate] list为null，直接返回");
            return;
        }
        if (ObjectNull.isNull(existsIds)) {
            log.info("[AppTemplateDataBase.saveOrUpdate] existsIds为null，执行INSERT，数量={}", list.size());
            iService.saveBatch(list);
            return;
        }
        Map<Boolean, List<T>> map = list.stream().collect(Collectors.groupingBy(p -> {
            R id = getId.apply(p);
            boolean exists = existsIds.contains(id);
            log.info("[AppTemplateDataBase.saveOrUpdate] 检查ID={}, 是否存在={}", id, exists);
            return exists;
        }));
        map.forEach((key, value) -> {
            if (key) {
                log.info("[AppTemplateDataBase.saveOrUpdate] 准备执行UPDATE，数量={}", value.size());
                
                if (value.size() > 0) {
                    // 记录UPDATE前的ID列表
                    List<R> allUpdateIds = value.stream().map(getId).collect(Collectors.toList());
                    List<R> sampleIds = value.stream().limit(10).map(getId).collect(Collectors.toList());
                    log.info("[AppTemplateDataBase.saveOrUpdate] UPDATE的ID列表（前10个）={}", sampleIds);
                    
                    // 执行UPDATE前，先检查这些ID在数据库中是否真实存在
                    List<T> existingRecords = iService.listByIds((Collection) allUpdateIds);
                    int existingCount = existingRecords != null ? existingRecords.size() : 0;
                    log.info("[AppTemplateDataBase.saveOrUpdate]  UPDATE前数据库实际存在的记录数={}/{}",
                            existingCount, allUpdateIds.size());
                    
                    if (existingCount == 0) {
                        // 数据库中没有任何记录，全部执行INSERT
                        log.warn("[AppTemplateDataBase.saveOrUpdate]  数据库中没有任何待UPDATE的记录！自动转为INSERT操作");
                        log.warn("[AppTemplateDataBase.saveOrUpdate]  原因：这些ID只存在于映射表，但实际数据表为空（脏数据）");
                        log.info("[AppTemplateDataBase.saveOrUpdate]  执行INSERT补偿操作，数量={}", value.size());
                        iService.saveBatch(value);
                        log.info("[AppTemplateDataBase.saveOrUpdate]  INSERT补偿操作完成");
                    } else if (existingCount < allUpdateIds.size()) {
                        // 部分记录存在，需要分别处理
                        log.warn("[AppTemplateDataBase.saveOrUpdate]  部分数据不存在：期望UPDATE {} 条，实际存在 {} 条记录",
                                allUpdateIds.size(), existingCount);
                        
                        // 获取存在的记录ID集合
                        Set<R> existingIds = existingRecords.stream()
                                .map(getId)
                                .collect(Collectors.toSet());
                        
                        // 分组：存在的执行UPDATE，不存在的执行INSERT
                        Map<Boolean, List<T>> splitMap = value.stream()
                                .collect(Collectors.groupingBy(item -> existingIds.contains(getId.apply(item))));
                        
                        // 执行UPDATE（已存在的记录）
                        List<T> toUpdate = splitMap.get(true);
                        if (toUpdate != null && !toUpdate.isEmpty()) {
                            log.info("[AppTemplateDataBase.saveOrUpdate] 对已存在的记录执行UPDATE，数量={}", toUpdate.size());
                            boolean updateResult = iService.updateBatchById(toUpdate);
                            log.info("[AppTemplateDataBase.saveOrUpdate] UPDATE操作完成，结果={}", updateResult);
                        }
                        
                        // 执行INSERT（不存在的记录）
                        List<T> toInsert = splitMap.get(false);
                        if (toInsert != null && !toInsert.isEmpty()) {
                            log.warn("[AppTemplateDataBase.saveOrUpdate]  对不存在的记录执行INSERT补偿操作，数量={}", toInsert.size());
                            iService.saveBatch(toInsert);
                            log.info("[AppTemplateDataBase.saveOrUpdate]  INSERT补偿操作完成");
                        }
                    } else {
                        // 所有记录都存在，正常执行UPDATE
                        log.info("[AppTemplateDataBase.saveOrUpdate]  所有记录在数据库中都存在，执行UPDATE操作");
                        boolean updateResult = iService.updateBatchById(value);
                        log.info("[AppTemplateDataBase.saveOrUpdate] UPDATE操作完成，结果={}", updateResult);
                    }
                }
            } else {
                log.info("[AppTemplateDataBase.saveOrUpdate] 执行INSERT，数量={}", value.size());
                iService.saveBatch(value);
            }
        });
    }

    /**
     * 新增
     *
     * @param iService  mybatis-plus#IService接口的实现类
     * @param list      目标版本模板中具体对象的数据集
     * @param <T>       目标对象
     */
    protected <T> void saveBatch(IService<T> iService, List<T> list) {
        if (ObjectNull.isNull(list)) {
            return;
        }
        iService.saveBatch(list);
    }

    /**
     * 设置应用版本号
     *
     * @param entity 实体类
     * @param versionSetter 设置应用版本号
     * @param targetAppVersion 目标版本
     * @param <T>
     */
    protected <T> void setAppVersion(T entity, BiConsumer<T, String> versionSetter, JvsAppVersion targetAppVersion) {
        String appVersion = null;
        // 非开发版本需要保存版本号, 开发版本的版本号默认为null
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(targetAppVersion.getVersionType()))) {
            appVersion = targetAppVersion.getAppVersion();
        }
        versionSetter.accept(entity, appVersion);
    }
}
