package cn.bctools.screen.query.impl;

import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.screen.query.ChartQueryTypeValue;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ChartQueryTypeValueRoleId implements ChartQueryTypeValue {
    @Override
    public String execute(String value) {
        return JSONObject.toJSONString(UserCurrentUtils.getRole());
    }
}
