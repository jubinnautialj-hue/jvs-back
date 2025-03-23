package cn.bctools.rule.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.utils.dto.RuleExecDto;
import cn.bctools.rule.utils.html.ResultDto;
import com.alibaba.fastjson2.JSONPath;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Rule system thread local.
 *
 * @author guojing
 */
@Slf4j
public class RuleSystemThreadLocal extends SystemThreadLocal {
    public static ThreadLocal<RuleExecDto> ruleThreadLocal = new ThreadLocal<>();

    /**
     * The constant PREFIX.
     */
    public static final String PREFIX = "©";

    /**
     * Gets rule.
     *
     * @return the rule
     */
    public static RuleExecDto getRule() {
        return ruleThreadLocal.get();
    }

    /**
     * Set.
     *
     * @param ruleExecDto the rule exec dto
     */
    public static void set(RuleExecDto ruleExecDto) {
        ruleThreadLocal.set(ruleExecDto);
    }

    /**
     * The Params.
     */
    static Set<String> params = new HashSet<>(1);

    /**
     * 将获取参数中需要的数据字段，直接将其服务把值存放到线程中
     *
     * @param data the data
     */
    public static void setParameterSelectedOption(Map<String, Object> data) {
        if (ObjectUtils.isEmpty(data)) {
            return;
        }
        Set<String> collect = SpringContextUtil.getApplicationContext().getBeansOfType(ParameterSelected.class).values().stream().filter(e -> ObjectNull.isNotNull(e.key())).map(ParameterSelected::key).collect(Collectors.toSet());
        params.addAll(collect);
        for (String name : params) {
            if (ObjectNull.isNotNull(data)) {
                if (data.containsKey(name)) {
                    SystemThreadLocal.set(name, data.get(name));
                }
            }
        }
    }

    /**
     * 将设置全局变量
     *
     * @param nodeResult 当前这个所在画布
     * @param canvasId   当前执行所在的画布
     */
    public static void setGlobalVariable(Map<String, ResultDto> nodeResult, String canvasId) {
        set("rule:globalvariable" + PREFIX + canvasId, nodeResult);
    }

    /**
     * 根据画布信息，和nodeId获取对应的值
     *
     * @param canvasId 画布id值
     * @param nodeId   某个节点的nodeId
     * @return global variable
     */
    public static Object getGlobalVariable(String canvasId, String nodeId) {
        Map<String, ResultDto> o = get("rule:globalvariable" + PREFIX + canvasId);
        return o.get(nodeId).getValue();
    }

    /**
     * 根据画布获取相关变量
     *
     * @param canvasId the canvas id
     * @return global variable
     */
    public static Map<String, ResultDto> getGlobalVariable(String canvasId) {
        return get("rule:globalvariable" + PREFIX + canvasId);
    }

    /**
     * 设置全局变量
     *
     * @param fieldKey the field key
     * @param obj      the obj
     */
    public static void setGlobalVariableValue(String fieldKey, Object obj) {
        String[] split = fieldKey.split("©");
        Map<String, ResultDto> o = get("rule:globalvariable" + PREFIX + split[1]);
        if (split.length == 4) {
            JSONPath.set(o.get(split[2]).getValue(), split[3], obj);
        } else if (split.length == 3 && "rule:globalvariable".equals(split[0])) {
            //如果全局变量，只修改了一个值的时候，直接存储
            String key = split[2];
            if (key.contains(".")) {
                int i = key.indexOf(".");
                JSONPath.set(o.get(key.substring(0, i)).getValue(), key.substring(i), obj);
            } else {
                o.get(key).setValue(obj);
            }
        } else if (split.length == 3 && "ergodic".equals(split[1])) {

        } else {
            set("rule:globalvariable" + PREFIX + split[1], o);
        }
    }

    /**
     * Gets global variable value.
     *
     * @param fieldKey the field key
     * @return the global variable value
     */
    public static Object getGlobalVariableValue(String fieldKey) {
        String[] split = fieldKey.split("©");
        Map<String, ResultDto> o = get("rule:globalvariable" + PREFIX + split[1]);
        if (split.length == 4) {
            return JSONPath.eval(o.get(split[2]).getValue(), split[3]);
        } else if (split.length == 3 && "rule:globalvariable".equals(split[0])) {
            //如果全局变量，只修改了一个值的时候，直接存储
            String key = split[2];
            if (key.contains(".")) {
                int i = key.indexOf(".");
                return JSONPath.eval(o.get(key.substring(0, i)).getValue(), key.substring(i));
            } else {
                return o.get(key).getValue();
            }
        }
        return null;
    }

    /**
     * 当前线程中的逻辑打上停止标识
     *
     * @return boolean
     */
    public static Boolean runStop() {
        Boolean stop = get("globalvariable");
        if (ObjectNull.isNotNull(stop)) {
            return stop;
        }
        return false;
    }

    /**
     * Sets run stop.
     */
    public static void setRunStop() {
        set("globalvariable", true);
    }

    /**
     * The type Ergodic.
     */
    @Data
    @Accessors(chain = true)
    public static final class Ergodic {
        /**
         * 循环当前执行的节点id
         */
        String execNodeId;
        /**
         * 执行的索引号
         */
        Integer i;
    }

    /**
     * The constant threadLocal.
     */
    public static ThreadLocal<Map<String, Stack<Integer>>> threadLocal = new ThreadLocal<>();

    static {
        threadLocal.set(new HashMap<>(8));
    }

    /**
     * Clear ergodic.
     */
    public static void clearErgodic() {
        threadLocal.set(new HashMap<>(8));
    }

    /**
     * 设置循环画布的索引号
     *
     * @param i 循环索引
     */
    public static void setErgodic(int i) {
        if (null == threadLocal.get()) {
            threadLocal.set(new HashMap<>(8));
        }
        Stack o = threadLocal.get().get("rule:ergodic");
        if (ObjectNull.isNull(o)) {
            o = new Stack();
        }
        o.push(i);
        threadLocal.get().put("rule:ergodic", o);
    }

    /**
     * Gets ergodic.
     *
     * @return the ergodic
     */
    public static int getErgodic() {
        Stack o = threadLocal.get().get("rule:ergodic");
        if (ObjectNull.isNull(o)) {
            throw new BusinessException("循环变量的索引号只允许使用一次请使用变量引用赋值");
        }
        return (int) o.pop();
    }

}
