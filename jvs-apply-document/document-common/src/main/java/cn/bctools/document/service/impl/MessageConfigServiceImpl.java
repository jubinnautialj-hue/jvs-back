package cn.bctools.document.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.MessageConfig;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.enums.MessagePushTypeEnum;
import cn.bctools.document.mapper.MessageConfigMapper;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.MessageConfigService;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Service
@AllArgsConstructor
@Slf4j
public class MessageConfigServiceImpl extends ServiceImpl<MessageConfigMapper, MessageConfig> implements MessageConfigService {
    private final AuthUserServiceApi authUserServiceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(List<MessageConfig> list, String id) {
        //这里直接删除原有的数据 然后重新入库
        this.remove(new LambdaQueryWrapper<MessageConfig>().eq(MessageConfig::getBusinessId, id));
        DcLibraryService dcLibraryService = SpringContextUtil.getBean(DcLibraryService.class);
        DcLibrary dcLibrary = dcLibraryService.getById(id);
        list = list.stream().peek(e ->
                        e.setBusinessId(id)
                                .setKnowledgeId(dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge) ? id : dcLibrary.getKnowledgeId())
                                .setPathId(dcLibrary.getPathId())
                                .setParentId(dcLibrary.getParentId())
                                .setType(dcLibrary.getType()))
                .collect(Collectors.toList());
        this.saveBatch(list);
    }


    @Override
    public void syncUserInfo(List<String> removeUserIds, DcLibrary dclLibrary) {
        if (!removeUserIds.isEmpty()) {
            LambdaQueryWrapper<MessageConfig> queryWrapper = new LambdaQueryWrapper<>();
            if (DcLibraryTypeEnum.isFile(dclLibrary.getType())) {
                queryWrapper.eq(MessageConfig::getBusinessId, dclLibrary.getId()).in(MessageConfig::getUserId, removeUserIds);
            } else {
                queryWrapper.in(MessageConfig::getUserId, removeUserIds)
                        .and(e -> e.apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", dclLibrary.getId()).or().eq(MessageConfig::getBusinessId, dclLibrary.getId()));
            }
            this.remove(queryWrapper);
        }
    }

    @Override
    public List<MessageConfig> getUser(String knowledgeId, MessagePushTypeEnum operatorType) {
        //获取此业务绑定的消息
        List<MessageConfig> list = this.list(new LambdaQueryWrapper<MessageConfig>()
                .eq(MessageConfig::getBusinessId, knowledgeId)
                .isNotNull(MessageConfig::getMessageId)
                .isNotNull(MessageConfig::getUserId)
                .eq(MessageConfig::getOperationType, operatorType.value))
                .stream().filter(e-> ObjectUtil.isNotEmpty(e.getMessageId())).collect(Collectors.toList());
        if (list.isEmpty()) {
            return list;
        }
        //获取用户信息
        //获取用户id
        list.stream().peek(e -> {
            SearchUserDto searchUserDto = new SearchUserDto();
            List<String> ids = Collections.singletonList(e.getUserId());
            switch (e.getDataAuthType()) {
                case group:
                    searchUserDto.setGroupIds(ids);
                    break;
                case role:
                    searchUserDto.setRoleIds(ids);
                    break;
                case dept:
                    searchUserDto.setDeptIds(ids);
                    break;
                case job:
                    searchUserDto.setJobIds(ids);
                    break;
                default:
            }
            if (!e.getDataAuthType().equals(DataAuthTypeEnum.user)) {
                List<String> data = authUserServiceApi.userSearch(searchUserDto).getData().parallelStream().map(UserDto::getId).collect(Collectors.toList());
                e.setUserIds(data);
            } else {
                e.setUserIds(ids);
            }
        }).collect(Collectors.toList());
        return list;
    }
}
