package cn.bctools.im.entity.arangodb;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * @Author: ZhuXiaoKang
 * @Description: Arangodb数据库存储的用户好友
 */
@Data
@Edge("friends")
public class ImFriend {

    /**
     * @Id 对应数据库中的_key
     */
    @Id
    private String key;

    /**
     * @From 对应数据库中的_from
     *
     * 目标用户
     */
    @From
    private ImUser from;

    /**
     * @To 对应数据库中的_to
     *
     * 指向用户
     */
    @To
    private ImUser to;

}
