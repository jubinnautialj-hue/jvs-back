package cn.bctools.common.constant;

import cn.bctools.common.utils.SpringContextUtil;

/**
 * 系统常量
 * <p>
 * 包含请求头名称, RedisKey等
 *
 * @author Administrator
 */
public class SysConstant {

    public static final String MGR = "-mgr";
    public static final String BIZ = "-biz";
    public static final String MGR_PATH = "/mgr/";
    public static final String AUTH_PATH = "/auth/";


    /**
     * 版本号
     */
    public static final String JVS = "jvs_session_uid";
    public static final String JVS_AUTH = "jvsauth:";
    public static final String VERSION = "jvs-rule-ua";
    public static final String TENANTID = "jvs-tenantId";
    public static final String DEFAULT = "default";
    public static final String USERID = "userId";
    public static final String DATASCOPE = "datascope:applicationname:";
    /**
     * 应用名称, 详情见{@link cn.bctools.common.utils.SpringContextUtil#applicationContextName}
     */
    public static final String APPLICATION_NAME = "_application_name_";

    /**
     * MySQL写锁关键词
     */
    public static final String FOR_UPDATE = "for update";
    /**
     * 存储redis的key, 每个项目都一样
     */
    public static final String SYSTEM_STORAGE_INFO = "jvs:System_Storage_info:";


    /**
     * 统一规范化Redis的Key命名，保证所有的Key都可以便于管理
     *
     * @param module 功能模块简称
     * @param key    Redis的key
     * @return
     */
    public static synchronized String redisKey(String module, String key) {
        StringBuffer stringBuffer = new StringBuffer(SpringContextUtil.getApplicationContextName());
        stringBuffer.append(":");
        stringBuffer.append(module);
        stringBuffer.append(":");
        stringBuffer.append(key);
        return stringBuffer.toString().replaceAll("-", "");
    }

    /**
     * todo 缓存区,需要区分是否公共, 或者是否是租户级,如果是级租户缓存,则默认使用租户
     * 为了不同角色不同岗位去确定缓存区不同缓存区存在关联性,如租户级缓存更新后,所有用户都将会更新
     *
     * 缓存支持平台级缓存
     *
     * 缓存Key的数据值
     */

    /**
     * 与登陆相关的,用户登陆的时候,清空所有这个相关的缓存
     * 默认所有缓存都会自动添加多租户的cacheNames 例示  cache_tenant1
     * 如果需要跳过租户的配置项,则需使用平台缓存cacheNames CACHE_PLATFORM_PREFIX 前缀
     */
    public static final String CACHE = "cache:";
    public static final String CACHE_TENANT_DEFAULT_PREFIX = "cache_tenant";
    public static final String CACHE_PLATFORM_PREFIX = "cache_platform";
    public static final String CACHE_USER_PREFIX = "cache_user";


    public static final String CACHE_LOGIN = "cache_login";
    public static final String CACHE_DICT = "cache_dict";
    public static final String CACHE_CLIENT = "cache_client";
    public static final String CACHE_SYS_CONFIG = "CACHE_SYS_CONFIG";
    public static final String CACHE_ROLE = "cache_role";
    public static final String CACHE_PERMISSION = "cache_permission";


    /**
     * OPEN API 标记
     * swagger扩展配置中，指定该标记，表示为OPEN API
     */
    public static final String OPEN_API_MARK = "openApi";

}
