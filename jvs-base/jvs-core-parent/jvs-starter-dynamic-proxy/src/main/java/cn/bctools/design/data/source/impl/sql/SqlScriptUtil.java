package cn.bctools.design.data.source.impl.sql;

import com.baomidou.mybatisplus.core.toolkit.StringPool;

/**
 * @Author: ZhuXiaoKang
 * @Description: sql 脚本工具类
 *
 * <p> 构造sql片段
 */
public final class SqlScriptUtil {

    private SqlScriptUtil() {
    }

    /**
     * 逗号 + 空格
     */
    private static final String COMMA = StringPool.COMMA + StringPool.SPACE;

    /**
     * `字段`
     *
     * @param column 字段
     * @return 脚本
     */
    public static String backtick(String column) {
        return StringPool.BACKTICK + column + StringPool.BACKTICK;
    }

    /**
     * 'sql片段'
     * @param segment
     * @return
     */
    public static String singleQuote(String segment) {
        return StringPool.SINGLE_QUOTE + segment + StringPool.SINGLE_QUOTE;
    }

    /**
     * (sql片段)
     *
     * @return 脚本
     */
    public static String bracket(String segment) {
        return StringPool.LEFT_BRACKET  + segment  + StringPool.RIGHT_BRACKET;
    }

    /**
     * 逗号 + 空格
     *
     * @return 脚本
     */
    public static String comma() {
        return COMMA;
    }

    /**
     * sql拼接
     *
     * @param segments
     * @return
     */
    public static String appendSqlSegment(Object... segments) {
        StringBuilder newSegment = new StringBuilder(StringPool.SPACE);
        for (Object segment : segments) {
            newSegment.append(segment).append(StringPool.SPACE);
        }
        return newSegment.toString();
    }
}
