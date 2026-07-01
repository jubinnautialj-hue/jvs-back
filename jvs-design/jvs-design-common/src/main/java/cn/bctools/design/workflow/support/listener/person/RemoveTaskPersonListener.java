package cn.bctools.design.workflow.support.listener.person;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.taskNotice.entity.FlowTaskNotice;
import cn.bctools.design.taskNotice.service.FlowTaskNoticeService;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.service.FlowTaskPersonService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 移除审批人监听
 */
@Slf4j
@Component
@AllArgsConstructor
public class RemoveTaskPersonListener {

    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskNoticeService flowTaskNoticeService;

    /**
     * 监听移除审批人事件
     *
     * @param event 事件对象
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskEndEvent(RemoveTaskPersonEvent event) {
        if (ObjectNull.isNull(event.getTaskPersonIds())) {
            return;
        }
        List<String> taskPersonIdAndUserIds = event.getTaskPersonIds();
        List<String> taskPersonIds =new ArrayList<>();
        log.info("代码移除审批人:{}",taskPersonIdAndUserIds);
        for (String idAndUserId : taskPersonIdAndUserIds) {
            if(idAndUserId != null && !"".equals(idAndUserId)){
                String[] split = idAndUserId.split(",");
                String personId = split[0];
                String userId = split[1];
                taskPersonIds.add(personId);
                List<String> removeBizTaskIds= flowTaskNoticeService.list(Wrappers.<FlowTaskNotice>lambdaQuery()
                        .like(FlowTaskNotice::getTaskId, personId)
                        .eq(FlowTaskNotice::getUserId,userId)
                        .eq(FlowTaskNotice::getStatus, 0)).stream().map(FlowTaskNotice::getBizTaskId).collect(Collectors.toList());
                //2025.09.10 关闭已完成的待办提醒通知
                if(removeBizTaskIds != null && removeBizTaskIds.size() > 0){
                    if(removeBizTaskIds.size() == 1){
                        flowTaskNoticeService.close(event.getFlowTask(),removeBizTaskIds);
                    }else {
                        log.info("消息结果大于一02:{}",removeBizTaskIds.toString());
                        List<String> newList = new ArrayList<String>();
                        newList.add(removeBizTaskIds.get(0));
                        flowTaskNoticeService.close(event.getFlowTask(),newList);
                    }
                }
            }
        }
        flowTaskPersonService.removeByIds(taskPersonIds);
    }

}
