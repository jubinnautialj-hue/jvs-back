package cn.bctools.im.entity.arangodb;

import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * @Author: ZhuXiaoKang
 * @Description: Arangodb数据库存储的用户信息
 */
@Data
@Document("user")
public class ImUser {

    /**
     * @Id 对应数据库中的_key
     */
    @Id
    private String key;

    /**
     * @ArangoId 对应数据库中的_id
     */
    @ArangoId
    private String id;

    /**
     * 真名
     */
    private String realName;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 租户id
     */
    private String tenantId;
}
