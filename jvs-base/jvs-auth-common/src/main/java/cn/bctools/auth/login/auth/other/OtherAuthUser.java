package cn.bctools.auth.login.auth.other;

import cn.bctools.auth.entity.enums.SexTypeEnum;
import cn.bctools.common.utils.ObjectNull;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.*;
import me.zhyd.oauth.model.AuthUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtherAuthUser extends AuthUser implements Serializable {

    /**
     * 手机号
     */
    String phone;
    /**
     * 帐号
     */
    String account;
    /**
     * 用户所在部门 id
     */
    Object deptIds;
    /**
     * 是否启用
     */
    Boolean enable;

    public List<String> getDeptIds() {
        List<String> objects = new ArrayList<>();
        if (ObjectNull.isNotNull(deptIds)) {
            if (JSON.isValidArray(JSONObject.toJSONString(deptIds))) {
                JSONArray.parseArray(JSONObject.toJSONString(deptIds)).forEach(e -> objects.add(e.toString()));
            }
        }
        return objects;
    }

    SexTypeEnum sex;

}
