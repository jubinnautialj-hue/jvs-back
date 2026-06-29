package cn.bctools.im.service.impl;

import cn.bctools.im.constants.ArangoConstants;
import cn.bctools.im.entity.arangodb.ImFriend;
import cn.bctools.im.repository.ImFriendRepository;
import cn.bctools.im.service.ImFriendService;
import com.arangodb.springframework.core.ArangoOperations;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM好友服务实现
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
@Slf4j
@Service
@AllArgsConstructor
public class ImFriendServiceImpl implements ImFriendService {

    private final ImFriendRepository friendRepository;
    private final ArangoOperations arangoTemplate;

    @Override
    public List<ImFriend> getUserFriends(String tenantId, String userId) {
        // 如果集合不存在，则自动创建。
        arangoTemplate.collection(ImFriend.class);
        try {
            String form = ArangoConstants.buildUserId(tenantId, userId);
            return friendRepository.getUserFriends(form);
        } catch (Exception e) {
            log.error("arangoDB获取好友失败:{}", e);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveAll(List<ImFriend> imFriends) {
        try {
            friendRepository.saveAll(imFriends);
        } catch (Exception e) {
            log.error("保存好友信息失败：{}", e);
        }

    }
}
