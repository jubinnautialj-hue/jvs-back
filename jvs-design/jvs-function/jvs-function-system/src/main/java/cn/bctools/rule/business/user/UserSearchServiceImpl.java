package cn.bctools.rule.business.user;


import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author czy
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "搜索用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 43,
        explain = "根据用户名搜索多个用户对象信息"
)
public class UserSearchServiceImpl implements BaseCustomFunctionInterface<UserSearchDto> {

    @Override
    public Object execute(UserSearchDto a, Map<String, Object> params) {
        return Arrays.stream(a.getSearchUser().split(","))
                .map(e -> {
                    UserSearchDto dto = new UserSearchDto().setType(a.getType()).setSearchUser(e);
                    SearchUserDto searchUserDto = new SearchUserDto();
                    switch (dto.getType()) {
                        case Email:
                            searchUserDto.setEmail(dto.getSearchUser());
                            break;
                        case Phone:
                            searchUserDto.setPhone(dto.getSearchUser());
                            break;
                        case Account:
                            searchUserDto.setAccountName(dto.getSearchUser());
                            break;
                    }
                    List<UserDto> userDtos = AuthorityManagementUtils.userSearch(searchUserDto);
                    if (ObjectNull.isNotNull(userDtos)) {
                        List<UserDto> userById = userDtos.stream().peek(b -> b.setPassword(null)).collect(Collectors.toList());
                        return BeanCopyUtil.copys(userById, UserDto.class);
                    } else {
                        return new ArrayList<>();
                    }
                }).flatMap(Collection::stream).collect(Collectors.toList());
    }
}

