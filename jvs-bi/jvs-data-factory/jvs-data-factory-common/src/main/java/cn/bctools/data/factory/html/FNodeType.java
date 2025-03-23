package cn.bctools.data.factory.html;

import cn.bctools.data.factory.html.node.*;
import cn.bctools.data.factory.html.run.Frun;

/**
 * @author guojing
 */

public enum FNodeType {

    /**
     * 输入、输出、过滤、行转列、字段设置
     */
    Input(InputNode.class),
    /**
     * 过滤
     */
    dataFilter(FilterNode.class),
    /**
     * 字段排序
     */
    sortNode(FieldSortNode.class),
    /**
     * 输出
     */
    export(OutPutNode.class),
    /**
     * 分组
     */
    groupBy(GroupNode.class),
    /**
     * 排名
     */
    rank(RankNode.class),
    /**
     * 行转列
     */
    rowToColumnTransformer(RowToColumnTransformer.class),
    /**
     * 列转行
     */
    columnToRowTransformer(ColumnToRowTransformer.class),
    /**
     * 数据清洗
     */
    dataClear(DataClearNode.class),
    /**
     * 数据追加合并
     */
    append(AppendNode.class),
    /**
     * 横向连接
     */
    crossBinding(CrossBindingNode.class),
//    /**
//     * sql执行
//     */
//    sqlExecSource(SqlExecSourceNode.class),
    /**
     * 条件分组
     */
    conditionGroupNode(ConditionGroupNode.class),
    /**
     * 结构解析 把map或者listmap里面的对象转换出来
     */
    dataExtractingNode(DataExtractingNode.class),
    /**
     * 数据去重 通过n个字段进行数据的去重
     */
    dataDeduplication(DataDeduplicationNode.class),
    /**
     * 函数执行
     */
    functionDispose(FunctionDisposeNode.class),
    /**
     * 字段设置
     */
    fieldSetup(FieldSetupNode.class),
    /**
     * 数据填充
     */
    dataFill(DataFillNode.class),
    ;

    Class<? extends Frun> cls;

    public Class<? extends Frun> getCls() {
        return cls;
    }

    public void setCls(Class<? extends Frun> cls) {
        this.cls = cls;
    }

    FNodeType(Class<? extends Frun> cls) {
        this.cls = cls;
    }

}
