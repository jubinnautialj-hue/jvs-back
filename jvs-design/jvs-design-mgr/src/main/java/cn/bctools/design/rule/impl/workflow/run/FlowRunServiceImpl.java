package cn.bctools.design.rule.impl.workflow.run;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.data.fields.enums.DataEventType;
import cn.bctools.design.data.service.DataLogService;
import cn.bctools.design.data.util.DataModelUtil;
import cn.bctools.design.workflow.dto.StartFlowTaskDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowResDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowVariables;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.TaskService;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * @author wl
 */
@Rule(value = "启动流程",
        group = RuleGroup.服务插件,
        returnType = ClassType.对象,
        statsMsg = "当前应用未找到流程，请先创建一个流程",
        order = 2,
//        iconUrl = "rule-jituanOAliuchengxinzengxiudingshangxianshenpiliucheng",
        explain = "选择的 oa 流程，根据传递业务参数创建一个工作流实例，并返回此流程实例的taskId(任务id)"
)
@AllArgsConstructor
public class FlowRunServiceImpl implements BaseCustomFunctionInterface<FlowRunDto> {

    private static final String MODEL_ID = "modelId";
    private static final String DATA_MODEL_ID = "dataModelId";
    private static final String ID = "id";
    private static final String NULL = "null";

    TaskService taskService;
    DataLogService dataLogService;

    @Override
    public Object execute(FlowRunDto flowRunDto, Map<String, Object> map) {
        if (ObjectNull.isNull(flowRunDto.getData())) {
            throw new BusinessException("未配置流程参数值");
        }
        if (ObjectNull.isNotNull(flowRunDto.getUserId()) && JSONUtil.isTypeJSON(flowRunDto.getUserId())) {
            throw new BusinessException("指定流程发起人必须是单个用户id");
        }
        if (!flowRunDto.getData().containsKey(ID)) {
            flowRunDto.getData().put(ID, map.get(ID));
        }
        if (!flowRunDto.getData().containsKey(MODEL_ID)) {
            flowRunDto.getData().put(MODEL_ID, map.get(MODEL_ID));
        }

        //启动流程, 只需要流程ID, 或流程指定参数即可
        StartFlowTaskDto copy = new StartFlowTaskDto().setData(flowRunDto.getData());
        copy.setId(flowRunDto.getWorkflow());
        StartFlowVariables startFlowDto = BeanCopyUtil.copy(copy, StartFlowVariables.class);
        if (MapUtils.isNotEmpty(copy.getData())) {
            startFlowDto.setData(JSONObject.parseObject(JSON.toJSONString(copy.getData())));
            if (flowRunDto.getData().containsKey(MODEL_ID)) {
                startFlowDto.setModelId(String.valueOf(flowRunDto.getData().get(MODEL_ID)));
            }
            if (flowRunDto.getData().containsKey(DATA_MODEL_ID)) {
                startFlowDto.setModelId(String.valueOf(flowRunDto.getData().get(DATA_MODEL_ID)));
            }
            // 将数据版本号设置到上下文
            String dataVersion = IdGenerator.getIdStr();
            DataModelUtil.setCurrentDataVersion(dataVersion);
            startFlowDto.setDataVersion(dataVersion);
            if (copy.getData().containsKey(ID)) {
                String dataId = String.valueOf(copy.getData().get(ID));
                //处理兼容问题
                copy.getData().put("dataId", dataId);
                if (!NULL.equals(dataId)) {
                    startFlowDto.setDataId(dataId);
                    // 不是保存数据前置后置触发逻辑调用的工作流，要保存数据版本
                    if (Boolean.FALSE.equals(DataModelUtil.whetherCurrentSaveData())) {
                        dataLogService.saveLog(startFlowDto.getModelId(), startFlowDto.getDataId(), copy.getData(), null, DataEventType.DATA_UPDATE);
                    }
                }
            }
        }
        //设置审核人
        startFlowDto.setApprovers(flowRunDto.getApprovers());
        if (ObjectNull.isNotNull(flowRunDto.getNode())) {
            String s = JSONObject.toJSONString(flowRunDto.getNode());
            Node node = JSONObject.parseObject(s, Node.class);
            startFlowDto.setNode(node);
        }

        // 设置发起人表单
        startFlowDto.setSendFormId(flowRunDto.getSendFormId());

        // 启动工作流
        boolean startFlow = Boolean.FALSE.equals(taskService.havePendingTask(startFlowDto.getDataId()));
        StartFlowResDto resDto;
        if (startFlow) {
            UserDto userDto;
            if (ObjectNull.isNull(flowRunDto.getUserId())) {
                userDto = UserCurrentUtils.getCurrentUser();
            } else {
                userDto = AuthorityManagementUtils.getUserById(flowRunDto.userId);
            }
            if (ObjectNull.isNull(userDto)) {
                throw new BusinessException("未登录或未指定发起人不能启动流程");
            }
            FlowContext flowContext = FlowContextUtil.context().getContext();
            resDto = taskService.start(userDto, startFlowDto);
            FlowContextUtil.reset();
            FlowContextUtil.context().refresh(flowContext);
            // 将处理后的数据返回到params
            if (ObjectNull.isNotNull(flowRunDto.getData())) {
                flowRunDto.getData().putAll(resDto.getData());
            } else {
                flowRunDto.setData(resDto.getData());
            }
            return resDto.getFlowTaskId();
        }

        throw new BusinessException("任务未结束不能启动新任务");
    }
}
