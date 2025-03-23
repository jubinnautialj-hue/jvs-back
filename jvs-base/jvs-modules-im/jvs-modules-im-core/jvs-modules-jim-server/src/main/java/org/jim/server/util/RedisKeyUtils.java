package org.jim.server.util;

import com.sun.org.apache.bcel.internal.generic.PUSH;
import org.apache.commons.lang3.StringUtils;
import org.jim.core.ImConst;

/**
 * @Author: ZhuXiaoKang
 * @Description: 统一管理redis key工具
 */
public class RedisKeyUtils {

    private RedisKeyUtils() {
    }

    /**
     * 组相关缓存 key
     */
    public static class GroupCache {

        private GroupCache() {
        }

        /**
         * 组基本信息key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param groupId 组id
         * @return 组基本信息key
         */
        public static String groupInfoKey(boolean prefix, String tenantId, String groupId) {
            // tenantId + SUFFIX + groupId + SUFFIX + INFO
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_GROUP + ImConst.SUFFIX : "")  + tenantId + ImConst.SUFFIX + groupId + ImConst.SUFFIX + ImConst.INFO;
        }

        /**
         * 组内用户id列表key
         * @param prefix 是否加前缀 true-是，false否
         * @param tempLinkId 临时链接id
         * @param tenantId 租户id
         * @param groupId 组id
         * @return 组内用户id列表key
         */
        public static String groupUserKey(boolean prefix, String tempLinkId, String tenantId, String groupId) {
            // tenantId + SUFFIX + groupId + SUFFIX + USER + SUFFIX + temp
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_GROUP + ImConst.SUFFIX : "")  + tenantId + ImConst.SUFFIX + groupId + ImConst.SUFFIX + ImConst.USER + (StringUtils.isNotBlank(tempLinkId) ? ImConst.SUFFIX + tempLinkId : "");
        }

        /**
         * 组内用户信息map集合key
         * @param prefix 是否加前缀 true-是，false否
         * @param tempLinkId 临时链接id
         * @param tenantId 租户id
         * @param groupId 组id
         * @return 用户组信息map集合key
         */
        public static String groupUserInfoKey(boolean prefix,  String tempLinkId, String tenantId, String groupId) {
            // tenantId + SUFFIX + groupId + SUFFIX + USER + SUFFIX + INFO
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_GROUP + ImConst.SUFFIX : "")  + tenantId + ImConst.SUFFIX + groupId + ImConst.SUFFIX + ImConst.USER + ImConst.SUFFIX + ImConst.INFO + (StringUtils.isNotBlank(tempLinkId) ? ImConst.SUFFIX + tempLinkId : "");
        }
    }

    /**
     * 用户相关缓存 key
     */
    public static class UserCache {
        private UserCache() {
        }

        /**
         * 用户信息key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param userId 用户id
         * @return 用户信息key
         */
        public static String userInfoKey(boolean prefix, String tenantId, String userId) {
            // tenantId + SUFFIX + userId + SUFFIX + INFO
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_USER + ImConst.SUFFIX : "")  +tenantId + ImConst.SUFFIX + userId + ImConst.SUFFIX + ImConst.INFO;
        }

        /**
         * 用户所有组id列表key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param userId 用户id
         * @return 用户所有组id列表key
         */
        public static String userGroupKey(boolean prefix, String tenantId, String userId) {
            // tenantId + SUFFIX + userId + SUFFIX + GROUP
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_USER + ImConst.SUFFIX : "")  + tenantId + ImConst.SUFFIX + userId + ImConst.SUFFIX + ImConst.GROUP;
        }

        /**
         * 用户终端信息key
         * @param prefix 是否加前缀 true-是，false否
         * @param suffix 后缀
         * @param tenantId 租户id
         * @param userId 用户id
         * @return 用户终端信息key
         */
        public static String userTerminalKey(boolean prefix, String suffix, String tenantId, String userId) {
            // tenantId + SUFFIX + userId + SUFFIX + TERMINAL
            // 具体终端key：tenantId + SUFFIX + userId + SUFFIX + TERMINAL + SUFFIX + terminal
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_USER + ImConst.SUFFIX : "")  + tenantId + ImConst.SUFFIX + userId + ImConst.SUFFIX + ImConst.TERMINAL +  (StringUtils.isNotBlank(suffix) ?  ImConst.SUFFIX + suffix : "");
        }

        /**
         * 用户好友列表key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param userId 用户id
         * @return 用户好友列表key
         */
        public static String userFriendKey(boolean prefix, String tenantId, String userId) {
            // tenantId + SUFFIX + userId + SUFFIX + FRIENDS
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_USER + ImConst.SUFFIX : "")  +  tenantId + ImConst.SUFFIX + userId + ImConst.SUFFIX + ImConst.FRIENDS;
        }


    }

    /**
     * 推送缓存相关key
     */
    public static class PushCache {

        private PushCache() {
        }

        /**
         * 组内成员离线消息key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param groupId 组id
         * @param userId 用户id
         * @return 组内成员离线消息key
         */
        public static String groupUserOfflineKey(boolean prefix, String tenantId, String groupId, String userId) {
            // GROUP + SUFFIX + tenantId + SUFFIX + groupId + SUFFIX + userId
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_PUSH + ImConst.SUFFIX : "")  +  ImConst.GROUP + ImConst.SUFFIX + tenantId + ImConst.SUFFIX + groupId + ImConst.SUFFIX + userId;
        }

        /**
         * 好友离线消息key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param userId 接收消息的用户id
         * @param fromUserId 发送消息的用户id
         * @return 好友离线消息key
         */
        public static String friendUserOfflineKey(boolean prefix, String tenantId, String userId, String fromUserId) {
            // USER + SUFFIX + tenantId + SUFFIX + userId + SUFFIX + fromUserId
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_PUSH + ImConst.SUFFIX : "")  +  ImConst.USER + ImConst.SUFFIX + tenantId + ImConst.SUFFIX + userId +  (StringUtils.isNotBlank(fromUserId) ? ImConst.SUFFIX + fromUserId : "");
        }

        /**
         * 通知离线消息key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param userId 接收消息的用户id
         * @return 通知离线消息key
         */
        public static String notifyUserOfflineKey(boolean prefix, String tenantId, String userId) {
            //  NOTIFY + SUFFIX + tenantId + SUFFIX + userId
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_PUSH + ImConst.SUFFIX : "")  + ImConst.NOTIFY + ImConst.SUFFIX + tenantId + ImConst.SUFFIX + userId;
        }

    }


    /**
     * 自定义消息缓存
     */
    public static class BusinessCache {
        private BusinessCache() {
        }

        /**
         * 自定义消息缓存key
         * @param prefix 是否加前缀 true-是，false否
         * @param tenantId 租户id
         * @param businessId 业务id
         * @return 自定义消息缓存key
         */
        public static String businessKey(boolean prefix, String tenantId, String businessId) {
            //  租户id_自定义消息id
            return  (Boolean.TRUE.equals(prefix) ? ImConst.PREFIX_BUSINESS + ImConst.SUFFIX : "")  + ChannelMarkUtil.buildMark(tenantId, businessId);
        }

    }




}
