package org.jim.core.packets;

import org.jim.core.packets.notify.NotifyReqBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WChao
 * @Description 离线/历史消息响应
*/
public class UserMessageData extends PageData implements Serializable{

	private static final long serialVersionUID = -1367597924020299919L;

	/**
	 * 查询消息类型[MessageReqTypeEnum]
	 */
	private Integer type;
	/**
	 * 用户ID
	 */
	private String userId;
	/**
	 * 好友聊天消息
	 */
	private Map<String,List<ChatBody>> friends = new HashMap<String, List<ChatBody>>();
	/**
	 * 群组聊天消息
	 */
	private Map<String,List<ChatBody>> groups = new HashMap<String, List<ChatBody>>();

	/**
	 * 通知消息
	 */
	private List<NotifyReqBody> notifys = new ArrayList<>();

	/**
	 * 业务消息
	 */
	private BusinessBody business;
	
	public UserMessageData(){}

	public UserMessageData(String userId){
		this.userId = userId;
	}
	public Map<String, List<ChatBody>> getFriends() {
		return friends;
	}
	public void setFriends(Map<String, List<ChatBody>> friends) {
		this.friends = friends;
	}
	public Map<String, List<ChatBody>> getGroups() {
		return groups;
	}
	public void setGroups(Map<String, List<ChatBody>> groups) {
		this.groups = groups;
	}

	public List<NotifyReqBody> getNotifys() {
		return notifys;
	}

	public void setNotifys(List<NotifyReqBody> notifys) {
		this.notifys = notifys;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public BusinessBody getBusiness() {
		return business;
	}

	public void setBusiness(BusinessBody business) {
		this.business = business;
	}
}
