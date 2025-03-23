package org.jim.core.packets;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 版本: [1.0]
 * 功能说明: 
 * @author : WChao 创建时间: 2017年7月26日 下午3:13:47
 */
@Data
public class User extends Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id;
     */
    private String userId;
    /**
     * 用户姓名
     */
    private String realName;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 在线状态(online、offline)
     */
    private String status = UserStatusType.OFFLINE.getStatus();
    /**
     * 个性签名;
     */
    private String sign;
    /**
     * 用户所属终端;(ws、tcp、http、android、ios等)
     */
    private String terminal;
    /**
     * 好友列表;
     */
    private List<Group> friends;
    /**
     * 群组列表;
     */
    private List<Group> groups;

    private User() {
    }

    private User(String userId, String nick, String realName, String avatar, String status, String sign, String terminal, List<Group> friends, List<Group> groups, JSONObject extras) {
        this.userId = userId;
        this.nick = nick;
        this.realName = realName;
        this.avatar = avatar;
        this.status = status;
        this.sign = sign;
        this.terminal = terminal;
        this.friends = friends;
        this.groups = groups;
        this.extras = extras;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public List<Group> getFriends() {
        return friends;
    }

    public void setFriends(List<Group> friends) {
        this.friends = friends;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public static class Builder extends Message.Builder<User, Builder> {
        /**
         * 用户id;
         */
        private String userId;
        /**
         * 用户姓名
         */
        private String realName;
        /**
         * 昵称
         */
        private String nick;
        /**
         * 用户头像
         */
        private String avatar;
        /**
         * 在线状态(online、offline)
         */
        private String status = UserStatusType.OFFLINE.getStatus();
        /**
         * 个性签名;
         */
        private String sign;
        /**
         * 用户所属终端;(ws、tcp、http、android、ios等)
         */
        private String terminal;
        /**
         * 好友列表;
         */
        private List<Group> friends;
        /**
         * 群组列表;
         */
        private List<Group> groups;

        public Builder() {
        }

        ;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder realName(String realName) {
            this.realName = realName;
            return this;
        }

        public Builder nick(String nick) {
            this.nick = nick;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder sign(String sign) {
            this.sign = sign;
            return this;
        }

        public Builder terminal(String terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder addFriend(Group friend) {
            if (CollectionUtils.isEmpty(friends)) {
                friends = Lists.newArrayList();
            }
            friends.add(friend);
            return this;
        }

        public Builder addGroup(Group group) {
            if (CollectionUtils.isEmpty(groups)) {
                groups = Lists.newArrayList();
            }
            groups.add(group);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public User build() {
            return new User(userId, nick, realName, avatar, status, sign, terminal, friends, groups, extras);
        }

    }

    @Override
    public User clone() {
        User cloneUser = new User();
        BeanUtil.copyProperties(this, cloneUser, "friends", "groups");
        return cloneUser;
    }

}
