package cn.bctools.design.external;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.util.RoleUtils;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.external.service.ExternalPageService;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author zhuxiaokang
 */
@Deprecated
@Api(tags = "[external]外部页面接入")
@RestController
@RequestMapping("/base/external/page")
@AllArgsConstructor
public class ExternalPageBaseController {
    private final ExternalPageService service;

    @ApiOperation("获取权限")
    @GetMapping("/{appId}/permissions")
    public R<Set<String>> detail(@PathVariable String appId, String url) {
        ExternalPage externalPage = Optional.ofNullable(service.getOne(Wrappers.<ExternalPage>lambdaQuery()
                .eq(ExternalPage::getJvsAppId, appId)
                .eq(ExternalPage::getUrl, url).select(ExternalPage::getPermissions))).orElseGet(ExternalPage::new);
        if (ObjectNull.isNull(externalPage)) {
            return R.ok(Collections.emptySet());
        }
        List<DesignRole> permissions = RoleUtils.filterDesignRole(JSONArray.parseArray(JSONObject.toJSONString(externalPage.getPermissions()), DesignRole.class));
        return R.ok(RoleUtils.getOperation(permissions));
    }
}
