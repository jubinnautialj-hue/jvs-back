package org.jim.server.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: ZhuXiaoKang
 * @Description: 通道标识工具类
 */
public class ChannelMarkUtil {

    /**
     * 构建通道标识
     *
     * @param tenantId 租户id
     * @param id 标识值（可以是用户id、群组id等可以唯一标识通道的id值）
     * @return 通道标识
     */
    public static String buildMark(String tenantId, String id) {
        StringBuilder markBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(tenantId)) {
            markBuilder.append(tenantId).append("_");
        }
        markBuilder.append(id);
        return markBuilder.toString();
    }
}
