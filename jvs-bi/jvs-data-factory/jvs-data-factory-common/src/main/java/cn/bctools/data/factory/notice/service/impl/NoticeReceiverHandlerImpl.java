package cn.bctools.data.factory.notice.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.notice.bo.ReceiverBo;
import cn.bctools.data.factory.notice.enums.ReceiverTypeEnum;
import cn.bctools.data.factory.notice.service.NoticeReceiverHandler;
import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知-获取接收人
 */
@Slf4j
@Component
@AllArgsConstructor
public class NoticeReceiverHandlerImpl implements NoticeReceiverHandler {

    private final AuthUserServiceApi authUserServiceApi;

    @Override
    public List<String> getUserIds(List<ReceiverBo> receiver) {
        List<UserDto> user = getUser(receiver);
        return user.stream().map(UserDto::getId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUser(List<ReceiverBo> receiver) {
        // 根据用户类型分组
        Map<ReceiverTypeEnum, List<String>> userGroup = receiver.stream()
                .collect(Collectors.groupingBy(ReceiverBo::getType, Collectors.mapping(ReceiverBo::getId, Collectors.toList())));
        SearchUserDto search = new SearchUserDto();
        for (Map.Entry<ReceiverTypeEnum, List<String>> e : userGroup.entrySet()) {
            switch (e.getKey()) {
                case user:
                    search.setUserIds(e.getValue());
                    break;
                case role:
                    search.setRoleIds(e.getValue());
                    break;
                case job:
                    search.setJobIds(e.getValue());
                    break;
                case dept:
                    search.setDeptIds(e.getValue());
                    break;
                case group:
                    search.setGroupIds(e.getValue());
                    break;
                default:
                    log.warn("消息通知，不支持的接收人类型");
                    break;
            }
        }
        return searchUser(search);
    }


    /**
     * @return 用户id集合
     */
    private List<UserDto> searchUser(SearchUserDto search) {
        if (CollectionUtil.isEmpty(search.getUserIds())
                && CollectionUtil.isEmpty(search.getJobIds())
                && CollectionUtil.isEmpty(search.getDeptIds())
                && CollectionUtil.isEmpty(search.getRoleIds())
                && CollectionUtil.isEmpty(search.getGroupIds())) {
            return Collections.emptyList();
        }
        return authUserServiceApi.userSearch(search).getData();
    }

}
