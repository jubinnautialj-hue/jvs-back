package cn.bctools.document.constant;

import cn.bctools.common.utils.TenantContextHolder;
import cn.hutool.core.util.StrUtil;

/**
 * @author admin
 */
public class RedisKey {
    /**
     * 上传的文件同步到es中时文件进入锁定状态
     */
    private static final String KNOWLEDGE_FILE_TO_ES_LOCK = "knowledge:file_to_es_lock_{}_{}";

    /**
     * 设计阶段 防止用户在上次请求没有完成的情况再次点击  造成数据混乱
     *
     * @param id 设计id
     * @return key
     */
    public static String getKnowledgeFileToEsLockKey(String id) {
        String tenantId = TenantContextHolder.getTenantId();
        return StrUtil.format(KNOWLEDGE_FILE_TO_ES_LOCK, id, tenantId);
    }

}
