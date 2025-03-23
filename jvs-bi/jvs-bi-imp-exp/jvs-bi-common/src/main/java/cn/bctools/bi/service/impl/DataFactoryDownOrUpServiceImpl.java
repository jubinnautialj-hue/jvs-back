package cn.bctools.bi.service.impl;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Component
@AllArgsConstructor
public class DataFactoryDownOrUpServiceImpl implements DownOrUpService {
    private final CommonConfig commonConfig;
    private final DataFactoryApi dataFactoryApi;
    private final static String DOWN_FILE_PATH = "dataFactory";
    private final static String DOWN_FILE_NAME = "dataFactory.json";

    @Override
    public List<DownIdTypeDto> downGetId(String id, Boolean isMock) {
        List<DownIdTypeDto> list = new ArrayList<>();
        //获取数据集数据源id
        List<SourceFactoryGetIdDto> dataIdList = dataFactoryApi.getSourceFactory(Arrays.asList(id)).getData();
        List<String> dataSourceId = dataIdList.stream().filter(SourceFactoryGetIdDto::getIsDataSource)
                .map(SourceFactoryGetIdDto::getId).distinct().collect(Collectors.toList());
        List<String> dataFactoryId = dataIdList.stream().filter(e -> !e.getIsDataSource()).map(SourceFactoryGetIdDto::getId).collect(Collectors.toList());
        //添加本身
        dataFactoryId.add(id);
        //去重
        dataFactoryId = dataFactoryId.stream().distinct().collect(Collectors.toList());
        //数据源与数据集
        list.add(new DownIdTypeDto().setType(TaskTypeEnums.data_source).setId(dataSourceId));
        list.add(new DownIdTypeDto().setType(TaskTypeEnums.data_factory).setId(dataFactoryId));
        return list;
    }

    @Override
    public void downFile(List<String> id, Boolean isMock) {
        List<JSONObject> data = dataFactoryApi.getByIds(id).getData();
        String separator = File.separator;
        ZipUtil.saveFile(JSONObject.toJSONString(data), commonConfig.getDownPath() + separator + DOWN_FILE_PATH + separator + DOWN_FILE_NAME);
    }

    @Override
    public String getJsonPath() {
        String separator = File.separator;
        return commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator + DOWN_FILE_NAME;
    }

    @Override
    public String up(String nameOrId, UserDto userDto) {
        String separator = File.separator;
        String filePathPrefix = commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator;
        //基础数据类型
        String charJsonPath = filePathPrefix + DOWN_FILE_NAME;
        List<JSONObject> data = JSONArray.parseArray(FileUtil.readUtf8String(charJsonPath), JSONObject.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", data);
        jsonObject.put("userDto", userDto);
        dataFactoryApi.up(jsonObject, nameOrId);
        return null;
    }
}
