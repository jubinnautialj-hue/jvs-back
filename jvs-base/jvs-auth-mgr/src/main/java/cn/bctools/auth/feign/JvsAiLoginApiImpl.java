package cn.bctools.auth.feign;

import cn.bctools.ai.api.JvsAILoginCheckApi;
import cn.bctools.ai.dto.AiUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jvs
 */
@RequestMapping
@RestController
@AllArgsConstructor
public class JvsAiLoginApiImpl implements JvsAILoginCheckApi {

    @Override
    @GetMapping(PREFIX)
    public R<AiUserDto> check(String authorization) {
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        AiUserDto aiUserDto = new AiUserDto().setId(currentUser.getId()).setAccountName(currentUser.getAccountName()).setEmail(currentUser.getEmail()).setRealName(currentUser.getRealName()).setHeadImg(currentUser.getHeadImg());
        return R.ok(aiUserDto);
    }

}
