package cn.bctools.message.push.utils;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.auth.api.dto.UserExtensionDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author czy
 */
@Slf4j
@Component
@AllArgsConstructor
public class JvsUserComponent {
    private final AuthUserServiceApi authUserServiceApi;
    private final UserExtensionServiceApi userExtensionServiceApi;

    public static final String DEFAULT_TENANT_ID = "-1";
    public static final String DEFAULT_ID = "-1";
    public static final String WECHAT_MP = "WECHAT_MP";
    public static final String DING = "Ding";

    public static final String DING_CONFIG = "userid";


    public List<UserDto> findByIds(List<ReceiversDto> list) {
        List<String> userIds = list.stream().map(ReceiversDto::getUserId).distinct().collect(Collectors.toList());
        List<UserDto> data = authUserServiceApi.getByIds(userIds).getData();
        if (CollectionUtil.isEmpty(data)) {
            log.info("未找到用户【{}】数据", userIds);
        }
        return data;
    }

    /**
     * 站内信接收人配置
     *
     * @param receivers 接收人
     * @param tenantId
     */
    public void setInsideNoticeConfig(List<ReceiversDto> receivers, String tenantId) {
        List<UserDto> userDtoList = this.findByIds(receivers);
        if (ObjectNull.isNotNull(userDtoList)) {
            userDtoList.forEach(e -> e.setTenantId(tenantId));
            Map<String, UserDto> userDtoMap = userDtoList.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
            receivers.forEach(e -> {
                UserDto userDto = userDtoMap.get(e.getUserId());
                e.setUserName(userDto.getRealName()).setReceiverConfig(userDto.getId());
            });
        }
    }

    /**
     * 设置微信公众号 配置
     *
     * @param receivers 接收人
     */
    public void setWxMpConfig(List<ReceiversDto> receivers) {
        List<UserDto> userDtoList = this.findByIds(receivers);
        this.setTenantId(userDtoList);
        Map<String, UserDto> userDtoMap = userDtoList.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        receivers.forEach(e -> {
            UserDto userDto = userDtoMap.get(e.getUserId());
            e.setUserName(userDto.getRealName());
            if (ObjectNull.isNotNull(userDto.getExceptions()) && userDto.getExceptions().containsKey(WECHAT_MP)) {
                JSONObject wechatmp = JSONUtil.parseObj(userDto.getExceptions().get(WECHAT_MP));
                e.setReceiverConfig(wechatmp.getStr("openId"));
            }
            log.info("微信公众号：模板：用户：{} ,属性：{},转换后: {}", userDto.getRealName(), userDto.getExceptions(), e.toString());
        });
    }

    /**
     * 设置邮件 配置
     *
     * @param receivers 接收人
     */
    public void setEmailConfig(List<ReceiversDto> receivers) {
        List<UserDto> userDtoList = this.findByIds(receivers);
        this.setTenantId(userDtoList);
        Map<String, UserDto> userDtoMap = userDtoList.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        receivers.forEach(e -> {
            UserDto userDto = userDtoMap.get(e.getUserId());
            e.setUserName(userDto.getRealName()).setReceiverConfig(userDto.getEmail());
        });
    }

    /**
     * 设置短信 配置
     *
     * @param receivers 接收人
     */
    public void setAliSmsConfig(List<ReceiversDto> receivers) {
        List<UserDto> userDtoList = this.findByIds(receivers);
        this.setTenantId(userDtoList);
        Map<String, UserDto> userDtoMap = userDtoList.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        receivers.forEach(e -> {
            UserDto userDto = userDtoMap.get(e.getUserId());
            e.setUserName(userDto.getRealName()).setReceiverConfig(userDto.getPhone());
        });
    }

    /**
     * 设置钉钉 配置
     *
     * @param receivers 接收人
     */
    public void setDingCropConfig(List<ReceiversDto> receivers) {
        List<String> userIds = receivers.stream().map(ReceiversDto::getUserId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, Map<String, Object>> configMap = userExtensionServiceApi.query(userIds, DING).getData().stream().collect(Collectors.toMap(UserExtensionDto::getUserId, UserExtensionDto::getExtension));
        receivers.forEach(e -> e.setReceiverConfig(StrUtil.toStringOrNull(configMap.get(e.getUserId()).get(DING_CONFIG))));
    }

    /**
     * 设置租户
     *
     * @param userDtoList 查询的接收人数据
     */
    public void setTenantId(List<UserDto> userDtoList) {
        Optional<String> first = Optional.ofNullable(userDtoList).orElse(Collections.emptyList()).stream().map(UserDto::getTenantId).findFirst();
        first.ifPresent(TenantContextHolder::setTenantId);
    }
}
