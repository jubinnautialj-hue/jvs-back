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
                log.info("[AppTemplateDataBase.saveOrUpdate] 执行UPDATE，数量={}", value.size());
                iService.updateBatchById(value);
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
