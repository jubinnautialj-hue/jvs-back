package cn.bctools.design.crud.expression;

import cn.bctools.auth.api.api.UserExtensionServiceApi;
import cn.bctools.design.expression.EnvConstant;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.function.enums.SysParamEnums;
import cn.bctools.function.handler.IJvsParam;
import cn.bctools.function.handler.JvsExpression;
import cn.bctools.word.utils.WordVariableReplaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jvs
 * 表单文件打印模板系统字段
 */
@Slf4j
@Order(-2)
@Component
@JvsExpression(groupName = "系统字段", useCase = {EnvConstant.PRINT_SYS_ITEM_VALUE})
public class PrintSysItemValue implements IJvsParam<ElementVo> {
    UserExtensionServiceApi extensionServiceApi;

    private static final String PREFIX = "SYS";
    public static final String USER_EXTENSION = "UserExtension";

    public PrintSysItemValue(UserExtensionServiceApi extensionServiceApi) {
        this.extensionServiceApi = extensionServiceApi;
    }

    @Override
    public Object get(String paramName, Map<String, Object> data) {
        return null;
    }

    @Override
    public List<ElementVo> getAllElements() {
        List<ElementVo> allParams = Arrays.stream(SysParamEnums.values()).map(this::paramEnums2Vo).collect(Collectors.toList());
        Map<String, String> data = extensionServiceApi.field().getData();
        data.keySet().forEach(e -> {
            allParams.add(new ElementVo()
                    .setId(WordVariableReplaceUtil.buildVariableKey(USER_EXTENSION + data.get(e)))
                    .setName("扩展字段" + e)
                    .setInfo("扩展字段" + data.get(e))
                    .setShortName(e)
                    .setInParamTypes(new ArrayList<>())
                    .setJvsParamType(JvsParamType.any));
        });
        return allParams;
    }

    /**
     * 系统变量转表达式元素类
     *
     * @param param 系统变量美剧
     * @return 表达式元素实体类
     */
    private ElementVo paramEnums2Vo(SysParamEnums param) {
        String id = PREFIX + param.getId();
        if (SysParamEnums.HeadImg.equals(param)) {
            id = WordVariableReplaceUtil.buildVariableKey(WordVariableReplaceUtil.DataType.IMAGE, id);
        } else {
            id = WordVariableReplaceUtil.buildVariableKey(id);
        }
        return new ElementVo()
                .setId(id)
                .setName(param.getName())
                .setInfo(param.getInfo())
                .setShortName(param.getInfo())
                .setInParamTypes(new ArrayList<>())
                .setJvsParamType(param.getJvsParamType());
    }
}
