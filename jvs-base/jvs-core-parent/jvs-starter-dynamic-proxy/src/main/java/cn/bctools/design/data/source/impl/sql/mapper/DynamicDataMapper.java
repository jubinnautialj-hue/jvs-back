package cn.bctools.design.data.source.impl.sql.mapper;

import cn.bctools.design.data.source.impl.sql.Constant;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * The interface Dynamic data mapper.
 *
 * @Author: ZhuXiaoKang
 * @Description: 动态数据
 * @see Constant
 */
@Mapper
public interface DynamicDataMapper {

    /**
     * Gets one.
     *
     * @param sql        the sql
     * @param whereParam the where param
     * @return the one
     */
    @Select("<script>${sql}</script>")
    JSONObject getOne(@Param(Constant.SQL) String sql, @Param(Constant.WHERE_PARAM) Map<String, Object> whereParam);

    /**
     * List list.
     *
     * @param sql        the sql
     * @param whereParam the where param
     * @return the list
     */
    @Select("<script>${sql}</script>")
    List<JSONObject> list(@Param(Constant.SQL) String sql, @Param(Constant.WHERE_PARAM) Map<String, Object> whereParam);

    /**
     * Count long.
     *
     * @param sql        the sql
     * @param whereParam the where param
     * @return the long
     */
    @Select("<script>${sql}</script>")
    Long count(@Param(Constant.SQL) String sql, @Param(Constant.WHERE_PARAM) Map<String, Object> whereParam);

    /**
     * Insert integer.
     *
     * @param <T>  the type parameter
     * @param sql  the sql
     * @param data the data
     * @return the integer
     */
    @Insert("<script>${sql}</script>")
    <T> Integer insert(@Param(Constant.SQL) String sql, @Param(Constant.DATA) T data);

    /**
     * Update long.
     *
     * @param <T>        the type parameter
     * @param sql        the sql
     * @param data       the data
     * @param whereParam the where param
     * @return the long
     */
    @Update("<script>${sql}</script>")
    <T> Long update(@Param(Constant.SQL) String sql, @Param(Constant.DATA) T data, @Param(Constant.WHERE_PARAM) Map<String, Object> whereParam);

    /**
     * Delete integer.
     *
     * @param sql        the sql
     * @param whereParam the where param
     * @return the integer
     */
    @Delete("<script>${sql}</script>")
    Integer delete(@Param(Constant.SQL) String sql, @Param(Constant.WHERE_PARAM) Map<String, Object> whereParam);
}
