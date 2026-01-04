package cn.bctools.design.data.fields.impl.extension;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.data.fields.DataFieldHandler;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.MultipleHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.data.fields.impl.IMultipleTypeHandler;
import cn.bctools.design.data.fields.impl.ISelectorDataHandler;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单字段: 用户选择
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
@DesignField(value = "用户选择", type = DataFieldType.user)
@AllArgsConstructor
public class UserFieldHandler extends IMultipleTypeHandler implements IDataFieldHandler<MultipleHtml>, ISelectorDataHandler {


    AuthUserServiceApi userApi;
    RedisUtils redisUtils;

    static final List<String> USER_FIELD_LIST = new ArrayList<>();

    static {
        USER_FIELD_LIST.add(Get.name(UserDto::getRealName));
    }

    private final Cache<String, String> userNameMapCache = CacheUtil.newTimedCache(1000 * 60 * 10);


    @Override
    public Object getEchoValue(MultipleHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        if (ObjectNull.isNull(data)) {
            return "";
        }
        List<String> userIds;
        if (data instanceof List) {
            userIds = (List<String>) data;
        } else {
            userIds = new ArrayList<>();
            userIds.add(data.toString());
        }
        //如果全部都是空，则直接返回
        if (userIds.stream().allMatch(ObjectNull::isNull)) {
            return "";
        }
        
        // 优先使用ThreadLocal预加载缓存，避免远程API调用
        Map<String, Object> preloadedUserCache = (Map<String, Object>) SystemThreadLocal.get("USER_NAME_MAP_CACHE");
        boolean usePreloadCache = ObjectNull.isNotNull(preloadedUserCache) && !preloadedUserCache.isEmpty();
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> userMap = new HashMap<>();
        List<String> queryUserId = new ArrayList<>();
        
        userIds.forEach(e -> {
            if (usePreloadCache && preloadedUserCache.containsKey(e)) {
                // 使用预加载缓存
                userMap.put(e, preloadedUserCache.get(e));
            } else {
                // 使用本地缓存
                String value = userNameMapCache.get(e, () -> "");
                if (ObjectNull.isNull(value)) {
                    queryUserId.add(e);
                }
                userMap.put(e, value);
            }
        });
        
        if (ObjectNull.isNotNull(queryUserId) && !queryUserId.isEmpty()) {
            // 需要远程查询，记录告警日志
            Map<String, String> queryMap = userApi.getBasicInfoById(queryUserId, USER_FIELD_LIST).getData().stream()
                .collect(Collectors.toMap(UserDto::getId, user -> StringUtils.defaultIfBlank(user.getRealName(), "")));
            queryMap.entrySet().forEach(e -> {
                userNameMapCache.put(e.getKey(), e.getValue());
                userMap.put(e.getKey(), e.getValue());
            });
            
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 100) {
                log.warn("[用户选择-Echo] 未找到预加载缓存，调用远程API耗时: {}ms，建议优化为批量预加载", duration);
            }
        }
        
        DataFieldHandler dataFieldHandler = SpringContextUtil.getBean(DataFieldHandler.class);
        boolean isMulti = ObjectNull.isNull(fieldDto.getMultiple()) ? false : fieldDto.getMultiple();
        return dataFieldHandler.joinFormItems(userMap, data, isMulti, false);
    }

    @Override
    public String getConversionKey(MultipleHtml dto, Object o, Map<String, Object> lineData, Map<String, Map<String, String>> cascaderFieldPathIdsMap, Map<String, List<Map<String, Object>>> generateCascaderList) {
        Map<String, String> orDefault = cascaderFieldPathIdsMap.getOrDefault(dto.getProp(), new LinkedHashMap<>());
        if (dto.getMultiple()) {
            o = Arrays.stream(o.toString().split(",")).map(String::trim).collect(Collectors.toList());
            Map<String, String> userMap = userApi.getByNames((List<String>) o).getData().stream().collect(Collectors.toMap(UserDto::getRealName, UserDto::getId));
            ((List<?>) o).removeIf(userMap::containsKey);
            if (!((List<?>) o).isEmpty()) {
                String collect = ((List<?>) o).stream().map(Object::toString).collect(Collectors.joining(","));
                if (!collect.isEmpty()) {
                    throw new BusinessException(collect + "用户不存在，导入失败");
                }
            }
            //判断是否存在这个用户
            orDefault.putAll(userMap);
        } else {
            try {
                Object finalO = o;
                String id = userApi.getByRealName(o.toString().trim()).getData().stream().filter(e -> e.getRealName().equals(finalO.toString().trim())).findAny().get().getId();
                orDefault.put(o.toString().trim(), id);
            } catch (Exception e) {
                throw new BusinessException(o.toString().trim() + "用户不存在，导入失败");
            }
        }
        cascaderFieldPathIdsMap.put(dto.getProp(), orDefault);
        return null;
    }

    @Override
    public Object getConversionKey(MultipleHtml dto, Object o, Map<String, Object> oldData) {
        if (dto.getMultiple()) {
            o = Arrays.stream(o.toString().split(",")).map(String::trim).collect(Collectors.toList());
            return userApi.getByNames((List<String>) o).getData().stream().map(UserDto::getId).collect(Collectors.toList());
        } else {
            return userApi.getByRealName(o.toString().trim()).getData().get(0).getId();
        }
    }

    @Override
    public Boolean whetherCoverValue() {
        return Boolean.FALSE;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                     "    \"prop\": \"" + field + "\",\n" +
                     "    \"type\": \"user\",\n" +
                     "    \"label\": \"" + name + "\",\n" +
                     "    \"span\": 24,\n" +
                     "    \"display\": true,\n" +
                     "    \"status\": \"\",\n" +
                     "    \"tips\": {\n" +
                     "        \"text\": \"\",\n" +
                     "        \"position\": \"right\"\n" +
                     "    },\n" +
                     "    \"multiple\": false,\n" +
                     "    \"allowinput\": false,\n" +
                     "    \"showFrom\": [\n" +
                     "        \"label\",\n" +
                     "        \"span\",\n" +
                     "        \"multiple\",\n" +
                     "        \"prop\",\n" +
                     "        \"sqlType\",\n" +
                     "        \"allowinput\",\n" +
                     "        \"disabled\",\n" +
                     "        \"isDefault\"\n" +
                     "    ],\n" +
                     "    \"rules\": [\n" +
                     "        {\n" +
                     "            \"required\": false,\n" +
                     "            \"message\": \"请选择用户\",\n" +
                     "            \"trigger\": \"change\"\n" +
                     "        }\n" +
                     "    ],\n" +
                     "    \"sqlType\": \"array\",\n" +
                     "    \"name\": \"" + DataFieldType.user.getDesc() + "\",\n" +
                     "    \"disabled\": false\n" +
                     "}";
        return JSONObject.parseObject(str);
    }
}
