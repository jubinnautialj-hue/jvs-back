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
     */
    @Insert("${sql}")
    int insert(@Param("sql") String sql);

    /**
     * 删除
     *
     * @param sql
     */
    @Delete("${sql}")
    int delete(@Param("sql") String sql);

    /**
     * 修改
     *
     * @param sql
     */
    @Update("${sql}")
    int update(@Param("sql") String sql);

    /**
     * 查询
     *
     * @param sql 执行SQL
     */
    @Select("${sql}")
    Map<String, Object> selectMap(@Param("sql") String sql);

    /**
     * 分页查询 带参数
     *
     * @param page
     * @param sql
     */
    @Select("${sql}")
    Page<Map<String, Object>> selectPage(@Param("sql") String sql, Page<Map<String, Object>> page);

    /**
     * 查询所有
     */
    @Select("${sql}")
    List<Map<String, Object>> selectList(@Param("sql") String sql);

}
