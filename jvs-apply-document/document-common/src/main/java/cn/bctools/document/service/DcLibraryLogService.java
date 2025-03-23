package cn.bctools.document.service;

import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryLog;
import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Auto Generator
 */
public interface DcLibraryLogService extends IService<DcLibraryLog> {

    /**
     * 通过日志类型分组统计数量
     *
     * @param id 文库id
     * @return 统计数据
     */
    Dict groupByCount(String id);

    /**
     * 常用文档
     *
     * @param dcLibraryLog 入参
     * @return 集合
     */
    List<DcLibraryLog> frequently(DcLibraryLog dcLibraryLog);

    /**
     * 常用文库
     *
     * @return 集合
     */
    List<DcLibrary> frequentlyKnowledge();

}
