package cn.bctools.data.factory.html.fill;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.node.params.DataFillParams;
import cn.bctools.data.factory.util.DorisUtil;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据填充抽象类
 *
 * @author Administrator
 */
public interface DataFillService {


    /**
     * 线程数量
     *
     * @param dataFillObj 数据填充设计数据
     * @param list        数据
     */
    default void insertValue(DataFillParams.DataFillObj dataFillObj, List<Object> list) {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO `").append(dataFillObj.getTableName()).append("`(`" + dataFillObj.getFieldKey() + "`) VALUES ");
        list.forEach(e -> insertSql.append("(?),"));
        insertSql.delete(insertSql.length() - 1, insertSql.length());
        SpringContextUtil.getBean(DorisJdbcTemplate.class).update(insertSql.toString(), list.toArray());
    }

    /**
     * 根据规则生成全量数据
     *
     * @param dataFillObj 填充配置
     */
    void dataGenerate(DataFillParams.DataFillObj dataFillObj);

    /**
     * 生成表
     *
     * @param dataFillObj 填充配置
     */
    default void createTable(DataFillParams.DataFillObj dataFillObj) {
        String tableName = dataFillObj.getTableName();
        List<DataSourceField> dataSourceFields = new ArrayList<>();
        DataSourceField sourceField = JSONObject.parseObject(JSONObject.toJSONString(dataFillObj), DataSourceField.class);
        dataSourceFields.add(sourceField);
        String tableSql = DorisUtil.getTableSql(tableName, dataSourceFields, Boolean.FALSE, new ArrayList<>());
        SpringContextUtil.getBean(DorisJdbcTemplate.class).execute(tableSql);
    }
}
