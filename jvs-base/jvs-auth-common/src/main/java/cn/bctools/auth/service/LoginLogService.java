package cn.bctools.auth.service;

import cn.bctools.auth.entity.LoginLog;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cache.annotation.CacheEvict;

/**
 * @author dynamic-maven
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     * 获取上一次登录信息
     *
     * @param userId 用户Id
     * @return 用户登录日志信息
     */
    LoginLog getLastLoginInfo(String userId);

    /**
     * 登陆成功
     *
     * @param userDto
     */
    @CacheEvict(value = SysConstant.CACHE_USER_PREFIX, allEntries = true)
    void loginSuccessful(UserDto userDto);


}
