package cn.bctools.design.data.source.impl.sql.enums;

import cn.bctools.design.data.source.impl.sql.methods.impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enum Dynamic sql method.
 *
 * @Author: ZhuXiaoKang
 * @Description: SQL方法
 */
@Getter
@AllArgsConstructor
public enum DynamicSqlMethod {
    /**
     * The Insert one.
     */
    INSERT_ONE(InsertOne.class, "插入一条数据（选择字段插入）", "\nINSERT INTO %s %s VALUES %s\n"),
    /**
     * The Delete.
     */
    DELETE(Delete.class, "根据条件删除记录", "\nDELETE FROM %s %s\n"),
    /**
     * The Update.
     */
    UPDATE(Update.class, "根据条件更新记录", "\nUPDATE %s SET %s %s\n"),
    /**
     * The Select list.
     */
    SELECT_LIST(SelectList.class,"查询满足条件所有数据", "\nSELECT %s FROM %s %s\n"),
    /**
     * The Select count.
     */
    SELECT_COUNT(SelectCount.class, "查询满足条件总记录数", "\nSELECT COUNT(%s) FROM %s %s\n"),
    ;
    private final Class<?> method;
    private final String desc;
    private final String sql;


}
