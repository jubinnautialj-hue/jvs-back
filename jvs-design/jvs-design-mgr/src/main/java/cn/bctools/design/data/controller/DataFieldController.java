package cn.bctools.design.data.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.service.DataFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据模型管理
 *
 * @Author: GuoZi
 */
@Slf4j
@Api(tags = "[data]数据字段")
@RestController
@AllArgsConstructor
@RequestMapping("/app/design/{appId}/data/field")
public class DataFieldController {

    DataFieldService fieldService;

    /**
     * 查询数据模型的字段信息
     *
     * @param designId 设计id
     * @return 字段信息集合
     */
    @ApiOperation("查询模型字段")
    @GetMapping("/list")
    public R<List<FieldBasicsHtml>> list(@ApiParam(value = "模型id", required = true) @RequestParam(name = "modelId") String modelId,
                                         @ApiParam("设计id") @RequestParam(name = "designId", required = false) String designId, @PathVariable String appId) {
        return R.ok(fieldService.getFields(appId, modelId, designId, false, true));
    }


    /**
     * 查询数据模型的用户相关字段信息
     *
     * @param designId 设计id
     * @return 用户相关字段信息集合
     */
    @ApiOperation("查询模型用户相关字段")
    @GetMapping("/user/list")
    public R<List<FieldBasicsHtml>> userFieldList(@ApiParam(value = "模型id", required = true) @RequestParam(name = "modelId") String modelId,
                                         @ApiParam("设计id") @RequestParam(name = "designId", required = false) String designId, @PathVariable String appId) {
        List<FieldBasicsHtml> fields = fieldService.getFields(appId, modelId, designId, false, false);
        if (ObjectNull.isNull(fields)) {
            return R.ok(Collections.emptyList());
        }
        return R.ok(fields.stream()
                .filter(field -> DataFieldType.user.equals(field.getType()) || DataFieldType.department.equals(field.getType()))
                .collect(Collectors.toList()));
    }

}
