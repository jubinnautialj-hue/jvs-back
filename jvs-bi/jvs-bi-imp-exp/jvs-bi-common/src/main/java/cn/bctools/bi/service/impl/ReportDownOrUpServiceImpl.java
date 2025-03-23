package cn.bctools.bi.service.impl;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.bctools.report.api.JvsDataReportApi;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Slf4j
@Component
@AllArgsConstructor
public class ReportDownOrUpServiceImpl implements DownOrUpService {
    private final JvsDataReportApi jvsDataReportApi;
    private final CommonConfig commonConfig;
    private final DataFactoryApi dataFactoryApi;
    private final static String DOWN_FILE_PATH = "report";
    private final static String DOWN_CHART_FILE_NAME = "report.json";

    @Override
    public String getJsonPath() {
        String separator = File.separator;
        return commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator + DOWN_CHART_FILE_NAME;
    }

    @Override
    public void downFile(List<String> id, Boolean isMock) {
        String key = id.get(0);
        JSONObject jsonObject = jvsDataReportApi.downFile(key, isMock).getData();

        JSONObject json = new JSONObject();
        String separator = File.separator;
        //创建目录
        FileUtil.mkdir(commonConfig.getDownPath() + separator + DOWN_FILE_PATH);
        //json保存 包含设计数据与图片关联信息
        json.put("dataJson", jsonObject);
        ZipUtil.saveFile(json.toJSONString(), commonConfig.getDownPath() + separator + DOWN_FILE_PATH + separator + DOWN_CHART_FILE_NAME);
    }

    @Override
    public String up(String nameOrId, UserDto userDto) {
        String separator = File.separator;
        String filePathPrefix = commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator;
        //基础数据类型
        String charJsonPath = filePathPrefix + DOWN_CHART_FILE_NAME;
        JSONObject dataJson = JSONObject.parseObject(FileUtil.readUtf8String(charJsonPath));
        //获取设计数据
        String designStr = dataJson.getString("dataJson");
        JSONObject jsonObject = new JSONObject();
        JSONObject design = JSONObject.parseObject(designStr);
        jsonObject.put("data", design);
        jsonObject.put("userDto", userDto);
        jvsDataReportApi.up(jsonObject, nameOrId);
        //获取名称
        return design.getString("name");
    }

    @Override
    public List<DownIdTypeDto> downGetId(String id, Boolean isMock) {
        List<DownIdTypeDto> list = new ArrayList<>(3);
        //图表id
        list.add(new DownIdTypeDto().setType(TaskTypeEnums.report).setId(Arrays.asList(id)));
        if (isMock) {
            return list;
        }
        //获取数据集id
        List<String> data = jvsDataReportApi.getDataFactoryId(id).getData();
        if (!data.isEmpty()) {
            //获取数据集数据源id
            List<SourceFactoryGetIdDto> dataIdList = dataFactoryApi.getSourceFactory(data).getData();
            List<String> dataSourceId = dataIdList.stream().filter(SourceFactoryGetIdDto::getIsDataSource)
                    .map(SourceFactoryGetIdDto::getId).collect(Collectors.toList());
            List<String> dataFactoryId = dataIdList.stream().filter(e -> !e.getIsDataSource()).map(SourceFactoryGetIdDto::getId).collect(Collectors.toList());
            dataFactoryId.addAll(data);
            //数据源与数据集
            list.add(new DownIdTypeDto().setType(TaskTypeEnums.data_source).setId(dataSourceId));
            list.add(new DownIdTypeDto().setType(TaskTypeEnums.data_factory).setId(dataFactoryId));
        }
        return list;
    }


}
