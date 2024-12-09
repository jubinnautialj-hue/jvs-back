package cn.bctools.common.enums;

import lombok.Data;

/**
 * @author zhuxiaokang
 * 发送消息间隔
 */
@Data
public class SendMessageTimeLimit {

    /**
     * 审批期限单位
     */
    private TimeLimitTypeEnum type = TimeLimitTypeEnum.HOUR;

    /**
     * 期限值（）
     */
    private Long limit = 1L;

    /**
     * 一分钟秒数
     */
    private static final int ONE_MINUTE_SECONDS = 60;

    /**
     * 一小时秒数
     */
    private static final int ONE_HOUR_SECONDS = 60 * ONE_MINUTE_SECONDS;


    /**
     * 一天秒数
     */
    private static final int ONE_DAY_SECONDS = 24 * ONE_HOUR_SECONDS;

    /**
     * 转为秒
     * @return
     */
    public Long calculateMill() {
        switch (type) {
            case MINUTE:
                // 分钟转秒
                return limit * ONE_MINUTE_SECONDS;
            case HOUR:
                // 小时转秒
                return limit * ONE_HOUR_SECONDS;
            case DAY:
                // 天转秒
                return limit * ONE_DAY_SECONDS;
            default:
                return 0L;
        }
    }
}
