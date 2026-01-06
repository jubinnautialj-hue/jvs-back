package cn.bctools.im.controller;

import cn.hutool.core.bean.BeanUtil;

import cn.bctools.common.utils.R;
import cn.bctools.im.client.JimClientAPI;
import cn.bctools.im.config.JimConfig;
import cn.bctools.im.dto.DataPushDto;
import cn.bctools.im.dto.NotifyDto;
import org.jim.core.ImPacket;
import org.jim.core.packets.Command;
import org.jim.core.packets.DataPushReqBody;
import org.jim.core.packets.RespBody;
import org.jim.core.packets.notify.NotifyReqBody;
import org.jim.core.tcp.TcpPacket;
import org.jim.server.JimServerAPI;
import org.jim.server.util.ChannelMarkUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据推送
 */
@RestController
@RequestMapping("/im")
public class DataPushController {

    /**
     * 推送任务线程池（使用TTL装饰，确保租户信息传递）
     * 优化：设置有界队列，防止OOM
     */
    private static final Executor pushTaskExecutor = TtlExecutors.getTtlExecutor(
            new ThreadPoolExecutor(
                    4,
                    100,
                    30L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(10000),  // 优化：设置有界队列，防止OOM
                    new ThreadFactory() {
                        private final AtomicInteger threadNumber = new AtomicInteger(1);
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "im-push-" + threadNumber.getAndIncrement());
                        }
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy()));  // 队列满时由调用者执行，保证不丢失

    @PostMapping("/data/push")
    public R<String> push(@RequestBody @Validated DataPushDto dataPushDto, @RequestHeader("tenantId") String tenantId) {
        DataPushReqBody dataPushReqBody = new DataPushReqBody();
        dataPushReqBody.setType(dataPushDto.getType());
        dataPushReqBody.setContent(dataPushDto.getContent());
        dataPushReqBody.setCmd(Command.COMMAND_DATA_PUSH_REQ.getNumber());
        dataPushReqBody.setTenantId(tenantId);

        ImPacket dataPushPacket = new ImPacket(Command.COMMAND_DATA_PUSH_RESP, new RespBody(Command.COMMAND_DATA_PUSH_RESP, dataPushReqBody).toByte());

        pushTaskExecutor.execute(() -> {
            dataPushDto.getMark().stream().forEach(mark -> {
                JimServerAPI.sendToUser(ChannelMarkUtil.buildMark(tenantId, mark), dataPushPacket);
            });
        });

        return R.ok();
    }


    /**
     * 通知
     *
     * @param notifyDto
     * @return
     */
    @PostMapping("/notify")
    public R<String> notify(@RequestBody @Validated NotifyDto notifyDto) {
        NotifyReqBody notifyReqBody = BeanUtil.copyProperties(notifyDto, NotifyReqBody.class);
        notifyReqBody.setCmd(Command.COMMAND_NOTIFY_REQ.getNumber());
        TcpPacket tcpPacket = new TcpPacket(Command.COMMAND_NOTIFY_REQ, notifyReqBody.toByte());
        JimClientAPI.send(JimConfig.imClientChannelContext, tcpPacket);
        return R.ok();
    }
}
