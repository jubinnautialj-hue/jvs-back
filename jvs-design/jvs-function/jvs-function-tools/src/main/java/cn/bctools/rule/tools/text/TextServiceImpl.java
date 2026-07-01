package cn.bctools.rule.tools.text;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.email.EmailUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.http.HtmlUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author jvs
 * @describe 文字替换
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "文字替换", group = RuleGroup.工具插件, test = true, returnType = ClassType.文本, testShowEnum = TestShowEnum.TEXT, order = 23,
//        iconUrl = "rule-youjian",
        explain = "文字替换。")
public class TextServiceImpl implements BaseCustomFunctionInterface<TextFunctionDto> {

    EmailUtils emailUtils;
    AuthTenantConfigServiceApi configServiceApi;
    AuthUserServiceApi authUserServiceApi;
    OssTemplate ossTemplate;

    @SneakyThrows
    @Override
    public Object execute(TextFunctionDto dto, Map<String, Object> params) {
        if (ObjectNull.isNotNull(dto.getText())) {
            dto.getText().keySet().forEach(e -> dto.setContent(dto.getContent().replaceAll("\\$\\{" + e + "}", ObjectNull.isNull(dto.getText().get(e)) ? "" : dto.getText().get(e))));
        }
        if (ObjectNull.isNotNull(dto.getClear())) {
            if (dto.getClear()) {
                return HtmlUtil.unescape(HtmlUtil.cleanHtmlTag(dto.getContent().toString()));
            }
        }
        return dto.getContent();
    }

}
