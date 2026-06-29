package org.jim.server.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: ZhuXiaoKang
 * @Description: 唯一标识工具类
*/
public class MarkUtils {

    private static final String GROUP_SPLIT = "_";

    public static String parseMark(String mark) {
        if (StringUtils.isBlank(mark)) {
            return null;
        }
        if (mark.contains(GROUP_SPLIT)) {
           return mark.split(GROUP_SPLIT)[1];
        }
        return mark;
    }
}
