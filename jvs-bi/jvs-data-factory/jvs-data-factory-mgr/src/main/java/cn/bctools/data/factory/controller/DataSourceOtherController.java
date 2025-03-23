package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.data.factory.source.data.db2.Db2DataSourceExecuteImpl;
import cn.bctools.data.factory.source.data.dmdb.DMDataSourceExecuteImpl;
import cn.bctools.data.factory.source.data.kingbase.es.KingBaseEsExecuteImpl;
import cn.bctools.data.factory.source.data.oracle.OracleDataSourceExecuteImpl;
import cn.bctools.data.factory.source.data.postgresql.PostgreSqlExecuteImpl;
import cn.bctools.data.factory.source.data.presto.PrestoDBExecuteImpl;
import cn.bctools.data.factory.source.data.sql.server.SqlServerExecuteImpl;
import cn.bctools.data.factory.source.dto.*;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.lang.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author admin
 */
@Api(tags = "[jvs-data-source]数据源获取其他参数例如获取pgsql的模式")
@Slf4j
@RestController
@RequestMapping("/data/source/other")
public class DataSourceOtherController {
    @Autowired
    PostgreSqlExecuteImpl postgreSqlExecuteImpl;
    @Autowired
    OracleDataSourceExecuteImpl oracleDataSourceExecute;
    @Autowired
    DMDataSourceExecuteImpl dmDataSourceExecute;
    @Autowired
    Db2DataSourceExecuteImpl db2DataSourceExecute;
    @Autowired
    KingBaseEsExecuteImpl kingbaseDataSourceExecute;
    @Autowired
    PrestoDBExecuteImpl prestoDBExecute;
    @Autowired
    SqlServerExecuteImpl sqlServerExecute;
    @Autowired
    DataSourceService dataSourceService;

    @Log(back = false)
    @ApiOperation("获取schema")
    @GetMapping("/{type}/get/schema")
    public R<List<Dict>> getSchema(@PathVariable("type") String type, @RequestParam Map<String, Object> connectDto) {
        if (connectDto.containsKey("id")) {
            connectDto = dataSourceService.getById(connectDto.get("id").toString()).getCustomJson();
        }
        List<String> schema;
        switch (type) {
            case "pgsql":
                PostgreSqlConnectDto postgreSqlConnectDto = BeanCopyUtil.copy(connectDto, PostgreSqlConnectDto.class);
                schema = postgreSqlExecuteImpl.getSchema(postgreSqlConnectDto);
                break;
            case "oracle":
                OracleConnectDto oracleConnectDto = BeanCopyUtil.copy(connectDto, OracleConnectDto.class);
                schema = oracleDataSourceExecute.getSchema(oracleConnectDto);
                break;
            case "kingbase":
                KingBaseConnectDto kingBaseConnectDto = BeanCopyUtil.copy(connectDto, KingBaseConnectDto.class);
                schema = kingbaseDataSourceExecute.getSchema(kingBaseConnectDto);
                break;
            case "dm":
                DMConnectDto dmConnectDto = BeanCopyUtil.copy(connectDto, DMConnectDto.class);
                schema = dmDataSourceExecute.getSchema(dmConnectDto);
                break;
            case "db2":
                Db2ConnectDto db2ConnectDto = BeanCopyUtil.copy(connectDto, Db2ConnectDto.class);
                schema = db2DataSourceExecute.getSchema(db2ConnectDto);
                break;
            case "presto":
                PrestoConnectDto prestoConnectDto = BeanCopyUtil.copy(connectDto, PrestoConnectDto.class);
                schema = prestoDBExecute.getSchema(prestoConnectDto);
                break;
            case "sqlServer":
                SqlServerConnectDto sqlServerConnectDto = BeanCopyUtil.copy(connectDto, SqlServerConnectDto.class);
                schema = sqlServerExecute.getSchema(sqlServerConnectDto);
                break;
            default:
                schema = Collections.emptyList();
        }
        List<Dict> dicts = schema.stream().map(e -> Dict.create().set("label", e).set("value", e)).collect(Collectors.toList());
        return R.ok(dicts);
    }

}
