package cn.bctools.rule.service;

import cn.bctools.auth.api.dto.EnvironmentVariableDto;

import java.util.List;

/**
 * @author jvs
 */
public interface ModelInterface {
    /**
     * 获取环境变量
     */
    List<EnvironmentVariableDto> getAll();

    /**
     * 获取对应的值
     *
     * @param label
     * @return
     */
    Object getByKey(String label);
}
