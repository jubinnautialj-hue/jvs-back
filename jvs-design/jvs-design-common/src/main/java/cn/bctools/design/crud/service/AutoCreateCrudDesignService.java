package cn.bctools.design.crud.service;

import cn.bctools.design.crud.dto.AutoCreateCrudDesignDto;

/**
 * @author jvs
 * 自动创建CRUD设计
 */
public interface AutoCreateCrudDesignService {

    /**
     * 生成设计
     *
     * @param autoDto 自动生成设计参数
     */
    void generate(AutoCreateCrudDesignDto autoDto);
}
