package cn.bctools.design.sqlInjector;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * The interface Custom base mapper.
 *
 * @param <T> the type parameter
 * @author zhuxiaokang 自定义扩展BaseMapper方法 <p>     若需要使用扩展的方法，可将原本继承BaseMapper的类改为继承CustomBaseMapper
 */
public interface CustomBaseMapper<T> extends BaseMapper<T> {

    /**
     * 批量物理删除
     *
     * @param idList id集合
     * @return int int
     */
    int deletePhysicalByIds(@Param(Constants.COLL) Collection<?> idList);


    /**
     * Delete physical int.
     *
     * @param queryWrapper the query wrapper
     * @return the int
     */
    int deletePhysical(@Param("ew") Wrapper<T> queryWrapper);
}
