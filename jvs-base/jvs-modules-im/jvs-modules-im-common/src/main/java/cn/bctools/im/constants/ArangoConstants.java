package cn.bctools.im.constants;

/**
 * @Author: ZhuXiaoKang
 * @Description: Arangodb常量
 */
public class ArangoConstants {

    private ArangoConstants() {
    }

    /**
     * Arangodb用户集合名称
     */
    public static final String COLLECTIONS_USER = "user";

    /**
     * Arangodb好友关系名称
     */
    public static final String COLLECTIONS_FRIENDS = "friends";

    /**
     * Arangodb分隔符
     */
    public static final String COLLECTIONS_SPLIT = "/";

    /**
     * _key字段分隔符
     */
    public static final String KEY_SPLIT = "-";

    /**
     * 构造Arangodb中的用户文档_key
     *
     * 格式：tenantId-userId
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    public static String buildUserKey(String tenantId, String userId) {
        return new StringBuilder()
                .append(tenantId)
                .append(KEY_SPLIT)
                .append(userId)
                .toString();
    }

    /**
     * 构造Arangodb中的用户文档_id
     *
     * @param tenantId 租户id
     * @param userId 用户id
     * @return
     */
    public static String buildUserId(String tenantId, String userId) {
        return new StringBuilder()
                .append(COLLECTIONS_USER)
                .append(COLLECTIONS_SPLIT)
                .append(buildUserKey(tenantId, userId))
                .toString();
    }
}
