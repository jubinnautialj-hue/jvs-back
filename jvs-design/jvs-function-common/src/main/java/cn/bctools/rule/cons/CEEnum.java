package cn.bctools.rule.cons;

/**
 * @author wl
 */
public enum CEEnum {

    /**
     * 组件扩展属性
     */
    add("可添加"),
    query("可查询"),
    ;

    String msg;

    CEEnum(String msg) {
        this.msg = msg;
    }

}
