package org.jim.core.packets.message;

import org.jim.core.packets.Message;

/**
 * @author WChao
*/
public class MessageReqBody extends Message {

	private static final long serialVersionUID = -4748178964168947701L;

	/**
	 * 发送用户id;
	 */
	private String fromUserId;
	/**
	 * 接收用户id;
	 */
	private String userId;
	/**
	 * 群组id;
	 */
	private String groupId;
	/**
	 * 消息开始时间;
	 */
	private Long beginTime;
	/**
	 * 消息结束时间
	 */
	private Long endTime;
	/**
	 * 分页偏移量
	 */
	private Long offset;
	/**
	 * 数量
	 */
	private Long count;

	/**
	 * 消息业务类型[MessageReqTypeEnum]：
	 */
	private Integer messageType;

	/**
	 * 业务消息id
	 */
	private String businessId;
	
	public String getFromUserId() {
		return fromUserId;
	}
	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Long beginTime) {
		this.beginTime = beginTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Long getOffset() {
		return offset;
	}
	public void setOffset(Long offset) {
		this.offset = offset;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}

	public Integer getMessageType() {
		return messageType;
	}

	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}
}
