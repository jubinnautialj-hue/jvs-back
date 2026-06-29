package cn.bctools.im.repository;

import cn.bctools.im.entity.arangodb.ImFriend;
import com.arangodb.springframework.annotation.Query;
import com.arangodb.springframework.repository.ArangoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: Arangodb数据库，friends文档集数据处理
 */
@ConditionalOnProperty(name = "social", havingValue = "arangodb", matchIfMissing = true)
public interface ImFriendRepository extends ArangoRepository<ImFriend, String> {

    /**
     * 查询目标用户好友
     * @param from 目标用户document id
     * @return
     */
    @Query("FOR friend IN friends FILTER friend._from==@from RETURN friend")
    List<ImFriend> getUserFriends(@Param("from") String from);
}
