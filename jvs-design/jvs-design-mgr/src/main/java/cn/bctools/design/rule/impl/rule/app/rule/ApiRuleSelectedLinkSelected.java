package cn.bctools.design.rule.impl.rule.app.rule;

import cn.bctools.rule.common.LinkFieldSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Service
public class ApiRuleSelectedLinkSelected implements LinkFieldSelected<String> {


    /**
     * 获取逻辑然后根据参数强校验进行匹配结构
     */
    @Override
    public Object link(String id, String field) {
        return null;
    }

    @Override
    public List<String> fields() {
        return Collections.singletonList("body");
    }

}
