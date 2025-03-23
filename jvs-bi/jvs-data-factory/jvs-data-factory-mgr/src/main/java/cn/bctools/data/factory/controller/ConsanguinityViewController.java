package cn.bctools.data.factory.controller;

import cn.bctools.common.utils.R;
import cn.bctools.data.factory.consanguinity.view.dto.Neo4jDto;
import cn.bctools.data.factory.consanguinity.view.entity.ConsanguinityViewEntity;
import cn.bctools.data.factory.consanguinity.view.service.ConsanguinityViewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Administrator
 */
@Api(tags = "血缘视图")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/consanguinity/view")
public class ConsanguinityViewController {
    private final ConsanguinityViewService consanguinityViewService;

    @ApiOperation("获取血缘")
    @GetMapping("/get/{id}")
    public R<Neo4jDto> getById(@ApiParam("数据集id") @PathVariable("id") String id) {
        Neo4jDto neo4jDto = consanguinityViewService.list(id);
        return R.ok(neo4jDto);
    }

    @ApiOperation("删除数据集与数据源时 获取是否存在使用方")
    @GetMapping("/check/{id}/{type}")
    public R<List<ConsanguinityViewEntity>> check(@ApiParam("数据集id") @PathVariable("id") String id, @ApiParam("来源类型 数据源1,数据集2") @PathVariable("type") Integer type) {
        List<ConsanguinityViewEntity> list = consanguinityViewService.check(id, type);
        return R.ok(list);
    }
}
