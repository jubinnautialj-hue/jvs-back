package cn.bctools.auth.login.dto;

import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserExtension;
import cn.bctools.auth.entity.UserTenant;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhuxiaokang
 * 同步用户
 */
@Data
@Accessors(chain = true)
public class SyncUserDto {

    private List<User> users = Collections.synchronizedList(new ArrayList<>());
    private List<UserTenant> userTenants = Collections.synchronizedList(new ArrayList<>());
    private List<UserExtension> userExtensions = Collections.synchronizedList(new ArrayList<>());
    List<? extends Dept> list = Collections.synchronizedList(new ArrayList<>());
    /**
     * 成功条数
     */
    private int success;
    /**
     * 失败条数
     */
    private int fail;
}
