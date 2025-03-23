package cn.bctools.data.factory.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * 系统启动时执行的类 用于判断一些默认值是否存在
 * 例如血缘视图同步的定时任务
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class XxJobApplicationRunner implements ApplicationRunner {

    @Autowired
    XxlJobComponent xxlJobComponent;

    @Override
    public void run(ApplicationArguments args) {
        xxlJobComponent.addConsanguinityJob();
        xxlJobComponent.addCheckSeaTunnelTaskJob();
    }
}
