package cn.bctools.design.project.service;

import cn.bctools.design.project.entity.JvsAppLog;
import cn.bctools.design.project.entity.enums.AppLogTypeEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;


/**
 * @author Auto Generator
 */
public interface JvsAppLogService extends IService<JvsAppLog> {

    /**
     * 保存业务操作日志
     *
     * @param modelId    模型id
     * @param type       类型
     * @param beforeJson 变更前
     * @param afterJson  变更后
     */
    void savelog(String modelId, AppLogTypeEnum type, Map<String, Object> beforeJson, Map<String, Object> afterJson);
}
