package cn.bctools.data.factory.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.api.DataSourceApi;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.ExcelCommitLog;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.ExcelCommitLogService;
import cn.bctools.database.entity.po.BasalPo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
@Api(tags = "数据源api")
@RestController
@AllArgsConstructor
public class DataSourceApiController implements DataSourceApi {
    private final DataSourceService dataSourceService;
    private final DataSourceStructureService dataSourceStructureService;
    private final ExcelCommitLogService excelCommitLogService;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final Map<String, DataSourceExecuteInterface> dataSourceExecuteInterfaceMap;

    @Override
    public R<String> getDataFatcorySourceId() {
        dataSourceService.saveDataFactory();
        DataSource dataSource = dataSourceService.getOne(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        return R.ok(dataSource.getId());
    }

    @Override
    public R<Boolean> up(JSONObject jsonObject) {
        UserDto userDto = jsonObject.getJSONObject("userDto").toJavaObject(UserDto.class);
        String tenantId = TenantContextHolder.getTenantId();
        for (Object object : jsonObject.getJSONArray("data")) {
            DataSource dataSource = JSONObject.parseObject(JSONObject.toJSONString(object), DataSource.class);
            DataSourceExecuteInterface dataSourceExecuteInterface = dataSourceExecuteInterfaceMap.get(dataSource.getSourceType().value);
            dataSourceExecuteInterface.up(dataSource, userDto);
            TenantContextHolder.setTenantId(tenantId);
        }
        return R.ok(Boolean.TRUE);
    }

    @Override
    public R<List<JSONObject>> getByList(List<String> ids) {
        List<JSONObject> jsonObjects = dataSourceService.listByIds(ids).stream()
                .peek(e -> {
                    List<DataSourceStructure> list = dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, e.getId()));
                    e.setChildren(list);
                    //判断是否为excel如果是就需要把数据导出
                    if (e.getSourceType().equals(DataSourceTypeEnum.excelDataSource)) {
                        JSONObject jsonObject = new JSONObject();
                        //获取数据组成记录
                        List<ExcelCommitLog> commitLogs = excelCommitLogService.list(Wrappers.lambdaQuery(ExcelCommitLog.class).eq(ExcelCommitLog::getDataSourceId, e.getId()).eq(ExcelCommitLog::getOperateStatus, Boolean.TRUE).orderByAsc(BasalPo::getCreateTime));
                        //获取数据
                        String executeName = list.get(0).getExecuteName();
                        List<Map<String, Object>> data = dorisJdbcTemplate.getData(executeName);
                        //获取建表语句
                        String createTableSql = dorisJdbcTemplate.queryForList("SHOW CREATE TABLE " + executeName).get(0).get("Create Table").toString();
                        jsonObject.put("createTableSql", createTableSql);
                        jsonObject.put("data", data);
                        jsonObject.put("excelCommitLog", commitLogs);
                        e.setExportData(jsonObject);
                    }
                }).map(e -> JSONObject.parseObject(JSONObject.toJSONString(e))).collect(Collectors.toList());
        return R.ok(jsonObjects);
    }
}
