package cn.bctools.auth.controller;

import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.service.OauthOtherService;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.utils.BeanToMapUtils;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author guojing
 */
@Api(tags = "三方应用登陆对接")
@RestController
@RequestMapping("/other/oauth")
@AllArgsConstructor
public class OauthOtherController {

    @Resource
    JvsSystemConfig systemConfig;
    @Resource
    SysConfigsService configService;
    @Resource
    OauthOtherService service;

    @Log
    @ApiOperation("获取有那些终端")
    @GetMapping("/clients")
    R clients() {
        List<Map<String, Object>> list = systemConfig.getService().stream()
                .map(e -> {
                    Map<String, Object> stringObjectMap = BeanToMapUtils.beanToMap(e);
                    stringObjectMap.put("label", e.getName().msg);
                    SysApplyConfig config = configService.getConfig(e.getName());
                    if (ObjectNull.isNull(config.getDomainName())) {
                        return null;
                    }
                    if (systemConfig.getMultiTenantMode()) {
                        stringObjectMap.put("url", "http://" + config.getDomainName() + "." + systemConfig.getDomain() + "/auth/just/callback");
                    } else {
                        stringObjectMap.put("url", "http://" + systemConfig.getDomain() + ":" + e.getPort() + "/auth/just/callback");
                    }
                    return stringObjectMap;
                }).filter(Objects::nonNull).collect(Collectors.toList());

        return R.ok(list);
    }

    @Log
    @GetMapping("/page")
    R page(Page<OauthOther> page) {
        page = service.page(page);
        return R.ok(page);
    }

    @Log
    @PostMapping
    R save(@RequestBody OauthOther oauthOther) {
        service.save(oauthOther);
        return R.ok();
    }

    @Log
    @PutMapping
    R put(@RequestBody OauthOther oauthOther) {
        service.updateById(oauthOther);
        return R.ok();
    }

    @Log
    @DeleteMapping("/{id}")
    R delete(@PathVariable("id") String id) {
        service.removeById(id);
        return R.ok();
    }

}
