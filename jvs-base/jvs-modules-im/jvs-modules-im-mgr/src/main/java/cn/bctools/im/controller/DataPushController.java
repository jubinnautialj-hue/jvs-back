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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据推送
 */
@RestController
@RequestMapping("/im")
public class DataPushController {

    /**
     * 推送任务线程池
     */
    private static final ThreadPoolExecutor pushTaskExecutor = new ThreadPoolExecutor(4, 100,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

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
