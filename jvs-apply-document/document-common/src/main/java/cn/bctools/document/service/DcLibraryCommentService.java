package cn.bctools.document.service;

import cn.bctools.document.entity.DcLibraryComment;
import cn.bctools.document.vo.DcLibraryCommentVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author wenxin
 * @Desc
 **/
public interface DcLibraryCommentService extends IService<DcLibraryComment> {
    /**
     * 获取评论信息
     *
     * @param parentId    上级id
     * @param knowledgeId 文库id
     * @param page        分页数据
     * @return 分页数据
     */
    Page<DcLibraryCommentVo> getPage(Page<DcLibraryComment> page, String knowledgeId, String parentId);

}
