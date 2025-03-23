package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.enums.CrossBindingTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.dto.ConnectionField;
import cn.bctools.data.factory.html.node.params.CrossBindingParams;
import cn.bctools.data.factory.html.run.Frun;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交叉合并
 * 横向连接：
 * 横向连接主要是用于将两个节点数据进行合并操作
 * 合并：
 * 内连接：以左侧节点title为准，过滤右侧节点中符合关联条件的title，剩下的title都在末尾加上 title所属节点id
 * 左连接：过滤右侧节点中符合连接条件（rightKey）的title，剩下的title都在末尾加上 title所属节点id
 * 右连接：过滤左侧节点中符合连接条件（leftKey）的title，剩下的title都在末尾加上 title所属节点id
 * 不合并：
 * 内连接、左连接、右连接：为title加上 title所属节id的后缀
 * 处理数据时都是将 《数据所属节点id》作为后缀 添加到符合条件的数据对应的key末尾
 */
@Service
public class CrossBindingNode implements Frun<CrossBindingParams> {
    /**
     * 左表别名
     */
    private final static String LEFT_TABLE_ALIAS = "leftTable";
    /**
     * 右表别名
     */
    private final static String RIGHT_TABLE_ALIAS = "rightTable";

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, CrossBindingParams crossBindingParams) {
        CrossBindingParams sourceData = crossBindingParams.getSourceData();
        //创建表 合并字段并合并连接字段
        FData leftData = linkBody.get(sourceData.getCrossBindingObj().getLeftNodeId());
        FData rightData = linkBody.get(sourceData.getCrossBindingObj().getRightNodeId());
        List<DataSourceField> leftTitle = leftData.getTitle();
        List<DataSourceField> rightTitle = rightData.getTitle();
        //根据不同的连接方式删除不同来源的字段
        switch (sourceData.getCrossBindingObj().getConnection()) {
            case join:
            case union:
            case right:
                //内连接与右连接都删除左表连接字段
                List<String> leftKey = sourceData.getCrossBindingObj().getConnectionFields().stream().map(ConnectionField::getLeftFieldKey).collect(Collectors.toList());
                leftTitle = leftTitle.stream().filter(e -> !leftKey.contains(e.getFieldKey())).collect(Collectors.toList());
                break;
            case left:
                //左连接删除右表连接字段
                List<String> rightKey = sourceData.getCrossBindingObj().getConnectionFields().stream().map(ConnectionField::getRightFieldKey).collect(Collectors.toList());
                rightTitle = rightTitle.stream().filter(e -> !rightKey.contains(e.getFieldKey())).collect(Collectors.toList());
                break;
            default:
                throw new BusinessException("未知的连接方式");
        }
        //字段合并
        List<DataSourceField> newTitle = new ArrayList<>(leftTitle);
        List<String> rightKey = rightTitle.stream().map(DataSourceField::getFieldKey).collect(Collectors.toList());
        //需要判断是否存在重复如果存在需要重新命名 重新copy一份 防止原始数据被修改
        List<DataSourceField> rightTitleNew = rightTitle.stream().map(e -> {
            DataSourceField dataSourceField = JSONObject.parseObject(JSONObject.toJSONString(e), DataSourceField.class);
            long count = newTitle.stream().filter(v -> v.getFieldKey().contains(dataSourceField.getFieldKey())).count();
            if (count > 0) {
                //防止 存在相同的输入源  使用两条不同的执行树  最后使用横向连接 进行合并 就会存在key重复
                long rightCount = rightKey.stream().filter(v -> v.contains(dataSourceField.getFieldKey()) || dataSourceField.getFieldKey().contains(v)).count();
                count += rightCount;
                dataSourceField.setFieldKey(e.getFieldKey() + (count + 1));
            }
            return dataSourceField;
        }).collect(Collectors.toList());
        //为了把主表字段放到前面 需要判断一下
        if (sourceData.getCrossBindingObj().getConnection().equals(CrossBindingTypeEnum.right)) {
            newTitle.addAll(0, rightTitleNew);
        } else {
            newTitle.addAll(rightTitleNew);
        }
        //创建表
        String documentName = crossBindingParams.getTableName();
        this.createTable(documentName, newTitle, null, Boolean.FALSE, new ArrayList<>());
        //同步数据
        StringBuffer sql = new StringBuffer();
        //判断是否为全连接  全连接与其他连接 sql 完全不一致  需要特殊处理
        if (sourceData.getCrossBindingObj().getConnection().equals(CrossBindingTypeEnum.union)) {
            this.unionCreateSql(leftTitle, rightTitle, newTitle, leftData.getDocumentName(), rightData.getDocumentName(), sql, documentName, sourceData);
        } else {
            this.joinCreateSql(leftTitle, rightTitle, newTitle, leftData.getDocumentName(), rightData.getDocumentName(), sql, documentName, sourceData);
        }
        this.save(sql.toString(), documentName, newTitle, Boolean.FALSE, null, Boolean.FALSE, new ArrayList<>());
        return new FData().setDocumentName(documentName).setTitle(newTitle);
    }

    /**
     * 构建连接sql
     *
     * @param targetFieldList    目标数据库的表结构
     * @param sql                sql
     * @param crossBindingParams 用户设计数据
     * @param leftFieldList      左表表结构
     * @param leftTableName      左表名称
     * @param rightFieldList     右表表结构
     * @param rightTableName     右表名称
     * @param targetTableName    目标表名称
     */
    public void joinCreateSql(List<DataSourceField> leftFieldList, List<DataSourceField> rightFieldList, List<DataSourceField> targetFieldList, String leftTableName, String rightTableName, StringBuffer sql, String targetTableName, CrossBindingParams crossBindingParams) {
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        sql.append("INSERT INTO ")
                .append(bean.getLibraryName())
                .append(".")
                .append(targetTableName)
                .append(" (")
                .append(targetColumn)
                .append(") ");
        //构建连接
        sql.append("SELECT ");
        //构建需要展示的表结构
        String leftColumn = leftFieldList.stream().map(DataSourceField::getFieldKey).map(e -> LEFT_TABLE_ALIAS + ".`" + e + "`").collect(Collectors.joining(","));
        String rightColumn = rightFieldList.stream().map(DataSourceField::getFieldKey).map(e -> RIGHT_TABLE_ALIAS + ".`" + e + "`").collect(Collectors.joining(","));
        //由于 主数据在前面 如果连接类型为 右连接 就需要把 右表的字段 放到前面
        if (crossBindingParams.getCrossBindingObj().getConnection().equals(CrossBindingTypeEnum.right)) {
            sql.append(rightColumn)
                    .append(",")
                    .append(leftColumn);
        } else {
            sql.append(leftColumn)
                    .append(",")
                    .append(rightColumn);
        }
        //条件
        StringBuffer joinWhere = new StringBuffer();
        crossBindingParams.getCrossBindingObj().getConnectionFields().forEach(e ->
                joinWhere.append(LEFT_TABLE_ALIAS)
                        .append(".`")
                        .append(e.getLeftFieldKey())
                        .append("`")
                        .append("=")
                        .append(RIGHT_TABLE_ALIAS)
                        .append(".`")
                        .append(e.getRightFieldKey())
                        .append("` and ")
        );
        //删除结尾的and
        joinWhere.delete(joinWhere.length() - 4, joinWhere.length());
        //join
        sql.append("FROM ")
                .append(leftTableName)
                .append(" AS ")
                .append(LEFT_TABLE_ALIAS);
        switch (crossBindingParams.getCrossBindingObj().getConnection()) {
            case left:
                sql.append(" LEFT JOIN ");
                break;
            case join:
                sql.append(" JOIN ");
                break;
            case right:
                sql.append(" RIGHT JOIN ");
                break;
            default:
        }
        sql.append(rightTableName)
                .append(" AS ")
                .append(RIGHT_TABLE_ALIAS)
                .append(" ON ( ")
                .append(joinWhere)
                .append(" )");
    }

    /**
     * 构建连接sql
     *
     * @param targetFieldList    目标数据库的表结构
     * @param sql                sql
     * @param crossBindingParams 用户设计数据
     * @param leftFieldList      左表表结构
     * @param leftTableName      左表名称
     * @param rightFieldList     右表表结构
     * @param rightTableName     右表名称
     * @param targetTableName    目标表名称
     */
    public void unionCreateSql(List<DataSourceField> leftFieldList, List<DataSourceField> rightFieldList, List<DataSourceField> targetFieldList, String leftTableName, String rightTableName, StringBuffer sql, String targetTableName, CrossBindingParams crossBindingParams) {
        DorisConfig bean = SpringContextUtil.getBean(DorisConfig.class);
        String targetColumn = targetFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
        //格式首尾
        targetColumn = "`" + targetColumn + "`";
        sql.append("INSERT INTO ")
                .append(bean.getLibraryName())
                .append(".")
                .append(targetTableName)
                .append(" (")
                .append(targetColumn)
                .append(") ");
        StringBuffer leftJoinSql = new StringBuffer();
        //构建连接
        leftJoinSql.append("SELECT ");
        //构建需要展示的表结构
        String leftColumn = leftFieldList.stream().map(DataSourceField::getFieldKey).map(e -> LEFT_TABLE_ALIAS + ".`" + e + "`").collect(Collectors.joining(","));
        String rightColumn = rightFieldList.stream().map(DataSourceField::getFieldKey).map(e -> RIGHT_TABLE_ALIAS + ".`" + e + "`").collect(Collectors.joining(","));
        leftJoinSql.append(leftColumn)
                .append(",")
                .append(rightColumn);
        //条件
        StringBuffer joinWhere = new StringBuffer();
        crossBindingParams.getCrossBindingObj().getConnectionFields().forEach(e ->
                joinWhere.append(LEFT_TABLE_ALIAS)
                        .append(".`")
                        .append(e.getLeftFieldKey())
                        .append("`")
                        .append("=")
                        .append(RIGHT_TABLE_ALIAS)
                        .append(".`")
                        .append(e.getRightFieldKey())
                        .append("` and ")
        );
        //删除结尾的and
        joinWhere.delete(joinWhere.length() - 4, joinWhere.length());
        leftJoinSql.append(" FROM ")
                .append(leftTableName)
                .append(" AS ")
                .append(LEFT_TABLE_ALIAS)
                .append(" LEFT JOIN ")
                .append(rightTableName)
                .append(" AS ")
                .append(RIGHT_TABLE_ALIAS)
                .append(" ON ( ")
                .append(joinWhere)
                .append(" )");
        StringBuffer rightJoinSql = new StringBuffer();
        //过滤左边数据
        String leftWhere = crossBindingParams.getCrossBindingObj().getConnectionFields().stream().map(e -> "leftTable.`" + e.getLeftFieldKey() + "` IS NULL").collect(Collectors.joining(" and "));
        rightJoinSql.append(" SELECT ")
                .append(leftColumn)
                .append(",")
                .append(rightColumn)
                .append(" FROM ")
                .append(leftTableName)
                .append(" AS ")
                .append(LEFT_TABLE_ALIAS)
                .append(" RIGHT JOIN ")
                .append(rightTableName)
                .append(" AS ")
                .append(RIGHT_TABLE_ALIAS)
                .append(" ON ( ")
                .append(joinWhere)
                .append(" ) where ")
                .append(leftWhere);

        sql.append(leftJoinSql)
                .append(" union ")
                .append(rightJoinSql);

    }
}
