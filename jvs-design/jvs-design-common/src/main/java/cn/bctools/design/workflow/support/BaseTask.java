package cn.bctools.design.workflow.support;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 启动/执行流程任务通用入参
 */
@Data
@Accessors(chain = true)
public class BaseTask {

    /**
     * 用户信息
     */
    private UserDto user;

    /**
     * 工作流任务信息
     */
    private FlowTask flowTask;

    /**
     * 工作流高级配置
     */
    private FlowExtendDto flowExtend;

    /**
     * 动态增加的节点
     */
    private Node addNode;


    /**
     * 工作流内容
     */
    private JSONObject data;

    /**
     * 当前节点的数据版本
     */
    private String dataVersion;
}
