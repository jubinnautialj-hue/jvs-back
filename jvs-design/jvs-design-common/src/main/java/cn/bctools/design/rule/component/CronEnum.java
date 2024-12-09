package cn.bctools.design.rule.component;

/**
 * @author guojing
 */

public enum CronEnum {
    /**
     * cron表达式
     */
    Per_hour("每小时", "0 0 * * * ? *"),
    Every_day("每天(0点)", "0 0 0 * * ? *"),
    Weekdays("工作日(周一至周五)", "0 0 0 ? * 2,3,4,5,6 *"),
    Weekly_Monday("每周（周一）", "0 0 0 ? * 2 *"),
    Monthly10("每月（10日）", "0 0 0 10 * ? "),
    Monthly("每月（15日）", "0 0 0 15 * ? "),
    The_last_day_of_each_month("每月最后一天", "0 0 0 L * ? "),
    Custom("自定义", null);

    String name;
    String corn;

    public String getName() {
        return name;
    }

    public String getCorn() {
        return corn;
    }

    CronEnum(String name, String corn) {
        this.name = name;
        this.corn = corn;
    }

}
