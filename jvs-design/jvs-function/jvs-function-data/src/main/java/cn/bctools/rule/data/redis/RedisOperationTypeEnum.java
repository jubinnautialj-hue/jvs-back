package cn.bctools.rule.data.redis;

/**
 * @author zhuxiaokang
 * redis操作类型
 */
public enum RedisOperationTypeEnum {

    /**
     * 取值
     */
    GET("取值"),

    /**
     * 设值
     */
    SET("设值"),

    /**
     * 删除
     */
    DEL("删除");

    private String name;

    RedisOperationTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
