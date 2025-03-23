package cn.bctools.design.data.controller;//package cn.bctools.design.data.controller;
//
//import cn.bctools.common.utils.R;
//import cn.bctools.design.data.fields.dto.DataModelDto;
//import cn.bctools.design.data.service.DataModelService;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * 数据模型管理
// *
// * @Author: GuoZi
// */
//@Slf4j
//@Api(tags = "[data]数据模型")
//@RestController
//@AllArgsConstructor
//@RequestMapping("/base/data/model")
//public class DataModelController {
//
//    DataModelService dataModelService;
//
//    @ApiOperation("数据模型添加索引")
//    @PostMapping("/indexField/{modelId}/{fieldKey}")
//    @Transactional(rollbackFor = Exception.class)
//    public R<Page<DataModelDto>> indexField(@PathVariable String modelId, @PathVariable String fieldKey, @PathVariable String appId) {
//        dataModelService.indexField(modelId, fieldKey, appId);
//        return R.ok();
//    }
//}
