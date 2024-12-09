package cn.bctools.auth.api.api;

import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.common.utils.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环境变量的api，
 * 轻应用中和业务模块中或公式中需要获取系统中的环境变量时，将其配置到后台即可所有服务中获取
 *
 * @author xc
 */
@FeignClient(value = "jvs-auth-mgr", contextId = "environment-variable")
public interface EnvironmentVariableApi {

    /**
     * The constant PREFIX.
     */
    String PREFIX = "/api/environment/variable";

    /**
     * 通过环境变量key获取值
     *
     * @param key  the key
     * @param mode the mode
     * @return by key
     */
    @ApiOperation("通过key获取值")
    @GetMapping(PREFIX + "/{key}")
    R<EnvironmentVariableDto> getByKey(@PathVariable("key") String key, @RequestParam("mode") ModeTypeEnum mode);

    /**
     * 查询所有环境变量
     *
     * @param mode the mode
     * @return all
     */
    @ApiOperation("查询所有环境变量")
    @GetMapping(PREFIX + "/all")
    R<List<EnvironmentVariableDto>> getAll(@RequestParam("mode") ModeTypeEnum mode);

    /**
     * Save r.
     *
     * @param variableDto the variable dto
     * @return the r
     */
    @ApiOperation("保存环境变量")
    @GetMapping(PREFIX + "/save")
    R save(@RequestBody EnvironmentVariableDto variableDto);


    /**
     * Save r.
     *
     * @param variableDto the variable dto
     * @return the r
     */
    @ApiOperation("覆盖环境变量")
    @PostMapping(PREFIX + "/cover")
    R cover(@RequestBody List<EnvironmentVariableDto> variableDto);

}
