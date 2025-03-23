package cn.bctools.document.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.po.enums.DocumentLogTypeEnum;
import cn.bctools.document.vo.req.DocumentEditLogVo;
import cn.bctools.document.vo.req.DocumentSearchVo;
import cn.bctools.document.vo.res.DocumentEditLogResVo;
import cn.bctools.document.vo.res.DocumentSearchResVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库-文档es服务
 */
public interface DocumentElasticService {

    /**
     * 搜索知识库文档
     *
     * @param page             分页条件
     * @param documentSearchVo 查询条件
     * @return 文档信息集合
     */
    Page<DocumentSearchResVo> searchDoc(Page page, DocumentSearchVo documentSearchVo);
    /**
     * 搜索知识库文档
     *
     * @param page             分页条件
     * @param dcLibrary 查询主体的信息
     * @return 文档信息集合
     */
    Page<DocumentSearchResVo> searchDocNoAuth(Page page, DcLibrary dcLibrary);


    /**
     * 单个知识库文档保存
     *
     * @param userDto   登录用户
     * @param dcLibrary 知识库信息
     * @param content   内容
     */
    void save(UserDto userDto, DcLibrary dcLibrary, String content);

    /**
     * 修改库的某些字段
     *
     * @param dcLibrary 知识库信息
     * @param document  修改的值与对象
     */
    UpdateResponse update(DcLibrary dcLibrary, Document document);

    /**
     * 封装documentEsPO 入库信息
     *
     * @param userDto   用户
     * @param dcLibrary 知识库文档信息
     * @param content   文档内容
     * @param knowledge 知识库
     * @return
     */
    DocumentEsPo build(UserDto userDto, DcLibrary dcLibrary, String content, DcLibrary knowledge);

    /**
     * 知识库文档操作日志保存
     *
     * @param dcLibrary   文档信息
     * @param userName    操作人
     * @param userId      操作人id
     * @param logTypeEnum 操作类型
     * @param time        操作时间
     */
    @Deprecated
    void saveLog(DcLibrary dcLibrary, String userName, String userId, DocumentLogTypeEnum logTypeEnum, LocalDateTime time);

    /**
     * 查询文档日志记录
     *
     * @param page              分页
     * @param documentEditLogVo 搜索内容
     * @return 编辑记录集合
     */
    @Deprecated
    Page<DocumentEditLogResVo> searchDocumentEditLog(Page page, DocumentEditLogVo documentEditLogVo);

    /**
     * 获取文档已读次数
     *
     * @param id 文档id
     * @return 已读次数
     */
    @Deprecated
    Long searchDocumentReadTotal(String id);

    /**
     * 根据文档名称，搜索文档信息集合
     *
     * @param name 文档名称
     * @return 文档信息集合
     */
    @Deprecated
    List<DocumentEsPo> searchDocumentByName(String name);

    /**
     * 删除文档【索引document_base_info】
     * <p>
     * 该操作耗时较多, 使用了异步注解{@link org.springframework.scheduling.annotation.Async}
     *
     * @param tenantId 租户id
     * @param docIds   文档id
     */
    void deleteDocument(String tenantId, List<String> docIds);

    /**
     * 删除文档【索引document_log】
     * <p>
     *
     * @param tenantId 租户id
     * @param userId   用户id
     */
    void deleteLog(String tenantId, String userId);

    /**
     * 部分更新知识库
     *
     * @param dcLibrary 知识库数据
     */
    void updateDocumentEs(DcLibrary dcLibrary);

}
