package cn.bctools.design.data.aspect;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.service.DataModelService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 转换模型的数据集名
 */

@Slf4j
@Aspect
@Data
@Component
@Order(3)
public class ModelCollectionNameAspect {

    @Resource
    private DataModelService dataModelService;

    /**
     * 数据集缓存
     * Map<模型id，数据集名>
     */
    public static final String COLLECTION_NAME_CACHE = "collection_name_cache";

    @SneakyThrows
    @Around("@annotation(dataModelAlias)")
    public Object around(ProceedingJoinPoint point, ModelCollectionName dataModelAlias) {
        Object[] args = point.getArgs();
        if (ObjectUtil.isNotNull(dataModelAlias.modelId())) {
            MethodSignature methodSignature = (MethodSignature) point.getSignature();
            //参数名数组
            String[] parameters = methodSignature.getParameterNames();
            for (int i = 0; i < parameters.length; i++) {
                if (dataModelAlias.modelId().equals(parameters[i])) {
                    args[i] = getCollectionName(args[i]);
                }
            }
        }
        //获取request对象
        Object obj = null;
        try {
            obj = point.proceed(args);
        } catch (DuplicateKeyException throwable) {
            String substring = throwable.getCause().getMessage().substring(throwable.getCause().getMessage().indexOf("index: "));
            String[] split = substring.split(" ");
            String error = split[1];
            throw new BusinessException("数据唯一性校验不通过,存在重复提交,请检查," + error + "异常");
        } catch (Throwable throwable) {
            log.error("AOP拦截到错误,数据操作失败", throwable);
            throw new BusinessException("数据操作失败");
        }
        return obj;
    }

    /**
     * 获取数据集名
     *
     * @param modelIdObj 模型id
     * @return
     */
    public String getCollectionName(Object modelIdObj) {
        // 优先从缓存中获取数据集名
        String collectionName = getCollectionNameCache(modelIdObj);
        if (ObjectNull.isNotNull(collectionName)) {
            return collectionName;
        }
        // 查询数据集名
        String modelIdStr = String.valueOf(modelIdObj);
        String[] arr = modelIdStr.split(DataModelUtil.SPLIT);
        // 约定最后一个是数据集的名称（如：log_模型id）
        String modelId = arr[arr.length - 1];
        collectionName = getCollectionNameCache(modelId);
        if (ObjectNull.isNotNull(collectionName)) {
            return modelIdStr.replace(modelId, collectionName);
        }
        DataModelPo dataModelPo = dataModelService.getById(modelId);
        if (ObjectNull.isNotNull(dataModelPo)) {
            collectionName = Optional.ofNullable(dataModelPo.getCollectionName()).orElse(dataModelPo.getId());
            setCache(dataModelPo.getId(), collectionName);
            return modelIdStr.replace(modelId, collectionName);
        }
        return modelIdStr;
    }


    /**
     * 设置数据集缓存
     *
     * @param reqModelId     参数中的模型id（可能是模型id，也可能是拼装好的数据集名。如：log_模型id）
     * @param collectionName 对应的
     */
    private static void setCache(Object reqModelId, String collectionName) {
        Map<Object, String> cache = Optional.ofNullable(getCache()).orElse(new HashMap<>(1));
        cache.put(reqModelId, collectionName);
        SystemThreadLocal.set(COLLECTION_NAME_CACHE, cache);
    }

    /**
     * 获取数据集缓存
     *
     * @return
     */
    private static Map<Object, String> getCache() {
        return SystemThreadLocal.get(COLLECTION_NAME_CACHE);
    }

    /**
     * 获取指定模型的数据集名
     *
     * @param reqModelId
     * @return
     */
    private static String getCollectionNameCache(Object reqModelId) {
        return Optional.ofNullable(getCache()).orElse(new HashMap<>(1)).get(reqModelId);
    }

}
