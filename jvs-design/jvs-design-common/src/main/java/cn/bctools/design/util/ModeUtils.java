package cn.bctools.design.util;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.oauth2.dto.CustomUser;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 轻应用模式（开发、测试、正式）
 */
public class ModeUtils {

    private ModeUtils() {
    }

    private static final String MODE_CACHE_KEY = "mode:user";

    /**
     * 使用的模式（开发、测试、正式）
     */
    private static final String KEY_MODE = "app_use_mode";

    private static final RedisUtils REDIS_UTILS = SpringContextUtil.getBean(RedisUtils.class);
    private static final OAuth2AuthorizationService authorizationService = SpringContextUtil.getBean(OAuth2AuthorizationService.class);
    private static final JvsSystemConfig jvsSystemConfig = SpringContextUtil.getBean(JvsSystemConfig.class);

    /**
     * 获取模式缓存key
     *
     * @param token 用户token
     * @return
     */
    private static final String getModeCacheKey(String token) {
        return SysConstant.redisKey(MODE_CACHE_KEY, token);
    }

    /**
     * 获取token
     *
     * @return token
     */
    private static String getCurrentToken() {
        // 以token为key，若token变了，前端调一次切换模式接口，刷新模式缓存
        String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (ObjectNull.isNull(authorization)) {
            return null;
        }
        return authorization.replaceFirst("Bearer ", "");
    }

    /**
     * 获取真实用户
     *
     * @return
     */
    public static UserDto getRealUser() {
        // 有模拟用户，根据token获取登录用户
        if (whetherAnalogUser()) {
            String token = getCurrentToken();
            if (ObjectNull.isNull(token)) {
                throw new BusinessException("用户未登录");
            }
            OAuth2Authorization authorization = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
            CustomUser user = authorization.getAttribute("user");
            return user.getUserDto();
        } else {
            // 无模拟用户，直接获取登录用户
            return UserCurrentUtils.getCurrentUser();
        }
    }

    /**
     * 缓存模式
     *
     * @param mode 模式
     */
    public static void setModeCache(SwitchModeDto mode) {
        // 以token为key，若token变了，前端调一次切换模式接口，刷新模式缓存
        String token = getCurrentToken();
        if (ObjectNull.isNull(token)) {
            return;
        }
        String key = getModeCacheKey(token);
        REDIS_UTILS.set(key, mode, Duration.ofDays(5));
    }

    /**
     * 设置模式上下文
     */
    public static void setMode() {
        // 从缓存中获取模式信息，若缓存不存在，则默认正式模式
        String token = getCurrentToken();
        if (ObjectNull.isNull(token)) {
            return;
        }
        String key = getModeCacheKey(token);
        SwitchModeDto mode = Optional.ofNullable((SwitchModeDto) REDIS_UTILS.get(key))
                .orElseGet(() -> new SwitchModeDto().setMode(AppVersionTypeEnum.valueOf(jvsSystemConfig.getDesignDefaultMode())));
        // 正式模式不能模拟用户
        if (AppVersionTypeEnum.GA.equals(mode.getMode())) {
            mode.setAnalogUser(null);
        }
        // 将模式放到上下文
        setSwitchModel(mode);

        // 设置模拟用户
        UserDto analogUser = mode.getAnalogUser();
        // 无模拟用户不用设置
        if (ObjectNull.isNull(analogUser) || ObjectNull.isNull(analogUser.getId())) {
            return;
        }
        // 将模拟用户设置到上下文
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        Object principal = authentication.getPrincipal();
        if (UserCurrentUtils.UN_LOGIN.equals(principal)) {
            throw new BusinessException("用户未登录");
        }
        CustomUser customUser = new CustomUser();
        customUser.setUserDto(analogUser);
        customUser.setRoles(analogUser.getRoleIds());
        UsernamePasswordAuthenticationToken currentAuthentication = new UsernamePasswordAuthenticationToken(customUser, "", Collections.emptyList());
        securityContext.setAuthentication(currentAuthentication);
    }


    public static void setSwitchModel(SwitchModeDto mode) {
        SystemThreadLocal.set(KEY_MODE, mode);
    }

    public static SwitchModeDto getSwitchMode() {
        return Optional.ofNullable((SwitchModeDto) SystemThreadLocal.get(KEY_MODE)).orElseGet(SwitchModeDto::new);
    }

    /**
     * 获取模式
     *
     * @return
     */
    public static AppVersionTypeEnum getMode() {
        return getSwitchMode().getMode();
    }

    /**
     * 是否使用模拟用户
     *
     * @return true-使用模拟用户，false=-不使用模拟用户
     */
    public static Boolean whetherAnalogUser() {
        return Optional.ofNullable(getSwitchMode().getAnalogUser())
                .map(e -> ObjectNull.isNotNull(e.getId()))
                .orElse(Boolean.FALSE);
    }
}
