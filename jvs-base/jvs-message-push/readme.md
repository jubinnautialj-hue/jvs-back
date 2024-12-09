## 消息管理和发送

> 包含邮件、短信、公众号、钉钉、站内信等消息渠道。使用的发送消息配置为租户级的相关配置进行发送，消息消费可以通过 api或 mq进行触发。

- [AliSmsApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FAliSmsApi.java)  短信消息
- [DingTalkCorpApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FDingTalkCorpApi.java)  钉钉应用消息
- [DingTalkRobotApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FDingTalkRobotApi.java) 钉钉机器人消息
- [EmailMessagePushApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FEmailMessagePushApi.java)  邮件消息
- [InsideNotificationApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FInsideNotificationApi.java)  站内信消息
- [MessagePushHisApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FMessagePushHisApi.java)   发送历史消息
- [MessagePushUtilsApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FMessagePushUtilsApi.java) 批量发送消息 需要自行设置接收人信息
- [WechatOfficialAccountApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FWechatOfficialAccountApi.java)  微信公众号消息
- [WechatWorkAgentApi.java](message-push-api%2Fsrc%2Fmain%2Fjava%2Fcn%2Fbctools%2Fmessage%2Fpush%2Fapi%2FWechatWorkAgentApi.java)   企业微信消息
