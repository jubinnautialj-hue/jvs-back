package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.dto.FileSyncDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcSyncLog;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.message.inside.InsideMessagePushPush;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcSyncLogService;
import cn.bctools.document.service.FileSyncService;
import cn.bctools.document.vo.req.DcLibraryAddReqVo;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.S3Template;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 */
@Service
@Slf4j
@AllArgsConstructor
public class FileSyncServiceImpl implements FileSyncService {
    private final static String MESSAGE_CONTENT = "文档同步已完成，共同步成功{}文件，消耗时间{}秒";
    private final DcLibraryService dcLibraryService;
    private final S3Template s3Template;
    private final static String BUCKET_NAME = "document-sync";
    private final InsideMessagePushPush insideMessagePushPush;

    private final DcSyncLogService dcSyncLogService;

    @Override
    public List<String> getFolder() {
        ListObjectsV2Result folder = this.getFolder("");
        return folder.getCommonPrefixes();
    }

    @Async
    @Override
    public void fileSync(FileSyncDto fileSyncDto, UserDto userDto) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        DcLibrary dcLibrary;
        if (StrUtil.isNotBlank(fileSyncDto.getDcLibraryId())) {
            dcLibrary = dcLibraryService.getById(fileSyncDto.getDcLibraryId());
            //新建文件夹
            DcLibraryAddReqVo dcLibraryAddReqVo = new DcLibraryAddReqVo()
                    .setFileType(DcLibraryTypeEnum.directory)
                    .setId(dcLibrary.getId())
                    .setParentId(dcLibrary.getId())
                    .setName(fileSyncDto.getFolderName().replaceAll("/", ""));
            dcLibrary = dcLibraryService.add(userDto, dcLibraryAddReqVo);
        } else {
            //创建知识库
            DcLibraryAddReqVo dcLibraryAddReqVo = new DcLibraryAddReqVo()
                    .setAddAuthConfigDto(new ArrayList<>())
                    .setDescription("")
                    .setName(fileSyncDto.getFolderName())
                    .setReadNotify(Boolean.TRUE);
            dcLibrary = dcLibraryService.add(userDto, dcLibraryAddReqVo);
        }
        String folderName = fileSyncDto.getFolderName();
        boolean b = StrUtil.endWith(folderName, "/");
        if (!b) {
            folderName = folderName + "/";
        }
        int number = dcLibrary(folderName, dcLibrary.getId(), userDto);
        //同步消耗时间
        stopWatch.stop();
        double totalTimeSeconds = stopWatch.getTotalTimeSeconds();
        int i = (int) totalTimeSeconds;
        //消息通知
        String format = StrUtil.format(MESSAGE_CONTENT, number, i);
        Boolean aBoolean = insideMessagePushPush.textPushMessage(format, userDto);
        log.info("文档同步消息推送结构:{}", aBoolean);
        dcSyncLogService.save(new DcSyncLog()
                .setDcId(dcLibrary.getId())
                .setDcName(dcLibrary.getName())
                .setSyncTime(stopWatch.getLastTaskTimeMillis())
                .setSyncIndicator(fileSyncDto.getSyncIndicator()));
    }


    /**
     * 获取文件夹 下面的所有内容
     *
     * @param folderName 文件夹路径
     */
    private ListObjectsV2Result getFolder(String folderName) {
        //先检查桶是否存在 防止报错 用户体验不好
        String bucketName = "document-sync";
        boolean bucketExists = s3Template.bucketExists(bucketName);
        if (!bucketExists) {
            s3Template.makeBucket(bucketName);
        }
        AmazonS3Client s3Client = s3Template.getS3Client();
        // 构建 ListObjectsV2Request 请求对象
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                // 设置前缀为指定文件夹路径
                .withPrefix(folderName)
                // 设置分隔符为斜杠
                .withDelimiter("/");
        return s3Client.listObjectsV2(request);
    }


    /**
     * 循环入库
     *
     * @param folderName 文件夹名称
     * @param parentId   上级id
     * @param userDto    用户信息
     * @return 本次同步成功的文件数量
     */
    private int dcLibrary(String folderName, String parentId, UserDto userDto) {
        int number = 0;
        try {
            ListObjectsV2Result folder = this.getFolder(folderName);
            // 遍历文件
            for (S3ObjectSummary objectSummary : folder.getObjectSummaries()) {
                String key = objectSummary.getKey();
                String[] split = key.split("/");
                BaseFile baseFile = new BaseFile()
                        .setSize(objectSummary.getSize())
                        .setBucketName(BUCKET_NAME)
                        .setFileName(key)
                        .setOriginalName(split[split.length - 1]);
                dcLibraryService.fileToSave(parentId, baseFile, split[split.length - 1]);
                number++;
            }
            //遍历文件夹
            for (String commonPrefix : folder.getCommonPrefixes()) {
                String[] split = commonPrefix.split("/");
                String name = split[split.length - 1];
                DcLibraryAddReqVo dcLibraryAddReqVo = new DcLibraryAddReqVo()
                        .setFileType(DcLibraryTypeEnum.directory)
                        .setId(parentId)
                        .setParentId(parentId)
                        .setName(name);
                String id = dcLibraryService.add(userDto, dcLibraryAddReqVo).getId();
                number += dcLibrary(commonPrefix, id, userDto);
            }
        } catch (Exception exception) {
            log.error("获取桶下面的文件错误", exception);
        }
        return number;
    }
}
