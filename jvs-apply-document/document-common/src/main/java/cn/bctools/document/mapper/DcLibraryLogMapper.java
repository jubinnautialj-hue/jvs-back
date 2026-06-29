package cn.bctools.document.mapper;

import cn.bctools.document.entity.DcLibraryLog;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Auto Generator
 */
@Mapper
public interface DcLibraryLogMapper extends BaseMapper<DcLibraryLog> {

    /**
     * 常用文库，添加 knowledge_id IN 条件
     *
     * @param id           用户id
     * @param knowledgeIds knowledge_id 列表
     */
    @Select({
            "<script>",
            "SELECT subquery.knowledge_id FROM(",
            "SELECT knowledge_id ,MAX(create_time) AS max_create_time ",
            "FROM `dc_library_log` ",
            "WHERE user_id = #{id} ",
            "<if test='knowledgeIds != null and knowledgeIds.size() > 0'>",
            "AND knowledge_id IN ",
            "<foreach item='knowledgeId' index='index' collection='knowledgeIds' open='(' separator=',' close=')'>",
            "#{knowledgeId}",
            "</foreach>",
            "</if>",
            " GROUP BY knowledge_id)  AS subquery",
            "order by subquery.max_create_time DESC  LIMIT 10",
            "</script>"
    })
    List<String> frequentlyKnowledge(@Param("id") String id, @Param("knowledgeIds") List<String> knowledgeIds);

    /**
     * 常用文档
     *
     * @param id            用户id
     * @param operationType 操作类型
     * @param type          文件类型
     */
    @Select({
            "<script>",
            "SELECT subquery.max_create_time AS create_time  ,dc_name,user_name,dc_library_id,knowledge_id,name_suffix,type,knowledge_name FROM(",
            "SELECT * ,MAX(create_time) AS max_create_time ",
            "FROM `dc_library_log` ",
            "WHERE user_id = #{userId} ",
            "<if test=\"operationType!=null and operationType!=''\">  and operation_type =#{operationType} </if>",
            "<if test=\"knowledgeId!=null and knowledgeId!=''\">  and knowledge_id =#{knowledgeId} </if>",
            "<if test=\"type!=null\">  and type =#{type} </if> GROUP BY dc_library_id)  AS subquery",
            "order by subquery.max_create_time DESC  LIMIT 20",
            "</script>"
    })
    List<DcLibraryLog> frequently(@Param("userId") String id, @Param("operationType") String operationType, @Param("type") DcLibraryTypeEnum type,@Param("knowledgeId") String knowledgeId);

}
