package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.LoginLog;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.mapper.LoginLogMapper;
import cn.bctools.auth.mapper.OauthOtherMapper;
import cn.bctools.auth.service.LoginLogService;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {
    OauthOtherMapper oauthOtherMapper;

    @Override
    public LoginLog getLastLoginInfo(String userId) {
        return this.baseMapper.selectOne(Wrappers.<LoginLog>lambdaQuery()
                .eq(LoginLog::getUserId, userId)
                //排除刷新和内部跳转
                .notIn(LoginLog::getLoginType, "refresh_token", "inside")
                .orderByDesc(LoginLog::getOperateTime)
                .last("limit 1"));
    }

    @Override
    public void loginSuccessful(UserDto userDto) {
        saveLog(userDto, true);
    }

    public void saveLog(UserDto userDto, boolean status) {
        //记录日志
        LoginLog copy = BeanCopyUtil.copy(userDto, LoginLog.class);
        //转换登录类型
        OauthOther oauthOther = oauthOtherMapper.selectOne(new LambdaQueryWrapper<OauthOther>()
                .select(OauthOther::getName)
                .eq(OauthOther::getType, copy.getLoginType()));
        if (ObjectNull.isNotNull(oauthOther)) {
            copy.setLoginType(oauthOther.getName());
        }
        //添加渠道信息
        copy.setRemark(JSONObject.toJSONString(Dict.create().set("ch", userDto.getCh())));
        copy.setId(null);
        copy.setOperateTime(LocalDateTime.now());
        copy.setStatus(status).setUserId(userDto.getId());
        copy.setTenantId(userDto.getTenant().getId());
        copy.setTenantShortName(userDto.getTenant().getShortName());
        copy.setTenantName(userDto.getTenant().getShortName());
        copy.setDevice(getDeviceInfo(userDto.getUserAgent()));
        save(copy);
    }

    private static String pattern = "^Mozilla/\\d\\.\\d\\s+\\(+.+?\\)";
    private static String pattern2 = "\\(+.+?\\)";
    private static Pattern r = Pattern.compile(pattern);
    private static Pattern r2 = Pattern.compile(pattern2);

    @SneakyThrows
    private static String getDeviceInfo(String userAgent) {
        Matcher m = r.matcher(userAgent);
        String result = null;
        if (m.find()) {
            result = m.group(0);
        }
        if (ObjectNull.isNull(result)) {
            throw new IOException("请不要使用模拟器");
        }
        Matcher m2 = r2.matcher(result);
        if (m2.find()) {
            result = m2.group(0);
        }
        result = result.replace("(", "");
        result = result.replace(")", "");
        return filterDeviceInfo(result);
    }

    public static String filterDeviceInfo(String result) {
        if (StringUtils.isBlank(result)) {
            return null;
        }
        result = result.replace(" U;", "");
        result = result.replace(" zh-cn;", "");
        return result;
    }

}
