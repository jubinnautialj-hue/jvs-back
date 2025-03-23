package org.jim.core.packets;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 版本: [1.0]
 * 功能说明: 用户群组
 * 作者: WChao 创建时间: 2017年9月21日 下午1:54:04
 */
public class Group extends Message {

    private static final long serialVersionUID = -3817755433171220952L;

    /**
     * GroupTypeEnum 组类型 (0:普通组、1: 文档消息组)
     */
    private Integer groupType;

    /**
     * 群组ID
     */
    private String groupId;
    /**
     * 群组名称
     */
    private String name;
    /**
     * 群组头像
     */
    private String avatar;
    /**
     * 在线人数
     */
    private Integer online;
    /**
     * 组用户
     */
    private List<User> users;


    private Group() {
    }

    private Group(String groupId, String name, String avatar, Integer online, List<User> users, JSONObject extras, Integer groupType) {
        this.groupId = groupId;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
        this.users = users;
        this.extras = extras;
        this.groupType = groupType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Integer getGroupType() {
        return groupType;
    }

    public void setGroupType(Integer groupType) {
        this.groupType = groupType;
    }

    public static class Builder extends Message.Builder<Group, Builder> {
        /**
         * 组类型 (0:普通组、1: 文档消息组)
         */
        private Integer groupType = 0;
        /**
         * 群组ID
         */
        private String groupId;
        /**
         * 群组名称
         */
        private String name;
        /**
         * 群组头像
         */
        private String avatar;
        /**
         * 在线人数
         */
        private Integer online;
        /**
         * 组用户
         */
        private List<User> users = null;


        public Builder() {
        }

        ;
        public Builder groupType(Integer groupType) {
            this.groupType = groupType;
            return this;
        }
        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder setChatType(Integer online) {
            this.online = online;
            return this;
        }

        public Builder addUser(User user) {
            if (CollectionUtils.isEmpty(users)) {
                users = Lists.newArrayList();
            }
            users.add(user);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public Group build() {
            return new Group(this.groupId, this.name, this.avatar, this.online, this.users, this.extras, this.groupType);
        }
    }

    @Override
    public Group clone() {
        Group group = Group.newBuilder().build();
        BeanUtil.copyProperties(this, group, "users");
        return group;
    }

}
