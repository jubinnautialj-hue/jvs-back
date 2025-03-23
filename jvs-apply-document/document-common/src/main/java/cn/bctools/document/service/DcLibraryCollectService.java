package cn.bctools.document.service;

import cn.bctools.document.entity.DcLibraryCollect;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 知识库-收藏
 *
 * @Author: GuoZi
 */
public interface DcLibraryCollectService extends IService<DcLibraryCollect> {

    /**
     * 获取当前用户的收藏集合
     *
     * @return 收藏集合
     */
    List<String> getCollection();

    /**
     * 收藏文档与取消收藏
     *
     * @param docId 文档id
     * @return 是否收藏
     */
    boolean collect(String docId);

}
