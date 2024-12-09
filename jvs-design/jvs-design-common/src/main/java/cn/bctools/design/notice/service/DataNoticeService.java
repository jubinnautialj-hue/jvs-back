package cn.bctools.design.notice.service;

import cn.bctools.common.enums.SysNoticeConfig;
import cn.bctools.design.notice.dto.DataNoticeDto;
import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import cn.bctools.design.workflow.entity.FlowTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 消息通知
 */
public interface DataNoticeService  extends IService<DataNoticePo> {

    /**
     * 模型消息模板——新增或修改消息通知配置
     * @param appId
     * @param dto
     *
     */
    void saveDataNotice(String appId, DataNoticeDto dto);

    /**
     * 模型消息模板——获取模型所有通知配置
     *
     * @param appId
     * @param modelId 模型id
     * @return 通知配置集合
     */
    List<QueryDataNoticeRespDto> getAllByModelId(String appId, String modelId);


    /**
     * 模型消息模板——获取启用的通知配置
     *
     * @param appId
     * @param modelId 模型id
     * @param flowTask 工作流任务
     * @return 通知配置集合
     */
    List<QueryDataNoticeRespDto> getEffective(String appId, String modelId, FlowTask flowTask);


    /**
     * 获取租户级通知配置
     *
     * @param triggerType 消息触发类型枚举
     * @return
     */
    SysNoticeConfig getTenantSystemConfig(TriggerTypeEnum triggerType);

}
