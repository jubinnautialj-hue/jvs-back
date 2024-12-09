package cn.bctools.design.cron;

import cn.hutool.cron.CronUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Component
public class GlobalCronRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        //支持秒级
        CronUtil.setMatchSecond(true);
        //开启定时任务
        CronUtil.start(true);
    }

}
