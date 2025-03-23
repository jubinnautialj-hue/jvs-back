package cn.bctools.data.factory.html.run;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisConfig;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.Constant;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.source.entity.JvsSourceToDoris;
import cn.bctools.data.factory.source.service.JvsSourceToDorisService;
import cn.bctools.data.factory.util.DorisUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
public interface Frun<T extends NodeHtml> {

    /**
     * 将数据进行处理后再结合,输入数据可能是会多个
     * 根据不同的节点处理不同的数据集
     *
     * @param formal   是否正式运行 ， 只有输入，和输出节点才会有用其它 不用判断,输入节点判断个数，输出节点，处理数据， 不返回数据
     * @param linkBody 节点的id ， 和节点的数据集  是数据流。如果数据流  处理的下级的数据集，将关联这个节点的其它数据都拿过来进行显示.最近的一个或两个节点的数据集
     *                 Map<Stirng,FData>  节点的nodeId 和节点的数据</Stirng,FData>
     * @param t        不同的入参
     * @return 处理结果
     */
    FData run(Boolean formal, Map<String, FData> linkBody, T t);

    /**
     * 保存数据 直接拉取数据保存
     *
     * @param documentName 表名称
     * @param firstTime    是否为第一次如果是就需要新建表
     * @param fieldList    字段
     * @param expandObj    入参
     * @param sourceType   如果类型为空 就表示直接获取{@link DataSourceField}
     * @param isUnique     是否创建 unique模型  目前只用于条件分组节点 因为 只有 此模型支持update 语句
     * @param sql          执行的sql 语句
     */
    default void save(String sql, String documentName, List<DataSourceField> fieldList, Boolean firstTime, String sourceType, Boolean isUnique, List<DataSourceField> uniqueKey, Object... expandObj) {
        //先判断是否存在此表 如果存在直接插入数据
        DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        if (firstTime) {
            createTable(documentName, fieldList, sourceType, isUnique, uniqueKey);
        }
        //插入数据
        dorisJdbcTemplate.executeCustomSql(sql, expandObj);
    }


    /**
     * 创建数据库
     *
     * @param sourceType   数据来源
     * @param documentName 数据库名称
     * @param fieldList    字段
     * @param isUnique     是否生成 Unique模型表
     */
    default void createTable(String documentName, List<DataSourceField> fieldList, String sourceType, Boolean isUnique, List<DataSourceField> uniqueKey) {
        //防止为null
        uniqueKey = Optional.ofNullable(uniqueKey).orElse(new ArrayList<>());
        DorisJdbcTemplate dorisJdbcTemplate = SpringContextUtil.getBean(DorisJdbcTemplate.class);
        //设置映射关系
        if (sourceType != null) {
            //获取映射关系表
            JvsSourceToDorisService bean = SpringContextUtil.getBean(JvsSourceToDorisService.class);
            Map<String, String> map = bean.list(new LambdaQueryWrapper<JvsSourceToDoris>().eq(JvsSourceToDoris::getSourceType, sourceType))
                    .stream().collect(Collectors.toMap(JvsSourceToDoris::getFieldType, JvsSourceToDoris::getDorisFieldType));
            fieldList.stream().peek(e -> e.setDorisType(map.getOrDefault(e.getDataType(), DataFieldTypeEnum.STRING.getCreateTable()))).collect(Collectors.toList());
        }
        //过滤数据
        fieldList = fieldList.stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
        List<DataSourceField> fields = JSONArray.parseArray(JSONObject.toJSONString(fieldList), DataSourceField.class);
        String tableSql = DorisUtil.getTableSql(documentName, fields, isUnique, uniqueKey);
        dorisJdbcTemplate.execute(tableSql);
    }

    /**
     * 生成sql
     *
     * @param sql                      sql
     * @param t                        不同节点的参数
     * @param sourceTable              源表
     * @param targetTableName          目标表
     * @param addOrderBy               是否添加排序
     * @param sourceFieldList          来源表的字段属性
     * @param targetFieldList          目标表的字段属性
     * @param sourceFieldEqTargetField 目标源是否等于输入源字段
     * @return sql执行时的入参
     */
    default List<Object> createSql(List<DataSourceField> sourceFieldList, List<DataSourceField> targetFieldList, StringBuffer sql, String sourceTable, String targetTableName, boolean sourceFieldEqTargetField, boolean addOrderBy, T t) {
        //通过部分的数据只会生成基础的
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
        String sourceColumn;
        //如果输入源 与 目标源字段一致就直接使用输入源的 减少计算
        if (sourceFieldEqTargetField) {
            sourceColumn = targetColumn;
        } else {
            sourceColumn = sourceFieldList.stream().filter(DataSourceField::getIsShow).map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
            sourceColumn = "`" + sourceColumn + "`";
        }
        sql.append("SELECT ")
                .append(sourceColumn)
                .append(" FROM ")
                .append(bean.getLibraryName())
                .append(".")
                .append(sourceTable);
        if (addOrderBy) {
            sql.append(" ORDER BY ").append(Constant.DORIS_ODS_KEY).append(" desc");
        }
        return new ArrayList<>();
    }
}
