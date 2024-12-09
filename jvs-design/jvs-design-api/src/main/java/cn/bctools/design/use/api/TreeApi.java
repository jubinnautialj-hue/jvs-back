package cn.bctools.design.use.api;

import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Administrator
 */
@Component
@FeignClient(value = "jvs-design-mgr", contextId = "tree")
public interface TreeApi {

    String PREFIX = "/api/tree/";

    /**
     * 唯一编码
     *
     * @param uniqueName
     * @return
     */
    @ApiOperation("唯一编码")
    @GetMapping(PREFIX + "/{uniqueName}")
    R<String> tree(@PathVariable("uniqueName") String uniqueName);

    /**
     * 根据树标识查询字典类型
     *
     * @param uniqueNames 查询的树节点唯一标识
     * @return 查询的树节点值
     */
    @PostMapping(PREFIX + "/query/by/ids")
    R<List<String>> getByIds(@ApiParam("查询的树节点唯一标识") @RequestBody List<String> uniqueNames);

}
