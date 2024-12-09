package cn.bctools.design.notice.handler.send.mq;

import cn.bctools.design.notice.dto.QueryDataNoticeRespDto;
import cn.bctools.design.workflow.entity.FlowTask;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 解析配置发送消息
 */
@Data
@Accessors(chain = true)
public class SendNotice {
    private String tenantId;
    private List<QueryDataNoticeRespDto> noticePos;
    private Map<String, Object> data;
    private FlowTask flowTask;
    private List<String> taskNodeIds;
}
