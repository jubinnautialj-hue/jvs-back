package cn.bctools.data.factory.api;

import cn.bctools.common.utils.R;
import cn.bctools.data.factory.api.dto.JvsDataFactoryDto;
import cn.bctools.data.factory.api.dto.RequestDetailDto;
import cn.bctools.data.factory.api.dto.RowWhereDto;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Administrator
 * api接口
 */
@Component
@FeignClient(value = "jvs-data-factory-mgr", contextId = "dataFactory")
public interface DataFactoryApi {

    String PREFIX = "/api/data/factory";


    /**
     * @param name 设计名称
     */
    @ApiOperation("获取所有设计")
    @GetMapping(PREFIX + "/get")
    R<List<Dict>> getAll(@RequestParam(value = "name", defaultValue = "") String name);

    /**
     * @param
     */
    @ApiOperation("获取所有设计")
    @GetMapping(PREFIX + "/get/table/name")
    R<List> getTableNames();

    @ApiOperation("获取设计个数")
    @GetMapping(PREFIX + "/size")
    R<Integer> size();

    /**
     * 获取结果
     *
     * @param dataId 设计id
     * @return 结构
     */
    @ApiOperation("通过设计id获取输出结构")
    @GetMapping(PREFIX + "/get/data")
    R<List<JSONObject>> getTable(@RequestParam(value = "dataId") String dataId);

    /**
     * 获取数据权限行权限
     *
     * @param dataId 设计id
     * @return 结构
     */
    @ApiOperation("通过设计id行数据权限")
    @GetMapping(PREFIX + "/get/auth/row")
    R<RowWhereDto> getAuthRow(@RequestParam(value = "dataId") String dataId);


    /**
     * 获取设计的输出结果
     *
     * @param requestDetailDto 设计id
     * @return 表结构
     */
    @ApiOperation("通过设计id获取输出结果")
    @PostMapping(PREFIX + "/get/table")
    R getData(@RequestBody RequestDetailDto requestDetailDto);

    /**
     * 获取设计的输出结果
     *
     * @param dataFactoryId 智仓id
     * @return 表结构
     */
    @ApiOperation("获取数据结果的数据量")
    @GetMapping(PREFIX + "/get/table/count")
    R<Long> getDataCount(@RequestParam(value = "dataId") String dataFactoryId);

    /**
     * 获取数据结果存储的表名称
     * 此方法会同时验证数据集 数据集结果 数据集输出的表是否正常
     *
     * @param dataFactoryId 智仓id
     * @return 表结构
     */
    @ApiOperation("获取数据结果存储的表名称")
    @GetMapping(PREFIX + "/get/table/name/id")
    R<String> getTableName(@RequestParam(value = "dataId") String dataFactoryId);

    @ApiOperation("通过分页查询数据")
    @GetMapping(PREFIX + "/page/data")
    R page(@RequestParam(value = "dataId") String dataId, @RequestParam(value = "current") Long current, @RequestParam(value = "size") Long size);

    @ApiOperation("根据id获取数据集数据 基础信息")
    @GetMapping(PREFIX + "/design/{id}")
    R<JvsDataFactoryDto> getById(@ApiParam("数据集id") @PathVariable("id") String id);

    @ApiOperation("根据id获取数据集数据 基础信息")
    @GetMapping(PREFIX + "/design/documentName/{id}")
    R<String> getDocumentName(@ApiParam("数据集id") @PathVariable("id") String id);

    @ApiOperation("根据id 获取所有关联的数据集id与数据源id-用于下载")
    @PostMapping(PREFIX + "/down/get/ids")
    R<List<SourceFactoryGetIdDto>> getSourceFactory(@RequestBody List<String> ids);

    @ApiOperation("根据id 获取数据")
    @PostMapping(PREFIX + "/query/by/ids")
    R<List<JSONObject>> getByIds(@RequestBody List<String> ids);

    @ApiOperation("上传数据")
    @PostMapping(PREFIX + "/up/{menuName}")
    R<Boolean> up(@RequestBody  JSONObject data,@PathVariable("menuName") String menuName);
}
