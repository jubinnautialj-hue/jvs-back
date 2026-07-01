package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 工作流设计版本服务
 */
public interface FlowDesignVersionService extends IService<FlowDesignVersion> {

    /**
     * 保存设计中的版本
     *
     * @param flowDesignId 流程设计id
     * @param appId        应用id
     * @param designBody   流程设计
     */
    void saveVersionDesigning(String flowDesignId, String appId, String designBody);

    /**
     * 发布版本
     *
     * @param flowDesignId 工作流设计id
     */
    void publish(String flowDesignId);

    /**
     * 获取下一个版本号
     *
     * @param flowDesignVersions 版本集合
     * @return 版本号
     */
    Integer getNextVersion(List<FlowDesignVersion> flowDesignVersions);

    /**
     * 获取指定状态的版本
     *
     * @param flowDesignId  工作流设计id
     * @param versionStatus 工作流版本状态
     * @return 工作流设计版本
     */
    FlowDesignVersion getVersion(String flowDesignId, FlowDesignVersionStatusEnum versionStatus);

    /**
     * 批量获取工作流设计
     *
     * @param flowDesigns   工作流设计集合
     * @param versionStatus 工作流版本状态
     * @return Map<工作流设计id, 完整的工作流设计>
     */
    Map<String, String> getBatchDesignBody(Collection<FlowDesign> flowDesigns, FlowDesignVersionStatusEnum versionStatus);

    /**
     * 批量获取工作流设计
     *
     * @param flowDesignIds 工作流设计id集合
     * @param versionStatus 工作流版本状态
     * @return Map<工作流设计id, 工作流设计版本>
     */
    Map<String, FlowDesignVersion> getBatchDesignVersion(Collection<String> flowDesignIds, FlowDesignVersionStatusEnum versionStatus);

    /**
     * 批量获取工作流设计版本
     *
     * @param flowDesignIds 工作流设计id集合
     * @return Map<工作流设计id, 版本集合>
     */
    Map<String, List<FlowDesignVersion>> getFlowDesignAllVersion(List<String> flowDesignIds);
}
