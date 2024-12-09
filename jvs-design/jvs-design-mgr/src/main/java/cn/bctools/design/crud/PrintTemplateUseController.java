package cn.bctools.design.crud;

import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.PrintTemplate;
import cn.bctools.design.crud.service.PrintTemplateService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
@Api(tags = "打印模板使用")
@RestController
@RequestMapping("/app/use/{appId}/print/template")
@AllArgsConstructor
public class PrintTemplateUseController {

    private final PrintTemplateService service;

    @ApiOperation("获取设计所有可用打印模板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "designId", value = "设计id", required = true),
    })
    @GetMapping("/{designId}/available")
    public R<List<PrintTemplate>> getDesignAvailableAll(@PathVariable String designId, @PathVariable String appId) {
        return R.ok(service.getDesignAvailableAll(appId, designId));
    }

    @ApiOperation("查询打印模板信息")
    @GetMapping("/info/{id}")
    public R<PrintTemplate> info(@PathVariable String id, @PathVariable String appId) {
        return R.ok(service.getOne(Wrappers.query(new PrintTemplate().setId(id).setJvsAppId(appId))));
    }

    @SneakyThrows
    @ApiOperation("文件模板打印预览")
    @GetMapping("/file/preview/{id}/{dataModelId}/{designId}/{dataId}")
    public void filePreview(@PathVariable String id, @PathVariable String dataModelId, @PathVariable String dataId, @PathVariable String designId, @PathVariable String appId) {
        service.filePreview(appId, id, dataModelId, designId, dataId);
    }

}
