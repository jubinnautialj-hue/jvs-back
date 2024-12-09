package cn.bctools.rule.entity.enums;

/**
 * @author administer
 */
public enum NodeType {
    /**
     * 开始
     */
    start("start"),
    /**
     * 结束
     */
    end("end"),
    /**
     * 执行
     */
    task("task"),
    ;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    NodeType(String name) {
        this.name = name;
    }
}
