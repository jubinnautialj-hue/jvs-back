package cn.bctools.rule.data.enums;

/**
 * @author guojing
 */

public enum OperationEnum {
    /**
     * 保存
     */
    SAVE("增"),
    /**
     * 删除
     */
    DELETE("删"),
    /**
     * 查询
     */
    QUERY("查");

    private String name;

    OperationEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
