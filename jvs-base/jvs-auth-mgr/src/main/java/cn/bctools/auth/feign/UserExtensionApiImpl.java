package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.service.OauthOtherService;
import cn.bctools.auth.service.UserExtensionService;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Slf4j
@RestController
@AllArgsConstructor
@Api(tags = "[Feign]用户扩展信息表")
public class UserExtensionApiImpl implements UserExtensionServiceApi {

    private final UserExtensionService userExtensionService;
    private final OauthOtherService oauthOtherService;

    @Override
    public R<List<UserExtensionDto>> query(List<String> userIds, String type) {
        List<UserExtension> list = userExtensionService.list(new LambdaQueryWrapper<UserExtension>()
                .in(UserExtension::getUserId, userIds).eq(UserExtension::getType, type));
        if (CollectionUtils.isEmpty(list)) {
            return R.ok();
        }
        List<UserExtensionDto> userExtensions = BeanCopyUtil.copys(list, UserExtensionDto.class);
        return R.ok(userExtensions);
    }

    @Override
    public R<List<UserExtensionDto>> queryPost(List<String> userIds, String type) {
        return query(userIds, type);
    }

    @Override
    public R<Map<String, String>> field() {
        HashMap<String, String> body = new HashMap<>();
        oauthOtherService.list().stream().filter(e -> ObjectNull.isNotNull(e.getExtensionJson())).forEach(e -> body.putAll(e.getExtensionJson()));
        return R.ok(body);
    }
}
