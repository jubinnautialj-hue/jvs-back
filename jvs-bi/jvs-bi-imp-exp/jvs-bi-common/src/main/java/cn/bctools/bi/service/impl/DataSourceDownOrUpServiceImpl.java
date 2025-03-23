package cn.bctools.bi.service.impl;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.api.DataSourceApi;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
@Component
@AllArgsConstructor
@Slf4j
public class DataSourceDownOrUpServiceImpl implements DownOrUpService {
    private final CommonConfig commonConfig;
    private final DataSourceApi dataSourceApi;
    private final OssTemplate ossTemplate;
    private final static String DOWN_FILE_PATH = "dataSource";
    private final static String DOWN_FILE_NAME = "dataSource.json";

    @Override
    public void downFile(List<String> id, Boolean isMock) {
        List<JSONObject> data = dataSourceApi.getByList(id).getData();
        String separator = File.separator;
        ZipUtil.saveFile(JSONObject.toJSONString(data), commonConfig.getDownPath() + separator + DOWN_FILE_PATH + separator + DOWN_FILE_NAME);
        String saveFilePath = commonConfig.getDownPath() + separator + DOWN_FILE_PATH + separator;
        //判断是否为excel 如果是 需要导出excel文件
        data.stream().filter(e -> e.getString("sourceType").equals("excelDataSource"))
                .forEach(e -> {
                    JSONObject exportData = e.getJSONObject("exportData");
                    JSONArray commitLog = exportData.getJSONArray("excelCommitLog");
                    if (!commitLog.isEmpty()) {
                        commitLog.forEach(v -> {
                            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(v));
                            FileNameDto fileConfig = jsonObject.getObject("fileConfig", FileNameDto.class);
                            try {
                                ZipUtil.downloadFile(ossTemplate.fileLink(fileConfig.getFileName(), fileConfig.getBucketName()), saveFilePath + jsonObject.get("id") + "." + FileUtil.extName(fileConfig.getFileName()));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                });
    }

    @Override
    public String getJsonPath() {
        String separator = File.separator;
        return commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator + DOWN_FILE_NAME;
    }

    @Override
    public List<DownIdTypeDto> downGetId(String id, Boolean isMock) {
        DownIdTypeDto downIdTypeDto = new DownIdTypeDto().setType(TaskTypeEnums.data_source).setId(Arrays.asList(id));
        return Arrays.asList(downIdTypeDto);
    }

    @Override
    public String up(String nameOrId, UserDto userDto) {
        String separator = File.separator;
        String filePathPrefix = commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator;
        //基础数据类型
        String charJsonPath = filePathPrefix + DOWN_FILE_NAME;
        List<JSONObject> data = JSONArray.parseArray(FileUtil.readUtf8String(charJsonPath), JSONObject.class);
        //excel 数据重新上传文件
        data.stream().filter(e -> e.getString("sourceType").equals("excelDataSource"))
                .forEach(e -> {
                    JSONObject exportData = e.getJSONObject("exportData");
                    JSONArray commitLog = exportData.getJSONArray("excelCommitLog");
                    if (!commitLog.isEmpty()) {
                        commitLog.forEach(v -> {
                            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(v));
                            FileNameDto fileConfig = jsonObject.getObject("fileConfig", FileNameDto.class);
                            String fileName = fileConfig.getFileName();
                            String saveFilePath = filePathPrefix + jsonObject.get("id") + "." + FileUtil.extName(fileName);
                            //重新上传文件
                            BufferedInputStream inputStream = FileUtil.getInputStream(saveFilePath);
                            BaseFile baseFile = ossTemplate.putFile(fileName, inputStream);
                            fileConfig.setOriginalFileName(baseFile.getOriginalName())
                                    .setBucketName(baseFile.getBucketName())
                                    .setFileName(baseFile.getFileName())
                                    .setFileLink(baseFile.getUrl())
                                    .setFileSize(baseFile.getSize());
                            jsonObject.put("fileConfig", JSONObject.parseObject(JSONObject.toJSONString(fileConfig)));
                            try {
                                inputStream.close();
                            } catch (IOException ex) {
                                log.error("导入数据源为excel时,上传excel错误", ex);
                                throw new BusinessException("关闭流文件错误");
                            }
                        });
                    }
                });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        jsonObject.put("userDto", userDto);
        dataSourceApi.up(jsonObject);
        return null;
    }
}
