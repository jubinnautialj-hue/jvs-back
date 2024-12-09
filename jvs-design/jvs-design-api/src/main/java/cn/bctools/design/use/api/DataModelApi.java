package cn.bctools.design.use.api;

import cn.bctools.common.utils.R;
import cn.bctools.design.use.api.dto.DataFiledDto;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.DataModelSearchDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The interface Data model api.
 *
 * @author zxk
 */
@Component
@FeignClient(value = "jvs-design-mgr", contextId = "datamodel")
public interface DataModelApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/jvsdesign/datamodel";

    /**
     * 查询数据模型的数据
     *
     * @param dataModelId 数据模型id
     * @param size        分页大小
     * @param current     页大小
     * @param mode        the mode
     * @return r
     */
    @ApiOperation("获取所有数据")
    @GetMapping(PREFIX + "/list")
    R<List> list(@RequestParam(value = "dataModelId") String dataModelId, @RequestParam(value = "size") long size, @RequestParam(value = "current") long current, @RequestParam(value = "mode") String mode);

    /**
     * 查询数据模型的数据
     *
     * @param searchDto the search dto
     * @return r
     */
    @ApiOperation("查询数据")
    @PostMapping(PREFIX + "/search")
    R<List> search(@RequestBody DataModelSearchDto searchDto);

    /**
     * 根据模型dataModelId  查询条数
     *
     * @param dataModelId 数据模型
     * @param mode        the mode
     * @return 返回总条数 count
     */
    @ApiOperation("获取所有数据条数")
    @GetMapping(PREFIX + "/get/count")
    R<Long> getCount(@RequestParam(value = "dataModelId") String dataModelId, @RequestParam(value = "mode") String mode);

    /**
     * 根据模型dataModelId  查询条数
     *
     * @param searchDto the search dto
     * @return 返回总条数 r
     */
    @ApiOperation("获取所有数据条数")
    @PostMapping(PREFIX + "/search/count")
    R<Long> countBySearch(@RequestBody DataModelSearchDto searchDto);

    /**
     * 根据数据模型ID查询字段信息
     * 此接口 bi使用较多
     *
     * @param appId       应用id
     * @param dataModelId 数据模型
     * @param mode        模式信息
     * @return 返回字段的详细信息
     */
    @ApiOperation("获取字段map对象")
    @GetMapping(PREFIX + "/field/map")
    R<List<DataFiledDto>> fieldMap(@RequestParam(value = "appId") String appId, @RequestParam(value = "dataModelId") String dataModelId, @RequestParam(value = "mode") String mode);

    /**
     * 根据数据模型ID查询字段信息
     * 此接口 bi使用较多
     * 提供给数据抽取的单独接口
     *
     * @param appId       应用id
     * @param dataModelId 数据模型
     * @param mode        模式信息
     * @return 返回字段的详细信息
     */
    @GetMapping(PREFIX + "/field/map/data")
    R<List<DataFiledDto>> fieldMapData(@RequestParam(value = "appId") String appId, @RequestParam(value = "dataModelId") String dataModelId, @RequestParam(value = "mode") String mode);

    /**
     * 根据数据模型标识查询字段信息
     * 此接口 bi使用较多
     *
     * @param tableCode   数据模型标识
     * @param mode        模式信息
     * @return 返回字段的详细信息
     */
    @ApiOperation("根据数据模型标识查询字段信息")
    @GetMapping(PREFIX + "/field/list")
    R<List<DataFiledDto>> dataFieldMap(@RequestParam(value = "tableCode") String tableCode, @RequestParam(value = "mode") String mode);

    /**
     * 根据数据模型查询字段详细信息结构
     *
     * @param appId 数据模型信息
     * @param mode  the mode
     * @return 返回字段详细信息 r
     */
    @ApiOperation("获取数据模型集")
    @GetMapping(PREFIX + "/datamodel/list")
    R<List<DataModelDto>> dataModelList(@RequestParam(value = "appId") String appId, @RequestParam(value = "mode") String mode);

}
