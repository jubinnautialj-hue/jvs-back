package cn.bctools.document.service;

import cn.bctools.document.entity.DcTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 文档模板
 *
 * @Author: GuoZi
 */
public interface DcTemplateService extends IService<DcTemplate> {

    /**
     * 根据id获取文档模板, 并做空值校验
     *
     * @param id 文档id
     * @return 文档模板数据
     */
    DcTemplate get(String id);


}
