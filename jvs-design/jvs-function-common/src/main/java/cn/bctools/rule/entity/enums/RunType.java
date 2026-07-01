package cn.bctools.rule.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * @author My_gj
 */

public enum RunType {
    /**
     * 定时任务
     */
    JOB("JOB", "定时任务"),
    /**
     * 测试执行
     */
    TEST("TEST", "设计测试"),
    API("API", "API"),
    /**
     * 正式运行
     */
    REAL("REAL", "正式运行");


    @EnumValue
    public final String value;
    public final String desc;

    RunType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
