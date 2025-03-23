package cn.bctools.bi.receiver;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.DownFileTaskDto;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.dto.UpFileTaskDto;
import cn.bctools.bi.entity.JvsBiFile;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.service.JvsBiFileService;
import cn.bctools.bi.util.RabbitMqUtils;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.api.DataSourceApi;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class BiConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final JvsBiFileService jvsBiFileService;
    private final CommonConfig commonConfig;
    private final OssTemplate ossTemplate;
    private final static String SERVICE_PATH = "service.json";

    @RabbitListener(queues = RabbitMqUtils.QUEUE_BI_TASK_DOWN, concurrency = "1")
    public void downFile(Message message, Channel channel) throws IOException {
        String errorMsg = null;
        String errorLog = null;
        BaseFile baseFile = new BaseFile();
        DownFileTaskDto downFileTaskDto = JSONObject.parseObject(message.getBody(), DownFileTaskDto.class);
        TenantContextHolder.setTenantId(downFileTaskDto.getTenantId());
        String downPath = commonConfig.getDownPath();
        try {
            DownOrUpService downOrUpService = SpringContextUtil.getBean(downFileTaskDto.getType().getAClass());
            List<DownIdTypeDto> list = downOrUpService.downGetId(downFileTaskDto.getDataId(), downFileTaskDto.getIsMock());
            //通过类型进行分组
            Map<TaskTypeEnums, List<String>> map = list.stream().flatMap(dto -> dto.getId().stream().map(id -> new AbstractMap.SimpleEntry<>(dto.getType(), id)))
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                    ));
            //根据不同类型 进行数据下载
            for (TaskTypeEnums taskTypeEnums : map.keySet()) {
                List<String> ids = map.get(taskTypeEnums);
                SpringContextUtil.getBean(taskTypeEnums.getAClass()).downFile(ids, downFileTaskDto.getIsMock());
            }
            //标识本地打包的数据有哪些服务的 方便上传时 数据处理
            Set<TaskTypeEnums> taskTypeEnums = map.keySet();
            ZipUtil.saveFile(JSONObject.toJSONString(taskTypeEnums), downPath + SERVICE_PATH);
            //下载完成进行打包
            String zipFileName = IdGenerator.getIdStr() + ".zip";
            String zipFilePath = downPath + File.separator + zipFileName;
            ZipUtil.zipFile(downPath, zipFilePath, commonConfig.getZipPwd());
            //上传
            BufferedInputStream inputStream = FileUtil.getInputStream(zipFilePath);
            baseFile = ossTemplate.putFile("jvs-public", "bi-down", zipFileName, inputStream);
            inputStream.close();
            FileUtil.del(zipFilePath);
        } catch (BusinessException businessException) {
            errorMsg = businessException.getMessage();
        } catch (Exception exception) {
            log.error("下载文件其他错误", exception);
            if (exception.getCause() instanceof BusinessException) {
                errorMsg = exception.getCause().getMessage();
            } else {
                errorMsg = "未知错误";
            }
            errorLog = exception.getMessage();
        } finally {
            //删除整个目录中的所有文件
            FileUtil.del(downPath);
            //修改日志
            boolean notBlank = StrUtil.isNotBlank(errorMsg);
            jvsBiFileService.update(new UpdateWrapper<JvsBiFile>().lambda()
                    .set(JvsBiFile::getOperateStatus, true)
                    .set(notBlank, JvsBiFile::getErrorMessage, errorMsg)
                    .set(!notBlank, JvsBiFile::getBucketName, baseFile.getBucketName())
                    .set(!notBlank, JvsBiFile::getFileName, baseFile.getFileName())
                    .set(notBlank, JvsBiFile::getErrorLog, errorLog)
                    .eq(JvsBiFile::getId, downFileTaskDto.getTaskId()));
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    @RabbitListener(queues = RabbitMqUtils.QUEUE_BI_TASK_UP, concurrency = "1")
    public void upFile(Message message, Channel channel) throws IOException {
        UpFileTaskDto upFileTaskDto = JSONObject.parseObject(message.getBody(), UpFileTaskDto.class);
        TenantContextHolder.setTenantId(upFileTaskDto.getTenantId());
        String errorMsg = null;
        String errorLog = null;
        try {
            FileUtil.mkdir(new File(commonConfig.getUnzipPath()));
            String unzipPath = commonConfig.getUnzipPath() + IdGenerator.getIdStr() + ".zip";
            //下载压缩包到本地
            String url = ossTemplate.fileLink(upFileTaskDto.getFileName(), upFileTaskDto.getBucketName());
            ZipUtil.downloadFile(url, unzipPath);
            //解压文件
            ZipUtil.unzip(commonConfig.getUnzipPath(), unzipPath, commonConfig.getZipPwd());
            //数据处理 获取服务标识数据
            String serviceData = FileUtil.readUtf8String(commonConfig.getUnzipPath() + SERVICE_PATH);
            List<TaskTypeEnums> taskTypeEnums = JSONArray.parseArray(serviceData, TaskTypeEnums.class);
            //判断是否存在数据源 并且如果存在数据集类型数据需要重新绑定id
            boolean isDataSource = taskTypeEnums.stream().anyMatch(e -> e.equals(TaskTypeEnums.data_source));
            if (isDataSource) {
                List<Map<String, String>> mapList = isDataFactorySource();
                if (!mapList.isEmpty()) {
                    for (TaskTypeEnums taskTypeEnum : taskTypeEnums) {
                        DownOrUpService downOrUpService = SpringContextUtil.getBean(taskTypeEnum.getAClass());
                        String path = downOrUpService.getJsonPath();
                        downOrUpService.replaceDataSourceId(mapList, path);
                    }
                }
            }
            //根据服务类型 调用不同的处理类
            String menuName;
            Optional<TaskTypeEnums> first = taskTypeEnums.stream().filter(TaskTypeEnums::getIsGetName).findFirst();
            if (first.isPresent()) {
                TaskTypeEnums typeEnums = first.get();
                DownOrUpService downOrUpService = SpringContextUtil.getBean(typeEnums.getAClass());
                menuName = downOrUpService.up(upFileTaskDto.getMenuId(), upFileTaskDto.getUserDto());
                //排除已经执行的数据
                taskTypeEnums = taskTypeEnums.stream().filter(e -> !e.equals(typeEnums)).collect(Collectors.toList());
            } else {
                menuName = upFileTaskDto.getMenuId();
            }
            //执行逻辑
            taskTypeEnums.forEach(e -> SpringContextUtil.getBean(e.getAClass()).up(menuName, upFileTaskDto.getUserDto()));
        } catch (Exception exception) {
            log.error("导入数据错误", exception);
            errorMsg = "未知错误";
            errorLog = exception.getMessage();
        } finally {
            //删除整个目录中的所有文件
            FileUtil.del(commonConfig.getUnzipPath());
            //修改日志
            boolean notBlank = StrUtil.isNotBlank(errorMsg);
            jvsBiFileService.update(new UpdateWrapper<JvsBiFile>().lambda()
                    .set(JvsBiFile::getOperateStatus, true)
                    .set(notBlank, JvsBiFile::getErrorMessage, errorMsg)
                    .set(notBlank, JvsBiFile::getErrorLog, errorLog)
                    .eq(JvsBiFile::getId, upFileTaskDto.getTaskId()));
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    /**
     * 获取数据集类型的数据源id
     *
     * @return 绑定关系
     */
    private List<Map<String, String>> isDataFactorySource() {
        //判断是否存在数据集类型的数据源如果存在需要进行数据的替换
        DownOrUpService downOrUpService = SpringContextUtil.getBean(TaskTypeEnums.data_source.getAClass());
        String jsonPath = downOrUpService.getJsonPath();
        List<JSONObject> data = JSONArray.parseArray(FileUtil.readUtf8String(jsonPath), JSONObject.class);
        //获取当前系统的 数据集类型的数据源id
        DataSourceApi dataSourceApi = SpringContextUtil.getBean(DataSourceApi.class);
        String id = dataSourceApi.getDataFatcorySourceId().getData();
        List<Map<String, String>> mapList = data.stream().filter(e -> e.getString("sourceType").equals("dataFactoryDataSource"))
                .map(e -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(e.getString("id"), id);
                    return map;
                }).collect(Collectors.toList());
        return mapList;
    }

    /**
     * 下载数据消息发送
     *
     * @param downFileTaskDto 消息对象
     */
    public void sendDownTask(DownFileTaskDto downFileTaskDto) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_BI_TASK_DOWN, downFileTaskDto);
    }

    /**
     * 下载数据消息发送
     *
     * @param upFileTaskDto 消息对象
     */
    public void sendUpTask(UpFileTaskDto upFileTaskDto) {
        rabbitTemplate.convertAndSend(RabbitMqUtils.QUEUE_BI_TASK_UP, upFileTaskDto);
    }
}
