package cn.bctools.bi.service.impl;

import cn.bctools.bi.config.CommonConfig;
import cn.bctools.bi.dto.ChartBackImgDto;
import cn.bctools.bi.dto.DownIdTypeDto;
import cn.bctools.bi.entity.enums.TaskTypeEnums;
import cn.bctools.bi.service.DownOrUpService;
import cn.bctools.bi.util.ZipUtil;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.api.dto.SourceFactoryGetIdDto;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.props.OssProperties;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.screen.api.ScreenLineageViewApi;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class ScreenDownOrUpServiceImpl implements DownOrUpService {

    private final ScreenLineageViewApi screenLineageViewApi;

    private final OssProperties ossProperties;
    private final OssTemplate ossTemplate;
    private final CommonConfig commonConfig;
    private final DataFactoryApi dataFactoryApi;
    private final static String DOWN_FILE_PATH = "screen";
    private final static String DOWN_CHART_FILE_NAME = "screen.json";

    /**
     * 图表设计的附件路径
     */
    private final static List<String> CANVAS_URL_JSON_KEY = new ArrayList<String>() {{

        add("customJson.backOption.uploadImgList");
        add("customJson.backOption.backImg");

        add("dataJson[*].setting.bgImg");
        add("dataJson[*].setting.backImg");
        add("dataJson[*].setting.defaultUrl");
        add("dataJson[*].setting.selected.imgUrl");
        add("dataJson[*].setting.selected.backImg");
        add("dataJson[*].setting.notSelected.imgUrl");
        add("dataJson[*].setting.notSelected.backImg");
        add("dataJson[*].setting.rows[*].bgImg");
        add("dataJson[*].imgUrl");
        add("dataJson[*].videoUrl");

        add("imgUrl");

        add("videoUrl");

        add("imgList");
    }};


    @Override
    public void downFile(List<String> id, Boolean isMock) {
        String endpoint = ossProperties.getEndpoint().startsWith("http") ? ossProperties.getEndpoint() : "http://" + ossProperties.getEndpoint();
        String key = id.get(0);
        JSONObject jsonObject = screenLineageViewApi.downFile(key, isMock).getData();

        //图片关联关系
        JSONObject json = new JSONObject();
        String separator = File.separator;
        //创建目录
        FileUtil.mkdir(commonConfig.getDownPath() + separator + DOWN_FILE_PATH);
        List<ChartBackImgDto> backImgList = new ArrayList<>();

        JSONObject base = jsonObject.getJSONObject("base");

        /*基础背景图*/
        List<ChartBackImgDto> baseInfoFileDTOList = buildFileDto("imgUrl", base, endpoint, separator, key);
        if (CollectionUtil.isNotEmpty(baseInfoFileDTOList)) {
            backImgList.addAll(baseInfoFileDTOList);
        }

        /*画布文件*/
        JSONArray canvas = jsonObject.getJSONArray("canvas");
        canvas.stream().map(e -> JSONObject.parseObject(JSONObject.toJSONString(e)))
                .forEach(e -> {
                    for (String urlJsonKey : CANVAS_URL_JSON_KEY) {
                        List<ChartBackImgDto> fileDTOList = buildFileDto(urlJsonKey, e, endpoint, separator, key);
                        if (CollectionUtil.isNotEmpty(fileDTOList)) {
                            backImgList.addAll(fileDTOList);
                        }
                    }
                });

        if (CollectionUtil.isNotEmpty(backImgList)) {
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
                    log.error("大屏导入时 关闭流文件失败", exception);
                    throw new BusinessException("大屏导入时 关闭流文件失败");
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject chartObj = JSONObject.parseObject(chartJson);
        jsonObject.put("data", chartObj);
        jsonObject.put("userDto", userDto);
        screenLineageViewApi.up(jsonObject, nameOrId);
        //获取名称
        return Optional.ofNullable(chartObj).map(e -> e.getJSONObject("base")).map(e -> e.getString("name")).orElse("大屏");
    }

    private List<ChartBackImgDto> buildFileDto(String urlJsonKey, JSONObject data, String ossEndpoint, String separator, String key) {
        Object read = JvsJsonPath.read(data, urlJsonKey);
        if (ObjUtil.isNotEmpty(read)) {
            //前端静态文件不需要下载
            String fileLink = read.toString();
            if (JSONUtil.isTypeJSONArray(fileLink)) {
                cn.hutool.json.JSONArray objects = JSONUtil.parseArray(fileLink);
                return objects.stream().map(e -> createDTO(e.toString(), ossEndpoint, separator, key)).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
            }
            ChartBackImgDto dto = createDTO(fileLink, ossEndpoint, separator, key);
            if (dto != null) {
                return Collections.singletonList(dto);
            }
        }
        return null;
    }

    public ChartBackImgDto createDTO(String fileLink, String ossEndpoint, String separator, String key) {
        ChartBackImgDto chartBackImgDto = new ChartBackImgDto();
        System.out.println(fileLink);
        String fileUrl = "";
        if (fileLink.startsWith("http://") || fileLink.startsWith("https://")) {
            fileUrl = fileLink;
        }
        if (fileLink.startsWith("/jvs-public")) {
            fileUrl = ossEndpoint + fileLink;
        }
        if (StrUtil.isBlank(fileUrl)) {
            return null;
        }
        String custom = IdGenerator.getIdStr(36);
        String string = key + "_" + custom;
        //获取后缀名
        String extName = FileUtil.extName(fileLink);
        if(StrUtil.isBlank(extName)){
            return null;
        }
        String fileImgUrl = DOWN_FILE_PATH + separator + string + "." + extName;
        chartBackImgDto.setMinioUrl(fileLink)
                .setFielUrl(fileImgUrl);
        try {
            ZipUtil.downloadFile(fileUrl, commonConfig.getDownPath() + separator + fileImgUrl);
            return chartBackImgDto;
        } catch (IOException ex) {
            log.info("下载附件错误:", ex);
        }
        return null;
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
        list.add(new DownIdTypeDto().setType(TaskTypeEnums.screen).setId(Arrays.asList(id)));
        if (isMock) {
            return list;
        }
        //获取数据集id
        List<String> data = screenLineageViewApi.getDataFactoryId(id).getData();
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
