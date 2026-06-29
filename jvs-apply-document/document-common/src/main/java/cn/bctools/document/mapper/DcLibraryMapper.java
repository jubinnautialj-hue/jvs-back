package cn.bctools.document.mapper;

import cn.bctools.document.entity.DcLibrary;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author auto
 */
@Mapper
public interface DcLibraryMapper extends BaseMapper<DcLibrary> {

    /**
     * 修改删除状态的值
     *
     * @param id 数据id
     */
    @Update("update dc_library set del_flag = 0  where id =#{id}")
    void retrieve(@Param("id") String id);

    /**
     * 获取逻辑删除的数据
     *
     * @param id 数据id
     * @return 数据
     */
    @Select("select * from dc_library  where del_flag = 1 and id =#{id}")
    DcLibrary getDeleteById(@Param("id") String id);

    @Select("select * from dc_library  where id =#{id}")
    DcLibrary getAllById(@Param("id") String id);

    @Select("select id from dc_library where  possessor = #{userId} and type='knowledge' and tenant_id=#{tenantId}")
    List<String> getCreateUser(@Param("userId") String userId, @Param("tenantId") String tenantId);

    @Delete("<script>" +
            "delete from dc_library  where id = #{id} or <if test=\"type == 'knowledge'\">knowledge_id = #{id}</if> <if test=\"type != 'knowledge'\"> parent_id = #{id}</if>" +
            "</script>")
    void deleteRecycle(@Param("id") String id, @Param("type") String type);


    @Select("<script>" +
            "select id,type from dc_library  where del_flag = 1 and id =#{id} or <if test=\"type == 'knowledge'\">knowledge_id = #{id}</if> <if test=\"type != 'knowledge'\"> parent_id = #{id}</if>" +
            "</script>")
    List<DcLibrary> recycleChildById(@Param("id") String id, @Param("type") String type);

    @Select("<script>" +
            "select id,name,color,name_suffix,type,update_time,knowledge_id,tenant_id,parent_id,update_by from dc_library where del_flag = 1" +
            "and (id in(" +
            "select id from dc_library where  possessor = #{userId} and type='knowledge' and tenant_id=#{tenantId})" +
            "or knowledge_id in(" +
            "select id from dc_library where  possessor = #{userId} and type='knowledge' and tenant_id=#{tenantId})" +
            ")" +
            "ORDER BY  update_time desc " +
//            "limit #{startIndex}, #{pageSize}" +
            "</script>")
    List<DcLibrary> recycle(@Param("userId") String userId, @Param("tenantId") String tenantId);


    @Select("<script>" +
            "SELECT *  FROM dc_library WHERE  del_flag=0 AND knowledge_id = #{id}  ORDER BY CONVERT(name USING GBK)" +
            "<if test=\"isSortDesc\"> DESC</if>" +
            "</script>")
    @InterceptorIgnore(tenantLine = "1")
    List<DcLibrary> resetSort(@Param("id") String id, @Param("isSortDesc") boolean isSortDesc);

}
