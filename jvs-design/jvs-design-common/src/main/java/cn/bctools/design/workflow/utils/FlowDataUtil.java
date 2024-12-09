package cn.bctools.design.workflow.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.support.node.AutomationFlowHandler;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流数据处理工具
 */
public class FlowDataUtil {

    private FlowDataUtil() {

    }

    /**
     * 不需要保存到数据模型的字段
     */
    private static final List<String> NO_SAVE_FIELDS = new ArrayList<>();

    static {
        NO_SAVE_FIELDS.add(AutomationFlowHandler.KEY_PARAM);
    }

    /**
     * 排除不需要保存到数据模型的字段
     *
     * @param data 数据
     * @return 排除不需要保存到数据模型的字段后的数据
     */
    public static JSONObject excludeData(JSONObject data) {
        if (ObjectNull.isNull(data)) {
            return data;
        }
        JSONObject newData = JSON.parseObject(JSON.toJSONString(data));
        NO_SAVE_FIELDS.forEach(newData::remove);
        return newData;
    }
}
