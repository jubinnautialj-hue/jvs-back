package cn.bctools.design.data.controller;

import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.service.DataFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Api(tags = "[data]数据模型 use")
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}/data/model")
public class DataModelUseController {

    DataFieldService dataFieldService;

    /**
     * 根据模型id查询是否
     *
     * @return 模型基本数据集合
     */
    @ApiOperation("查询模型下所有字段")
    @GetMapping("/list/{modelId}")
    public R<List> dataModel(@PathVariable String modelId, @PathVariable String appId) {
        List<FieldBasicsHtml> allField = dataFieldService.getAllField(appId, modelId);
        return R.ok(allField);
    }

}
