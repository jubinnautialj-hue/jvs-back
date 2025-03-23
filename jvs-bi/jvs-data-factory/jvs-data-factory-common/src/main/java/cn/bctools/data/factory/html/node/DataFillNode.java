package cn.bctools.data.factory.html.node;


import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.enums.DataFillTypeEnum;
import cn.bctools.data.factory.html.fill.DataFillService;
import cn.bctools.data.factory.html.node.params.DataFillParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.query.QueryExecuteFactory;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据填充
 *
 * @author guojing
 */

@Data
@Service
@AllArgsConstructor
public class DataFillNode implements Frun<DataFillParams> {

    private final QueryExecuteFactory queryExecuteFactory;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final static String LEFT_MAIN_AS_NAME = "leftMainTable";

    @Override
    public FData run(Boolean formal, Map<String, FData> linkBody, DataFillParams dataFillParams) {
        String next = linkBody.keySet().iterator().next();
        FData fData = new FData();
        String documentName = linkBody.get(next).getDocumentName();
        fData.setDocumentName(documentName);
        List<DataSourceField> title = linkBody.get(next).getTitle();
        List<DataFillParams.DataFillObj> dataFillObj = dataFillParams.getSourceData().getDataFillObj();
        long count = dataFillObj.stream().filter(DataFillParams.DataFillObj::getIsMain).count();
        //如果没有任何配置直接使用上一个节点 的数据即可 或者没有主要填充字段
        if (!dataFillObj.isEmpty() && count > 0) {
            //中间表名称
            String nodeDocumentName = dataFillParams.getTableName();
            fData.setDocumentName(nodeDocumentName);
            //上一个节点的数据库名称
            StringBuffer sql = new StringBuffer();
            List<Object> objects = createSql(new ArrayList<>(), title, sql, documentName, nodeDocumentName, Boolean.TRUE, Boolean.FALSE, dataFillParams.getSourceData());
            this.save(sql.toString(), nodeDocumentName, title, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
            this.fill(dataFillParams.getSourceData(), nodeDocumentName);
        }
        return fData.setTitle(title);
    }

    /**
     * 数据填充
     *
     * @param dataFillParams 设计数据
     */
    private void fill(DataFillParams dataFillParams, String tableName) {
        List<DataFillParams.DataFillObj> dataFillObj = dataFillParams.getDataFillObj();
        //主字段 并生成临时表名称 方便统一删除
        List<DataFillParams.DataFillObj> fillMain = dataFillObj.stream().filter(DataFillParams.DataFillObj::getIsMain)
                .peek(e -> e.setTableName("data_fill_" + e.getFieldKey()))
                .collect(Collectors.toList());
        if (fillMain.isEmpty()) {
            return;
        }
        try {
            //次字段 过滤设置了默认值的字段
            List<DataFillParams.DataFillObj> fillNoMain = dataFillObj.stream().filter(e -> !e.getIsMain() && StrUtil.isNotBlank(e.getFixedValue())).collect(Collectors.toList());
            //生成数据
            fillMain.stream().peek(e -> {
                DataFillTypeEnum type = e.getType();
                DataFillService dataFillService = SpringContextUtil.getBean(type.getAClass());
                dataFillService.createTable(e);
                dataFillService.dataGenerate(e);
            }).collect(Collectors.toList());
            //通过生成的数据交叉合并入库并判断是否存在此数据 这里直接使用 CROSS JOIN 语句
            //笛卡尔积 sql定义
            StringBuilder sql = new StringBuilder();
            sql.append("select ");
            //把需要添加默认值的数据放到语句中
            fillNoMain.forEach(e -> {
                switch (e.getDataFieldTypeClassify()) {
                    case 数字:
                        sql.append(e.getFixedValue());
                        break;
                    default:
                        sql.append("'").append(e.getFixedValue()).append("'");
                }
                sql.append(" as `").append(e.getFieldKey()).append("`,");
            });
            //第一个字段为主表
            DataFillParams.DataFillObj fillObj = fillMain.get(0);
            sql.append(fillObj.getTableName()).append(".`").append(fillObj.getFieldKey()).append("`");
            //添加字段
            for (int i = 1; i < fillMain.size(); i++) {
                sql.append(",").append(fillMain.get(i).getTableName()).append(".`").append(fillMain.get(i).getFieldKey()).append("`");
            }
            //定义合并语法
            sql.append(" FROM ").append(fillObj.getTableName());
            //添加 合并
            for (int i = 1; i < fillMain.size(); i++) {
                sql.append(" CROSS JOIN ").append(fillMain.get(i).getTableName());
            }
            ;
            //通过join的方式查找 需要填充的值
            StringBuilder mainJoin = new StringBuilder();
            mainJoin.append("SELECT ");
            //获取本次需要写入的字段
            List<DataFillParams.DataFillObj> fillObjList = dataFillObj.stream().filter(e -> e.getIsMain() || StrUtil.isNotBlank(e.getFixedValue())).collect(Collectors.toList());
            String fieldStr = fillObjList.stream().map(DataSourceField::getFieldKey)
                    .collect(Collectors.joining("`," + LEFT_MAIN_AS_NAME + ".`"));
            fieldStr = LEFT_MAIN_AS_NAME + ".`" + fieldStr + "`";
            mainJoin.append(fieldStr)
                    .append(" FROM ")
                    .append(tableName)
                    .append(" RIGHT JOIN (")
                    .append(sql)
                    .append(") as ")
                    .append(LEFT_MAIN_AS_NAME)
                    .append(" on ");
            //where条件
            StringBuilder mainJoinWhere = new StringBuilder();
            //添加条件
            for (int i = 0; i < fillMain.size(); i++) {
                String fieldKey = fillMain.get(i).getFieldKey();
                mainJoin.append(tableName).append(".`").append(fieldKey).append("`")
                        .append("=").append(LEFT_MAIN_AS_NAME).append(".`").append(fieldKey)
                        .append("` AND ");
                mainJoinWhere.append(tableName).append(".").append("`").append(fieldKey).append("` IS NULL AND ");
            }
            mainJoinWhere.delete(mainJoinWhere.length() - 4, mainJoinWhere.length());
            mainJoin.delete(mainJoin.length() - 4, mainJoin.length());
            mainJoin.append(" where ").append(mainJoinWhere);
            //插入语句
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("INSERT INTO ").append(tableName).append("(");
            String insertIntoKey = fillObjList.stream().map(DataSourceField::getFieldKey).collect(Collectors.joining("`,`"));
            insertIntoKey = "`" + insertIntoKey + "`";
            insertSql.append(insertIntoKey).append(") ").append(mainJoin);
            //执行sql
            dorisJdbcTemplate.execute(insertSql.toString());
        } finally {
            fillMain.stream().map(DataFillParams.DataFillObj::getTableName).forEach(dorisJdbcTemplate::dropForce);
        }

    }


}
