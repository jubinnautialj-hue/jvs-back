package cn.bctools.rule.data.mysql.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Mapper
public interface DynamicMapper {

    /**
     * 插入
     *
     * @param sql sql语句
     * @return
     */
    @Insert("${sql}")
    int insert(@Param("sql") String sql);

    /**
     * 删除
     *
     * @param sql sql语句
     * @param sql
     * @return
     */
    @Delete("${sql}")
    int delete(@Param("sql") String sql);

    /**
     * 修改
     *
     * @param sql sql语句
     * @param sql
     * @return
     */
    @Update("${sql}")
    int update(@Param("sql") String sql);

    /**
     * 查询
     *
     * @param sql sql语句
     * @param sql 执行SQL
     * @return
     */
    @Select("${sql}")
    Map<String, Object> selectMap(@Param("sql") String sql);

    /**
     * 分页查询 带参数
     *
     * @param sql  sql语句
     * @param page
     * @param sql
     * @return
     */
    @Select("${sql}")
    Page<Map<String, Object>> selectPage(@Param("sql") String sql, Page<Map<String, Object>> page);

    /**
     * 查询所有
     *
     * @param sql sql语句
     * @return
     */
    @Select("${sql}")
    List<Map<String, Object>> selectList(@Param("sql") String sql);

}
