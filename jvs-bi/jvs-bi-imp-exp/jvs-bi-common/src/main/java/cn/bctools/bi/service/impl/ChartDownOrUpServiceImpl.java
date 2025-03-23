package cn.bctools.bi.service.impl;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.ChartBackImgDto;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.chart.api.JvsChartApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
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
public class ChartDownOrUpServiceImpl implements DownOrUpService {
    private final JvsChartApi jvsChartApi;
    private final OssProperties ossProperties;
    private final OssTemplate ossTemplate;
    private final CommonConfig commonConfig;
    private final DataFactoryApi dataFactoryApi;
    private final static String DOWN_FILE_PATH = "chart";
    private final static String DOWN_CHART_FILE_NAME = "chart.json";
    /**
     * 图表设计的附件路径
     */
    private final static List<String> URL_JSON_KEY = new ArrayList<String>() {{
        //背景图
        add("setting,cardBackImg,backImg");
        //标题背景图
        add("setting,titleBackImg,backImg");
        //标题icon
        add("setting,titleIcon,backImg");
    }};


    @Override
    public void downFile(List<String> id, Boolean isMock) {
        String endpoint = ossProperties.getEndpoint().startsWith("http") ? ossProperties.getEndpoint() : "http://" + ossProperties.getEndpoint();
        String key = id.get(0);
        JSONObject jsonObject = jvsChartApi.downFile(key, isMock).getData();
        JSONArray dataJson = jsonObject.getJSONArray("dataJson");
        //图片关联关系
        JSONObject json = new JSONObject();
        String separator = File.separator;
        //创建目录
        FileUtil.mkdir(commonConfig.getDownPath() + separator + DOWN_FILE_PATH);
        List<ChartBackImgDto> backImgList = new ArrayList<>();
        dataJson.stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e)))
                .forEach(e -> {
                    for (String urlJsonKey : URL_JSON_KEY) {
                        ChartBackImgDto chartBackImgDto = new ChartBackImgDto();
                        String md5 = SecureUtil.md5(urlJsonKey);
                        String string = key + "_" + md5;
                        Object object = getValueByPath(e, urlJsonKey.split(","));
                        if (ObjUtil.isNotEmpty(object)) {
                            String imgUrl = object.toString();
                            //判断是否为前端资源图片 如果是就不需要导出
                            if (imgUrl.startsWith("/jvs-public")) {
                                //下载图片
                                //获取后缀名
                                String extName = FileUtil.extName(imgUrl);
                                String fileImgUrl = DOWN_FILE_PATH + separator + string + "." + extName;
                                chartBackImgDto.setMinioUrl(imgUrl)
                                        .setFielUrl(fileImgUrl);
                                try {
                                    ZipUtil.downloadFile(endpoint + imgUrl, commonConfig.getDownPath() + separator + fileImgUrl);
                                    backImgList.add(chartBackImgDto);
                                } catch (IOException ex) {
                                    log.info("url：{}，图表下载附件错误:", endpoint + imgUrl, ex);
                                    throw new BusinessException("下载附件错误");
                                }
                            }
                        }
                    }
                });
        if (!backImgList.isEmpty()) {
            json.put("backImg", backImgList);
        }
        //json保存 包含设计数据与图片关联信息
        json.put("dataJson", jsonObject);
        ZipUtil.saveFile(json.toJSONString(), commonConfig.getDownPath() + separator + DOWN_FILE_PATH + separator + DOWN_CHART_FILE_NAME);
    }

    @Override
    public String getJsonPath() {
        String separator = File.separator;
        return commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator + DOWN_CHART_FILE_NAME;
    }

    @Override
    public String up(String nameOrId, UserDto userDto) {
        String separator = File.separator;
        String filePathPrefix = commonConfig.getUnzipPath() + separator + DOWN_FILE_PATH + separator;
        //基础数据类型
        String charJsonPath = filePathPrefix + DOWN_CHART_FILE_NAME;
        JSONObject dataJson = JSONObject.parseObject(FileUtil.readUtf8String(charJsonPath));
        //获取设计数据
        String chartJson = dataJson.getString("dataJson");
        //获取附件
        if (dataJson.containsKey("backImg")) {
            List<ChartBackImgDto> backImg = dataJson.getJSONArray("backImg").toJavaList(ChartBackImgDto.class);
            for (ChartBackImgDto chartBackImgDto : backImg) {
                BufferedInputStream inputStream = FileUtil.getInputStream(commonConfig.getUnzipPath() + separator + chartBackImgDto.getFielUrl());
                BaseFile baseFile = ossTemplate.put("jvs-public", "jvs-bi", inputStream, FileUtil.extName(chartBackImgDto.getFielUrl()), Boolean.FALSE);
                chartJson = chartJson.replace(chartBackImgDto.getMinioUrl(), "/" + baseFile.getBucketName() + "/" + baseFile.getFileName());
                try {
                    inputStream.close();
                } catch (Exception exception) {
                    log.error("图表导入时 关闭流文件失败", exception);
                    throw new BusinessException("图表导入时 关闭流文件失败");
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject chartObj = JSONObject.parseObject(chartJson);
        jsonObject.put("data", chartObj);
        jsonObject.put("userDto", userDto);
        jvsChartApi.up(jsonObject, nameOrId);
        //获取名称
        return chartObj.getString("name");
    }

    /**
     * 获取json中某个key的值
     *
     * @param path       key值路径
     * @param jsonObject 判断对象
     * @return 值
     */
    private static Object getValueByPath(JSONObject jsonObject, String... path) {
        //防止元数据被修改
        JSONObject current = JSONObject.parseObject(JSONObject.toJSONString(jsonObject));
        for (int i = 0; i < path.length; i++) {
            String key = path[i];
            if (current != null && current.containsKey(key)) {
                if (i + 1 == path.length) {
                    return current.getString(key);
                }
                current = current.getJSONObject(key);
            } else {
                return null;
            }
        }
        // 返回最终结果，可能是JSONObject或者其它值
        return current;
    }

    @Override
    public List<DownIdTypeDto> downGetId(String id, Boolean isMock) {
        List<DownIdTypeDto> list = new ArrayList<>(3);
        //图表id
        list.add(new DownIdTypeDto().setType(TaskTypeEnums.chart).setId(Arrays.asList(id)));
        if (isMock) {
            return list;
        }
        //获取数据集id
        List<String> data = jvsChartApi.getDataFactoryId(id).getData();
        log.info("图表获取数据来源完成");
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
        log.info("获取的数据来源为:{}",JSONObject.toJSONString(list));
        return list;
    }


}
