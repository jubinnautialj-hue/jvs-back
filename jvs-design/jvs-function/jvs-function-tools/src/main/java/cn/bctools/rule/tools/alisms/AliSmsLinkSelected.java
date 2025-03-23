package cn.bctools.rule.tools.alisms;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.redis.utils.RedisUtils;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 根据类型确定选择模板
 *
 * @author wl
 */
@Slf4j
@Service
@AllArgsConstructor
public class AliSmsLinkSelected implements LinkFieldSelected<String> {

    RedisUtils redisUtils;

    @Override
    public Object link(String id, String body) {
        fields field = fields.valueOf(body);
        String key = "message:push:SMS" + TenantContextHolder.getTenantId();
        //如果是不是最新的，则直接返回缓存数据
        //做字段解析
        if (field == fields.content) {
            AliSmsTemplateVo templateVo = ((List<AliSmsTemplateVo>) redisUtils.get(key))
                    .stream()
                    .filter(e -> e.getTemplateCode().equals(id))
                    .findFirst().orElseThrow(() -> new BusinessException("没有找到关联字段"));
            String templateContent = templateVo.getTemplateContent();
            Map<String, String> map = getMap(SMSREGEX, templateContent, 2, 1);
            return map.keySet()
                    .stream()
                    .map(e -> new ParameterOption<String>().setLabel(e).setValue(e)).collect(Collectors.toList());
        }
        throw new BusinessException("没有找到关联字段");
    }

    public static final String WXREGEX = "\\{\\{(.*?)}}";
    public static final String SMSREGEX = "\\$\\{(.*?)}";

    /**
     * 解析正则内容
     *
     * @param regex   正则
     * @param content 要解析的内容
     * @param st      截取开始位
     * @param end     截取结束拉
     * @return
     */
    public static Map<String, String> getMap(String regex, String content, int st, int end) {
        Map<String, String> map = new HashMap<>(2);
        Pattern pattern = Pattern.compile(regex);
//自旋进行最小匹配，直到无法匹配
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group();
            String substring = group.substring(st, group.length() - end);
            map.put(substring, "");
        }
        return map;
    }

    @Override
    public List<String> fields() {
        return Arrays.stream(fields.values()).map(Enum::name).collect(Collectors.toList());
    }

    enum fields {
        /**
         * content
         */
        content;
    }

}
