package cn.bctools.design.use.api;

import cn.bctools.common.utils.R;
import cn.bctools.design.use.api.dto.DataModelDto;
import cn.bctools.design.use.api.dto.ModeDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The interface App api.
 *
 * @author Administrator
 */
@Component
@FeignClient(value = "jvs-design-mgr", contextId = "useapp")
public interface AppApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/jvsdesign/menu";

    /**
     * 根据模式查询 获取应用
     *
     * @param mode 模式信息，  开发模式，测试模式，正式模式
     * @return 返回模型信息 r
     */
    @GetMapping(PREFIX + "/base/app")
    R<List<DataModelDto>> apps(@RequestParam("mode") String mode);

    /**
     * 检查这个应用是否存在
     * @param mode
     * @param appCode
     * @return
     */
    @GetMapping(PREFIX + "/base/mode/app")
    R<DataModelDto> apps(@RequestParam("mode") String mode, @RequestParam("appCode") String appCode);

    /**
     * 获取模式
     *
     * @return r
     */
    @GetMapping(PREFIX + "/base/mode")
    R<List<ModeDto>> mode();

    /**
     * 低代码租户数据存储大小
     *
     * @return r
     */
    @ApiOperation("低代码租户数据存储大小")
    @GetMapping(PREFIX + "/data/size")
    R<Long> dataSize();

}
