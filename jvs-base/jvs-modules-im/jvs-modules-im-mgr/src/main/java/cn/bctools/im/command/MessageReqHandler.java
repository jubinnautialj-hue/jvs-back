package cn.bctools.im.command;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.im.service.SearchMessageService;
import cn.bctools.im.service.SearchMessageStrategy;
import cn.bctools.im.utils.ChannelTenantUtil;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImChannelContext;
import org.jim.core.ImPacket;
import org.jim.core.ImStatus;
import org.jim.core.config.ImConfig;
import org.jim.core.exception.ImException;
import org.jim.core.packets.Command;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.UserMessageData;
import org.jim.core.packets.message.MessageReqBody;
import org.jim.core.packets.message.MessageReqTypeEnum;
import org.jim.core.utils.JsonKit;
import org.jim.server.command.AbstractCmdHandler;
import org.jim.server.config.ImServerConfig;
import org.jim.server.protocol.ProtocolManager;

/**
 * 获取聊天消息命令处理器
 * @author WChao
 */
public class MessageReqHandler extends AbstractCmdHandler {

	private SearchMessageStrategy messageStrategy = SpringContextUtil.getBean(SearchMessageStrategy.class);
	
	@Override
	public Command command() {
		
		return Command.COMMAND_GET_MESSAGE_REQ;
	}

	@Override
	public ImPacket handler(ImPacket packet, ImChannelContext imChannelContext) throws ImException {
		MessageReqBody messageReqBody;
		try{
			messageReqBody = JsonKit.toBean(packet.getBody(),MessageReqBody.class);
			messageReqBody.setTenantId(ChannelTenantUtil.build(imChannelContext.getTenantId(), messageReqBody.getTenantId()));
			if (StringUtils.isBlank(messageReqBody.getTenantId())) {
				return ProtocolManager.Packet.dataInCorrect(Command.COMMAND_GET_MESSAGE_RESP, imChannelContext);
			}
		}catch (Exception e) {
			//用户消息格式不正确
			return getMessageFailedPacket(imChannelContext);
		}

		ImServerConfig imServerConfig = ImConfig.Global.get();
		// 参数校验
		if (!checkBody(messageReqBody, imServerConfig)) {
			return getMessageFailedPacket(imChannelContext);
		}

		UserMessageData messageData = null;
		// 执行查询
		SearchMessageService messageService = messageStrategy.getMessageService(messageReqBody.getMessageType());
		messageData = messageService.search(messageReqBody, imServerConfig.getMessageHelper());
		if (messageData == null) {
			return getMessageFailedPacket(imChannelContext);
		}

		messageData.setType(messageReqBody.getMessageType());
		RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP, messageData);
		return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
	}

	/**
	 * 入参校验
	 *
	 * @param messageReqBody 入参
	 * @param imServerConfig IM全局配置
	 * @return
	 */
	private boolean checkBody(MessageReqBody messageReqBody, ImServerConfig imServerConfig) {
		if(StringUtils.isEmpty(messageReqBody.getUserId())) {
			return Boolean.FALSE;
		}

		if (MessageReqTypeEnum.getByValue(messageReqBody.getMessageType()) == null) {
			return Boolean.FALSE;
		}
		if(!ImServerConfig.ON.equals(imServerConfig.getIsStore())){
			return Boolean.FALSE;
		}
		if (messageReqBody.getMessageType() == null) {
			return Boolean.FALSE;
		}
		if (MessageReqTypeEnum.getByValue(messageReqBody.getMessageType()) == null) {
			return Boolean.FALSE;
		}

		// 历史消息查询参数校验
		if (MessageReqTypeEnum.CHAT_HISTORY.getValue() == messageReqBody.getMessageType() ||
				MessageReqTypeEnum.NOTIFY_HISTORY.getValue() == messageReqBody.getMessageType()) {
			if (messageReqBody.getOffset() == null) {
				return Boolean.FALSE;
			}
			if (messageReqBody.getCount() == null) {
				return Boolean.FALSE;
			}
			if (messageReqBody.getBeginTime() == null) {
				return Boolean.FALSE;
			}
		}

		// 消息类型为“业务自定义消息”时，业务消息id必传
		if (MessageReqTypeEnum.BUSINESS.getValue() == messageReqBody.getMessageType()) {
			if (StringUtils.isBlank(messageReqBody.getBusinessId())) {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * 获取用户消息失败响应包;
	 * @param imChannelContext
	 * @return
	 */
	public ImPacket getMessageFailedPacket(ImChannelContext imChannelContext) throws ImException{
		RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP,ImStatus.C10015);
		return ProtocolManager.Converter.respPacket(resPacket, imChannelContext);
	}
}
