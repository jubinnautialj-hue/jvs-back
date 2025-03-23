package cn.bctools.data.factory.source.receiver;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.source.config.RabbitMqConfig;
import cn.bctools.data.factory.source.data.excel.ExcelExecuteImpl;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.dto.MqMessageDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author xiaohui
 */
@Slf4j
@Component
@Lazy(false)
public class DataSourceTaskConsumer {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ExcelExecuteImpl excelExecute;
    @Autowired
    OssTemplate ossTemplate;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    InsideNotificationApi insideNotificationApi;

    @SneakyThrows
    @RabbitListener(queues = RabbitMqConfig.QUEUE_DATA_SOURCE_READ_EXCEL_TASK)
    public void receiveMessage(MqMessageDto mqMessageDto) {
        TenantContextHolder.setTenantId(mqMessageDto.getTenantId());
        BaseFile baseFile = mqMessageDto.getBaseFile();
        String url = ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName());
        boolean status = true;
        try {
            //读取excel
            excelExecute.read(new ExcelReadDataPo().setDataSourceId(mqMessageDto.getId()).setAddIs(mqMessageDto.getAddIs()).setUrl(url));
            //修改状态
            dataSourceService.update(new UpdateWrapper<DataSource>().lambda().eq(DataSource::getId, mqMessageDto.getId()).set(DataSource::getSyncStructure, 1));
        } catch (Exception exception) {
            log.info("解析失败", exception);
            status = false;
        }
        this.pushMessage(baseFile.getOriginalName(), status, mqMessageDto.getUserDto());
    }

    /**
     * 消息发送
     *
     * @param fileName 文档名称
     * @param userDto  此操作发起人信息
     * @param status   执行状态文本
     */
    private void pushMessage(String fileName, boolean status, UserDto userDto) {
        String title = "EXCEL数据源数据读取完毕";
        String content = "尊敬的用户，您上传的《" + fileName + "》数据已成功读取完毕";
        if (!status) {
            title = "EXCEL数据源数据读取失败";
        }
        Dict set = Dict.create().set("title", title).set("content", content);
        ReceiversDto receiversDto = new ReceiversDto().setUserId(userDto.getId()).setTenantId(userDto.getTenantId());
        InsideNotificationDto notificationDto = new InsideNotificationDto();
        notificationDto
                .setContent(JSONObject.toJSONString(set))
                .setDefinedReceivers(Arrays.asList(receiversDto));
        Boolean data = insideNotificationApi.send(notificationDto).getData();
        log.info("消息推送结果为:{}", data);
    }

    /**
     * 消息发送
     */
    public void send(MqMessageDto mqMessageDto) {
        try {
            rabbitTemplate.convertAndSend(RabbitMqConfig.QUEUE_DATA_SOURCE_READ_EXCEL_TASK, mqMessageDto);
        } catch (Exception exception) {
            log.error("同步es消息发送失败:", exception);
        }
    }
}
