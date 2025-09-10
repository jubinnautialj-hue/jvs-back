package cn.bctools.design.taskNotice.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Accessors(chain = true)
public class FlowNoticeRequestDto {
    private String bizTaskId;//;// 1553038444628619299,
    private String workNum;// B2888220700114,
    private String flowId;// 1522872123143737345,
    private String flowName;// 请假申请,
    private String bizInstanceId;// 1553038444628619265,
    private String bizNodeId;// 1553038444628619266,
    private String currentNode;// 审批,
    private String title;// 张三请假申请--提交申请, taskType;// 0,
    private int taskType;// 0:待办,1:待阅
    private String businessMode;// HR,
    private String handler;// wuzong,
    private String handlerName;// 吴总,
    private String applicantName;// 张三,
    private String 	preHandlerName;// 李副总,
    private String formUrl;// http;////www.baidu.com,
    private long 	createDate;// 1650606662038,
    private int priority;// 0,
    private String buttonStatus;// 0,
    private String extraJson;//{\age\;//\14\,\name\;//\张三\}
}
