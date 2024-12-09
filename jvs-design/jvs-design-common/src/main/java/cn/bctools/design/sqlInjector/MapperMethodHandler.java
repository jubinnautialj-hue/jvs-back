package cn.bctools.design.sqlInjector;

import cn.bctools.common.utils.ObjectNull;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Mapper method handler.
 *
 * @author zhuxiaokang  调用自定义的Mapper方法 <p>     若不继承CustomBaseMapper， 也可以直接调用此类的方法
 */
@Component
public class MapperMethodHandler {

    /**
     * The Log.
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * 根据主键id批量物理删除
     *
     * @param iService mybatis-plus#IService接口的实现类
     * @param ids      主键id集合
     */
    public void deletePhysicalByIds(IService<?> iService, Collection<?> ids) {
        if (ObjectNull.isNull(ids)) {
            return;
        }
        Class<?> entityClass = iService.getEntityClass();
        Class<?> currentMapper = ReflectionKit.getSuperClassGenericType(iService.getClass(), ServiceImpl.class, 0);
        String sql = currentMapper.getName() + StringPool.DOT + DeletePhysicalByIds.METHOD;
        SqlHelper.executeBatch(entityClass, log, ids, ids.size(), (sqlSession, e) -> {
            Map<String, Collection<?>> param = new HashMap<>(ids.size());
            param.put(Constants.COLL, ids);
            sqlSession.delete(sql, param);
        });
    }

    /**
     * 条件物理删除
     *
     * @param <T>           the type parameter
     * @param iService      mybatis-plus#IService接口的实现类
     * @param deleteWrapper 删除条件
     */
    public <T> void deletePhysical(IService<T> iService, Wrapper<T> deleteWrapper) {
        Class<?> entityClass = iService.getEntityClass();
        Class<?> currentMapper = ReflectionKit.getSuperClassGenericType(iService.getClass(), ServiceImpl.class, 0);
        SqlSession sqlSession = SqlHelper.sqlSession(entityClass);
        try {
            String sql = currentMapper.getName() + StringPool.DOT + DeletePhysical.METHOD;
            Map<String, Object> map = CollectionUtils.newHashMapWithExpectedSize(1);
            map.put(Constants.WRAPPER, deleteWrapper);
            sqlSession.delete(sql, map);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, GlobalConfigUtils.currentSessionFactory(entityClass));
        }
    }
}
