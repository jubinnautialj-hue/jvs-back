package cn.bctools.report.mock;

import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.report.mock.demo.BuiltInData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MockConfig {

    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final String MOCK_TABLE_NAME = "jvs_report_mock_data";

    @PostConstruct
    public void init() {
        try {
            boolean b = dorisJdbcTemplate.ifNotExistsTable(MOCK_TABLE_NAME);
            if(!b){
                String createSql = getCreateSql();
                dorisJdbcTemplate.execute(createSql);
                List<Map<String, Object>> mockData = BuiltInData.getMockData();
                List<DataSourceField> fields = getFields();
                dorisJdbcTemplate.insert(mockData,fields,MOCK_TABLE_NAME);
            }
        } catch (DataAccessException e) {
            log.error("注册mock数据失败");
        }
    }

    private String getCreateSql(){
        return "CREATE TABLE IF NOT EXISTS "+MOCK_TABLE_NAME+" (\n" +
                "    id BIGINT COMMENT '唯一订单ID',\n" +
                "    province VARCHAR(20) COMMENT '省份',\n" +
                "    city VARCHAR(20) COMMENT '城市',\n" +
                "    order_amount DECIMAL(12, 2) COMMENT '订单金额',\n" +
                "    transportation_cost DECIMAL(10, 2) COMMENT '运输成本',\n" +
                "    profit DECIMAL(10, 2) COMMENT '利润',\n" +
                "    merchant_name VARCHAR(50) COMMENT '商家名称'\n" +
                ")\n" +
                "ENGINE=OLAP\n" +
                "DUPLICATE KEY(id)\n" +
                "COMMENT '订单明细表'\n" +
                "DISTRIBUTED BY HASH(id) BUCKETS 10\n" +
                "PROPERTIES (\n" +
                "    \"replication_num\" = \"1\",\n" +
                "    \"storage_format\" = \"V2\"\n" +
                ");";
    }

    private List<DataSourceField> getFields(){
        List<DataSourceField> fieldList = new ArrayList<>();
        fieldList.add(new DataSourceField().setFieldKey("id").setFieldType(DataFieldTypeEnum.BIGINT));
        fieldList.add(new DataSourceField().setFieldKey("province").setFieldType(DataFieldTypeEnum.VARCHAR));
        fieldList.add(new DataSourceField().setFieldKey("city").setFieldType(DataFieldTypeEnum.VARCHAR));
        fieldList.add(new DataSourceField().setFieldKey("order_amount").setFieldType(DataFieldTypeEnum.DECIMAL));
        fieldList.add(new DataSourceField().setFieldKey("transportation_cost").setFieldType(DataFieldTypeEnum.DECIMAL));
        fieldList.add(new DataSourceField().setFieldKey("profit").setFieldType(DataFieldTypeEnum.DECIMAL));
        fieldList.add(new DataSourceField().setFieldKey("merchant_name").setFieldType(DataFieldTypeEnum.VARCHAR));
        return fieldList;
    }

}
