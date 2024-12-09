package cn.bctools.design.jvslog.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.jvslog.entity.JvsLog;
import cn.bctools.design.jvslog.mapper.JvsLogMapper;
import cn.bctools.design.jvslog.service.JvsLogService;
import cn.bctools.design.project.service.JvsAppService;
import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author guojing
 */
@Service
public class JvsLogServiceImpl extends ServiceImpl<JvsLogMapper, JvsLog> implements JvsLogService {

    @Autowired
    JvsAppService jvsAppService;

    private final BlockingQueue<JvsLog> logBatch = new LinkedBlockingQueue<>();

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    /**
     * 添加应用的名称缓存
     */
    private final Cache<String, String> appName = CacheUtil.newTimedCache(1000 * 60 * 10);

    @Override
    public void callBack(String createBy, String description, String content) {
        try {
            DesignRoleSettingDto dto = DynamicDataUtils.getDto();
            JvsLog entity = new JvsLog().setDescription(description).setCreateBy(createBy).setContent(content).setCreateTime(LocalDateTime.now());
            if (ObjectNull.isNotNull(dto) && ObjectNull.isNotNull(dto.getJvsAppId())) {
                entity.setJvsAppId(dto.getJvsAppId())
                        .setJvsAppName(Optional.ofNullable(dto.getJvsAppName()).orElseGet(() -> appName.get(dto.getJvsAppId(), () -> jvsAppService.getById(dto.getJvsAppId()).getName())));
            }
            executorService.execute(() -> logBatch.add(entity));
            logBatch.add(entity);
        } catch (Exception e) {
            log.error("日志后保存数据失败");
        }
    }

    @PostConstruct
    private void heart() {
        //添加日志处理
        executorService.execute(this::consumerTaskQueue);
    }

    private void consumerTaskQueue() {
        while (true) {
            try {
                // 阻塞等待任务
                logBatch.take();
                List<JvsLog> list = new ArrayList<>();
                logBatch.drainTo(list);
                super.saveBatch(list);
            } catch (InterruptedException ignored) {
            }
        }
    }

}
