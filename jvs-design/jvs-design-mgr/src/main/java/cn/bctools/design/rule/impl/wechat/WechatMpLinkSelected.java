package cn.bctools.design.rule.impl.wechat;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.message.push.dto.vo.WxMpTemplateVo;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 根据类型确定选择模板
 *
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class WechatMpLinkSelected implements LinkFieldSelected<String> {

    WechatOfficialAccountApi wechatOfficialAccountApi;

    @Override
    public Object link(String id, String body) {

        List<WxMpTemplateVo> allPrivateTemplate = wechatOfficialAccountApi.getAllPrivateTemplate(false);
        if (ObjectNull.isNull(allPrivateTemplate)) {
            return new ArrayList<>();
        }
        String content = allPrivateTemplate
                .stream()
                .filter(e -> e.getTemplateId().equals(id))
                .findFirst()
                .map(e -> e.getContent())
                .get();

        Pattern pattern = Pattern.compile(WXREGEX);
        //自旋进行最小匹配，直到无法匹配
        Matcher matcher = pattern.matcher(content);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group();
            String substring = group.substring(2, group.length() - 7);
            list.add(substring);
        }

        fields field = fields.valueOf(body);
        //如果是不是最新的，则直接返回缓存数据
        switch (field) {
            case body:
                return list.stream()
                        .map(e -> new ParameterOption<String>().setLabel(e).setValue(e))
                        .collect(Collectors.toList());
            default:
                break;
        }
        throw new BusinessException("没有找到关联字段");
    }

    public static final String WXREGEX = "\\{\\{([a-zA-Z0-9]*)\\.DATA}}";

    @Override
    public List<String> fields() {
        return Arrays.stream(fields.values()).map(Enum::name).collect(Collectors.toList());
    }

    enum fields {
        /**
         * 微信的属性值 body
         */
        body;
    }

}
