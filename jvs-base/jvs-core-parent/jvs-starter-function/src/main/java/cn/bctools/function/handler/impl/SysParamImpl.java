package cn.bctools.function.handler.impl;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.enums.SysParamEnums;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Order(-1)
@JvsExpression(groupName = "系统参数", prefix = "SYS")
public class SysParamImpl implements IJvsParam<ElementVo> {

    UserExtensionServiceApi extensionServiceApi;

    public static final String USER_EXTENSION = "UserExtension";

    public SysParamImpl(UserExtensionServiceApi extensionServiceApi) {
        this.extensionServiceApi = extensionServiceApi;
    }

    @Override
    public List<ElementVo> getAllElements() {
        List<ElementVo> allParams = Arrays.stream(SysParamEnums.values()).map(this::paramEnums2Vo).collect(Collectors.toList());
        Map<String, String> data = extensionServiceApi.field().getData();
        data.keySet().forEach(e -> {
            allParams.add(new ElementVo()
                    .setId(USER_EXTENSION + data.get(e))
                    .setName("扩展字段" + e)
                    .setInfo("扩展字段" + data.get(e))
                    .setShortName(e)
                    .setInParamTypes(new ArrayList<>())
                    .setJvsParamType(JvsParamType.any));
        });
        return allParams;
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        //判断是否是扩展参数
        if (paramName.startsWith(USER_EXTENSION)) {
            //直接返回扩展参数
            return Optional.ofNullable(UserCurrentUtils.getCurrentUser())
                    .map(UserDto::getExceptions)
                    .map(e -> e.get(paramName.replaceAll(USER_EXTENSION, "")))
                    .orElseGet(() -> "");
        }
        SysParamEnums sysParam = SysParamEnums.getById(paramName);
        if (Objects.isNull(sysParam)) {
            throw new BusinessException("未知的系统参数", paramName);
        }
        switch (sysParam) {
            case RealName:
                return UserCurrentUtils.getRealName();
            case UserId:
                return UserCurrentUtils.getUserId();
            case AccountName:
                return UserCurrentUtils.getAccountName();
            case Phone:
                return UserCurrentUtils.getCurrentUser().getPhone();
            case Email:
                return UserCurrentUtils.getCurrentUser().getEmail();
            case HeadImg:
                return UserCurrentUtils.getCurrentUser().getHeadImg();
            case DeptId:
                return UserCurrentUtils.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toList());
            case DeptName:
                return UserCurrentUtils.getDept().stream().map(DeptDto::getDeptName).collect(Collectors.toList());
            case DeptCode:
                return UserCurrentUtils.getDept().stream().map(DeptDto::getDeptCode).collect(Collectors.toList());
            case JobName:
                return UserCurrentUtils.getCurrentUser().getJobName();
            case UserRole:
                return UserCurrentUtils.getRole();
            case UserEmployeeNo:
                return UserCurrentUtils.getCurrentUser().getEmployeeNo();
            case TenantCode:
                return UserCurrentUtils.getCurrentUser().getTenantId();
            default:
                throw new BusinessException("暂不支持的系统参数", paramName);
        }
    }

    /**
     * 系统变量转表达式元素类
     *
     * @param param 系统变量美剧
     * @return 表达式元素实体类
     */
    private ElementVo paramEnums2Vo(SysParamEnums param) {
        return new ElementVo()
                .setId(param.getId())
                .setName(param.getName())
                .setInfo(param.getInfo())
                .setShortName(param.getInfo())
                .setInParamTypes(new ArrayList<>())
                .setJvsParamType(param.getJvsParamType());
    }

}
