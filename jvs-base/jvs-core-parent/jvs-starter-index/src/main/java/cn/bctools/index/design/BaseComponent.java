package cn.bctools.index.design;

import cn.bctools.index.design.component.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author jvs
 * 基础组件
 */
@Component
public class BaseComponent {

    /**
     * 二维码组件
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    ComponentCodeCardService componentCodeCardService() {
        return new ComponentCodeCardService() {
        };
    }

    /**
     * 信息卡片
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ComponentLabelService labelService() {
        return new ComponentLabelService() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public DesignAppFrameService designAppFrame() {
        return new DesignAppFrameService() {
        };
    }


    /**
     * 日历
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    ComponentCalendarService calendarRender() {
        return new ComponentCalendarService() {

        };
    }


    /**
     * 公告
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    ComponentNoticeService noticeService() {
        return new ComponentNoticeService() {
        };
    }

    /**
     * 消息组件
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    ComponentMessageService messageService() {
        return new ComponentMessageService() {
        };
    }


}
