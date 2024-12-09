package cn.bctools.design.project;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.project.dto.DesignRoleSettingDto;
import cn.bctools.design.project.entity.JvsAppTemplate;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.mapper.JvsAppTemplateDataMapper;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppTemplateService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "应用模板")
@RestController
@AllArgsConstructor
@RequestMapping("/base/JvsAppTemplate")
public class JvsAppTemplateController {

    /**
     * 返回推荐应用数量上限
     */
    private static final Integer RECOMMEND_MAX = 20;

    JvsAppTemplateService templateService;

    JvsAppTemplateDataMapper templateDataMapper;
    JvsAppService appService;


    @ApiOperation("获取banner")
    @GetMapping("/banner")
    public R<List<JvsAppTemplate>> banner() {
        List<JvsAppTemplate> list = getList(new LambdaQueryWrapper<JvsAppTemplate>()
                .isNotNull(JvsAppTemplate::getBanner)
                .eq(JvsAppTemplate::getDeploy, true)
                .eq(JvsAppTemplate::getVersionTemplate, Boolean.FALSE)
                .select(JvsAppTemplate::getId, JvsAppTemplate::getBanner));
        return R.ok(list);
    }

    @ApiOperation("推荐应用")
    @GetMapping("/recommend")
    public R<List<JvsAppTemplate>> recommend(@RequestParam("size") int size) {
        if (size >= RECOMMEND_MAX) {
            size = 20;
        }
        List<JvsAppTemplate> list = getList(new LambdaQueryWrapper<JvsAppTemplate>()
                .eq(JvsAppTemplate::getDeploy, true)
                .eq(JvsAppTemplate::getRecommend, true)
                .eq(JvsAppTemplate::getVersionTemplate, Boolean.FALSE)
                .select(JvsAppTemplate::getId, JvsAppTemplate::getName, JvsAppTemplate::getLogo, JvsAppTemplate::getBriefDescription)
                .orderByDesc(JvsAppTemplate::getCreateTime)
                //只显示前10个
                .last(" limit " + size));
        if (ObjectNull.isNull(list)) {
            //如果没有则默认返回 10个
            list = getList(new LambdaQueryWrapper<JvsAppTemplate>()
                    .eq(JvsAppTemplate::getVersionTemplate, Boolean.FALSE)
                    .select(JvsAppTemplate::getId, JvsAppTemplate::getName, JvsAppTemplate::getLogo, JvsAppTemplate::getBriefDescription)
                    .orderByDesc(JvsAppTemplate::getCreateTime)
                    //只显示前10个
                    .last(" limit " + size));
        }
        return R.ok(list);
    }

    private List<JvsAppTemplate> getList(LambdaQueryWrapper<JvsAppTemplate> size) {
        return templateService.list(size);
    }

    @ApiOperation("获取类型")
    @GetMapping("/types")
    public R types() {
        Boolean equals = true;
        List<String> collect = getList(new LambdaQueryWrapper<JvsAppTemplate>()
                .select(JvsAppTemplate::getType)
                .eq(JvsAppTemplate::getVersionTemplate, Boolean.FALSE)
        )
                .stream()
                .map(JvsAppTemplate::getType)
                .distinct()
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    @ApiOperation("查询模板详情")
    @GetMapping("/detail/{templateId}")
    public R getDetail(@PathVariable("templateId") String templateId) {
        JvsAppTemplate template = templateService.getOne(new LambdaQueryWrapper<JvsAppTemplate>().select(JvsAppTemplate.class, e -> !e.getColumn().equals(Get.name(JvsAppTemplate::getData))).eq(JvsAppTemplate::getId, (templateId)));
        return R.ok(template);
    }

    @ApiOperation("获取模板数据")
    @GetMapping("/data/{templateId}")
    public R getData(@PathVariable("templateId") String templateId) {

        JvsAppTemplate template = templateService.getOne(Wrappers.query(new JvsAppTemplate().setId(templateId)));
        if (Objects.isNull(template)) {
            return R.ok(null);
        }
        //如果是免费的直接返回，收费的不返回
        if (template.getFree()) {
            return R.ok(template);
        }
        //获取数据
        template.setData(templateService.getData(template));
        return R.ok(template);
    }

    @ApiOperation("查询所有模板")
    @GetMapping("/list")
    public Object page(Page<JvsAppTemplate> page, JvsAppTemplate dto) {
        dto.setSize(null);
        LambdaQueryWrapper<JvsAppTemplate> jvsAppTemplateLambdaQueryWrapper = new LambdaQueryWrapper<JvsAppTemplate>()
                .eq(JvsAppTemplate::getVersionTemplate, Boolean.FALSE)
                .eq(ObjectNull.isNotNull(dto.getType()), JvsAppTemplate::getType, dto.getType())
                .like(ObjectNull.isNotNull(dto.getName()), JvsAppTemplate::getName, dto.getName());
        jvsAppTemplateLambdaQueryWrapper.select(JvsAppTemplate::getId,
                JvsAppTemplate::getName,
                JvsAppTemplate::getCreateTime,
                JvsAppTemplate::getType,
                JvsAppTemplate::getSize,
                JvsAppTemplate::getIcon,
                JvsAppTemplate::getDescription,
                JvsAppTemplate::getImgs,
                JvsAppTemplate::getLogo,
                JvsAppTemplate::getBanner,
                JvsAppTemplate::getVersion);
        jvsAppTemplateLambdaQueryWrapper.orderByDesc(JvsAppTemplate::getCreateTime);
        templateService.page(page, jvsAppTemplateLambdaQueryWrapper);
        return R.ok(page);

    }

    @ApiOperation("发布原生应用到模板")
    @PostMapping("/deploy/primitive")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> primitive(@RequestBody JvsAppTemplate jvsAppTemplate) {
        jvsAppTemplate.setPrimitive(true);
        templateService.save(jvsAppTemplate);
        return R.ok(true);
    }

    @SneakyThrows
    @ApiOperation("发布到模板")
    @PostMapping("/deploy")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deploy(@RequestBody JvsAppTemplate jvsAppTemplate) {
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(jvsAppTemplate.getId()).setJvsAppName(jvsAppTemplate.getName()));
        jvsAppTemplate.setVersion(SpringContextUtil.getVersion());
        templateService.saveTemplate(jvsAppTemplate);
        return R.ok(true);
    }

    @ApiOperation("上传模板中心")
    @PostMapping("/client/template")
    @Transactional(rollbackFor = Exception.class)
    public R client(@RequestBody JvsAppTemplate jvsAppTemplate) {
        templateService.saveUploadTemplate(jvsAppTemplate);
        return R.ok(true, "编辑成功");
    }

    @ApiOperation("模板-编辑")
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public R put(@RequestBody JvsAppTemplate jvsAppTemplate) {
        templateService.updateById(jvsAppTemplate);
        return R.ok(true, "编辑成功");
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @ApiOperation("模板-删除")
    @DeleteMapping("/del/{templateId}")
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete(@PathVariable("templateId") String templateId) {
        JvsAppTemplate jvsAppTemplate = templateService.getById(templateId);
        if (ObjectNull.isNull(jvsAppTemplate)) {
            return R.ok();
        }
        DynamicDataUtils.setDto(new DesignRoleSettingDto().setJvsAppId(jvsAppTemplate.getId()).setJvsAppName(jvsAppTemplate.getName()));
        templateService.removeById(templateId);
        templateService.removeTemplateData(jvsAppTemplate);
        return R.ok(true, "删除成功");
    }

    static String[] list = new String[]{"id", "name", "type", "appIcon", "version", "banner", "imgs", "data"};

    @SneakyThrows
    @ApiOperation("下载模板")
    @GetMapping("/download/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) {
        JvsAppTemplate jvsAppTemplate = templateService.getById(id);
        if (ObjectNull.isNull(jvsAppTemplate)) {
            throw new BusinessException("没有找到模板");
        }
        String data = templateService.getData(jvsAppTemplate);
        jvsAppTemplate.setId(null);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(URLUtil.encode(jvsAppTemplate.getName() + SpringContextUtil.getVersion() + ".jvs", StandardCharsets.UTF_8)));
        response.setStatus(HttpStatus.OK.value());
        {
            Map<String, Object> map = BeanCopyUtil.beanToMap(jvsAppTemplate);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream);
                jsonGenerator.writeStartObject();
                for (int i = 1; i < list.length; i++) {
                    String name = list[i];
                    jsonGenerator.writeFieldName(name);
                    if ("data".equals(name)) {
                        jsonGenerator.writeString(data);
                    } else {
                        jsonGenerator.writeObject(map.get(name));
                    }
                }
                jsonGenerator.writeEndObject();
                jsonGenerator.flush();
                jsonGenerator.close();
            } catch (IOException ignored) {
            }
        }
    }

    @SneakyThrows
    @ApiOperation("上传模板")
    @PostMapping("/fileUpload")
    @Transactional(rollbackFor = Exception.class)
    public R fileUpload(@RequestParam("file") MultipartFile file) {
        //如果是平台管理，则直接将模板上传到应用中心，如果是其它平台，则直接将其上传的模板文件，创建为应用
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String s = new String(bytes);
        JvsAppTemplate jvsAppTemplate = null;
        try {
            jvsAppTemplate = JSONObject.parseObject(s, JvsAppTemplate.class);
        } catch (Exception e) {
            return R.failed("文件异常请使用jvs的应用文件");
        }
        templateService.saveUploadTemplate(jvsAppTemplate);
        return R.ok();
    }


    @SneakyThrows
    @ApiOperation("上传模板创建应用")
    @PostMapping("/fileUpload/app")
    @Transactional(rollbackFor = Exception.class)
    public R fileUploadApp(@RequestParam("file") MultipartFile file) {
        // 开发模式可以上传模板创建应用
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(ModeUtils.getMode()))) {
            throw new BusinessException("请切换到开发模式");
        }
        //如果是平台管理，则直接将模板上传到应用中心，如果是其它平台，则直接将其上传的模板文件，创建为应用
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String s = new String(bytes);
        JvsAppTemplate jvsAppTemplate = null;
        try {
            jvsAppTemplate = JSONObject.parseObject(s, JvsAppTemplate.class);
        } catch (Exception e) {
            return R.failed("文件异常请使用jvs的应用文件");
        }
        if (ObjectNull.isNull(jvsAppTemplate.getData())) {
            return R.failed("文件异常请使用jvs的应用文件");
        }

        templateService.uploadTemplateCreateApp(jvsAppTemplate, file.getOriginalFilename());
        return R.ok();
    }
}
