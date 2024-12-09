package cn.bctools.design.workflow.enums;

import cn.bctools.function.enums.JvsParamType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enum Default param enums.
 *
 * @author zhuxiaokang  工作流默认公式参数
 */
@Getter
@AllArgsConstructor
public enum DefaultParamEnums {

    /**
     * Task id default param enums.
     */
    TASK_ID("defTaskId", "流程实例id", "流程实例id", JvsParamType.text),
    /**
     * Create time default param enums.
     */
    CREATE_TIME("defCreateTime", "流程发起时间", "流程发起时间", JvsParamType.date),
    /**
     * Create by default param enums.
     */
    CREATE_BY("defCreateBy", "流程发起人", "流程发起人", JvsParamType.text),
    ;

    private final String id;
    private final String name;
    private final String info;
    private final JvsParamType jvsParamType;

    /**
     * Gets by id.
     *
     * @param id the id
     * @return the by id
     */
    public static DefaultParamEnums getById(String id) {
        for (DefaultParamEnums value : DefaultParamEnums.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
