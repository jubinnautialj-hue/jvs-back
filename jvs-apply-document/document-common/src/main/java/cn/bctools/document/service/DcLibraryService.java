package cn.bctools.document.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.dto.ResetSortDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.vo.req.DcLibraryAddReqVo;
import cn.bctools.document.vo.req.DcLibraryEditReqVo;
import cn.bctools.document.vo.req.DcLocationOtherDto;
import cn.bctools.document.vo.res.PreviewDocumentResVo;
import cn.bctools.oss.dto.BaseFile;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author auto
 */
public interface DcLibraryService extends IService<DcLibrary> {

    /**
     * 根据id获取文档, 并做空值校验
     *
     * @param id 文档id
     * @return 文档数据
     */
    DcLibrary get(String id);

    /**
     * 下载源文件 富文本
     * @param id 文件id
     * @return 打包后的文件 oss对象
     * */
    BaseFile downSourceFile(String id) throws IOException;
    /**
     * 上传源文件 富文本
     * @param baseFile 文件oss对象
     * @param parentId 上级id
     * @param userDto 用户信息
     * @param logId 日志id
     * @return
     * */
    void upSourceFile(BaseFile baseFile,String parentId,UserDto userDto,String logId) throws IOException;

    /**
     * 文件入库
     *
     * @param baseFile 文件信息
     * @param id       文档id
     * @param fileName 文件名称
     * @return 文档数据
     */
    DcLibrary fileToSave(String id, BaseFile baseFile, String fileName);

    /**
     * 文件入库
     *
     * @param baseFile 文件信息
     * @param id       文档id
     * @param fileName 文件名称
     * @return 文档数据
     */
    DcLibrary fileToSave(String id, BaseFile baseFile, String fileName, UserDto currentUser);

    /**
     * 获取用户作为拥有者的文库数据
     *
     * @return 文库id
     */
    List<String> getOwner();



    /**
     * 删除一个知识库，或目录，或文档
     *
     * @param id 知识库ID，或目录ID，或文档ID
     * @return 删除的id集合
     */
    void removeDc(String id);

    /**
     * 删除一个知识库，或目录，或文档
     *
     * @param dcLibrary 文档数据
     * @return 删除的id集合
     */
    void removeDc(DcLibrary dcLibrary);

    /**
     * 移动到回收站
     *
     * @param id 知识库ID，或目录ID，或文档ID
     * @return 移动到回收站的id集合
     */
    DcLibrary recycleDc(String id);

    /**
     * 获取文件流
     *
     * @param id 文件id
     */
    void getFile(String id);

    /**
     * 下载为类型 office文件
     *
     * @param id   文件id
     * @param type 文件类型
     */
    String downOffice(String id, String type) throws IOException;

    /**
     * 清空当前目录下面的所有定制权限
     *
     * @param id 知识库ID，或目录ID，或文档ID
     */
    void clearAuth(String id);


    /**
     * 文件自动转换  doc->docx xls->xlsx
     *
     * @param id 文档id
     */
    void fileTransition(String id);

    /**
     * 检查文件或文件夹名称重复
     *
     * @param id     上级id
     * @param name   文件名称
     * @param type   新建的数据类型
     * @param parent 是否为上级id
     * @return 是否
     */
    Boolean checkRepetition(String id, String name, DcLibraryTypeEnum type, Boolean parent);

    /**
     * 移动文件或者文件夹
     *
     * @param dcLocationOtherDto 此次移动的入参
     */
    void locationOther(DcLocationOtherDto dcLocationOtherDto);

    /**
     * 新增知识库 目录 或文档
     *
     * @param userDto 当前用户
     * @param reqVo   新增入参
     * @return
     */
    DcLibrary add(UserDto userDto, DcLibraryAddReqVo reqVo);


    /**
     * 获取知识库树形目录结构
     *
     * @return 树形目录结构
     */
    List<DcLibrary> currentLevel();

    /**
     * 获取某一个分支的数据
     *
     * @param id 文档
     * @return 树形目录结构
     */
    List<String> getTreeName(String id);

    /**
     * 获取子集属于哪个知识库
     *
     * @param id 子集id
     * @return 知识库  {@link DcLibrary}
     */
    DcLibrary getKnowledgeByChildren(String id);

    /**
     * 重命名知识库/目录/文档，或设置知识库
     *
     * @param userDto 登录用户
     * @param dto     知识库/目录/文档
     * @return 知识库/目录/文档 {@link DcLibrary}
     */
    DcLibrary put(UserDto userDto, DcLibraryEditReqVo dto);

    /**
     * 查询知识库成员
     *
     * @param id   知识库id
     * @param page 分页
     * @return 知识库成员
     */
    Page<DcAuthConfig> queryUser(Page<DcAuthConfig> page, String id);


    /**
     * 查询用户有权限的知识库
     *
     * @param page      分页
     * @param dcLibrary 查询条件
     * @return 分页对象
     */
    Page<DcLibrary> queryKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary);

    /**
     * 查询用户自己的知识库
     *
     * @param page      分页
     * @param dcLibrary 查询条件
     * @return {@link DcLibrary}
     */
    Page<DcLibrary> queryOwnerKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary);

    /**
     * 获取指定知识库所有下级节点id(仅包括文档)
     *
     * @param id 目录id
     * @return 节点id集合
     */
    Set<String> getAllChildDocumentId(String id);


    /**
     * 保存文档
     *
     * @param userId     登录用户
     * @param dcLibrary  文档内容
     * @param documentId 文档id
     */
    void saveContent(String userId, DcLibrary dcLibrary, String documentId);


    /**
     * 文档预览
     *
     * @param id      知识库文档id
     * @param userDto 用户信息
     * @return {@link PreviewDocumentResVo}
     */
    PreviewDocumentResVo preview(String id, UserDto userDto);

    /**
     * 文档预览
     *
     * @param dcLibrary 知识库文档
     * @param userDto   用户信息
     * @return {@link PreviewDocumentResVo}
     */
    PreviewDocumentResVo preview(DcLibrary dcLibrary, UserDto userDto);

    /**
     * 找回删除的数据
     *
     * @param dcLibrary 恢复的数据
     */
    void retrieve(DcLibrary dcLibrary);

    /**
     * 获取数据不管是否删除
     *
     * @param id 数据id
     * @return 查询的对象
     */
    DcLibrary getAllId(String id);

    /**
     * 查找删除的数据
     *
     * @param page 分页对象
     */
    void recycle(Page<DcLibrary> page);

    /**
     * 重置排序
     *
     * @param resetSortDto
     */
    void resetSort(ResetSortDto resetSortDto);
}
