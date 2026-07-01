package cn.bctools.design.project;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.jvslog.service.impl.JvsLogServiceImpl;
import cn.bctools.design.project.dto.AppVersionSubmitBetaDto;
import cn.bctools.design.project.dto.AppVersionSwitchDto;
import cn.bctools.design.project.dto.VersionIterationBaseDto;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppTemplate;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.handler.AppTemplateTaskProgressHandler;
import cn.bctools.design.project.service.JvsAppService;
import cn.bctools.design.project.service.JvsAppTemplateService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
@Api(tags = "应用版本")
@RestController
@RequestMapping("/app/design/{appId}/version")
@AllArgsConstructor
public class JvsAppVersionController {
    private final JvsAppVersionService service;
    private final JvsAppTemplateService templateService;
    private final JvsAppService appService;
    private final AppTemplateTaskProgressHandler templateTaskProgressHandler;

    @ApiOperation("获取版本详情")
    @GetMapping("/detail/{versionId}")
    public R<JvsAppVersion> detail(@PathVariable("appId") String appId, @PathVariable("versionId") String versionId) {
        return R.ok(service.getById(versionId));
    }

    @ApiOperation("获取应用使用中的版本")
    @GetMapping("/use")
    public R<List<JvsAppVersion>> appVersion(@PathVariable("appId") String appId) {
        // 根据应用id获取“所属应用唯一标识”
        String affiliationApp = service.getAffiliationAppByAppId(appId);
        return R.ok(service.list(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getAffiliationApp, affiliationApp)
                .ne(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY)));
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("提交测试")
    @PostMapping("/submit/beta")
    public R<Boolean> submitBeta(@PathVariable("appId") String appId, @Validated @RequestBody AppVersionSubmitBetaDto dto) {
        JvsApp jvsApp = Optional.ofNullable(appService.getById(appId)).orElseThrow(() -> new BusinessException("应用不存在"));
        // 校验是否可迭代
        String locKey = templateService.checkCanAppIterator(appId, AppVersionTypeEnum.BETA);
        // 提交测试
        templateService.submitBeta(locKey, jvsApp, dto);
        return R.ok(true);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("正式发布")
    @PostMapping("/publish/{versionId}")
    public R<Boolean> publish(@PathVariable("appId") String appId, @PathVariable("versionId") String versionId, @Validated @RequestBody VersionIterationBaseDto dto) {
        // 获取待上线的版本
        JvsAppVersion sourceVersion = Optional.of(service.getById(versionId)).orElseThrow(() -> new BusinessException("版本不存在"));
        if (appId.equals(sourceVersion.getAffiliationApp())) {
            throw new BusinessException("该版本不能发布到当前应用");
        }
        if (Boolean.FALSE.equals(AppVersionTypeEnum.BETA.equals(sourceVersion.getVersionType()))) {
            throw new BusinessException("非测试版不能正式发布");
        }
        if (Boolean.FALSE.equals(AppVersionStatusEnum.USE.equals(sourceVersion.getVersionStatus()))) {
            throw new BusinessException("该版本不能正式发布");
        }
        JvsApp jvsApp = Optional.ofNullable(appService.getById(sourceVersion.getJvsAppId())).orElseThrow(() -> new BusinessException("应用不存在"));
        // 校验是否可迭代
        String locKey = templateService.checkCanAppIterator(sourceVersion.getAffiliationApp(), AppVersionTypeEnum.GA);
        // 提交到正式
        templateService.submitGa(locKey, jvsApp, sourceVersion, dto);
        return R.ok(true);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "暂停服务", notes = "正式版本可以暂停服务")
    @PostMapping("/suspend")
    public R<Boolean> suspend(@PathVariable("appId") String appId) {
        if (Boolean.FALSE.equals(AppVersionTypeEnum.GA.equals(ModeUtils.getMode()))) {
            throw new BusinessException("非正式模式的应用不支持该功能");
        }
        // 卸载应用
        appService.unload(appId);
        // 修改版本状态
        JvsAppVersion version = service.getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getJvsAppId, appId)
                // 正式版本
                .eq(JvsAppVersion::getVersionType, AppVersionTypeEnum.GA)
                // 启用状态
                .eq(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.USE));
        if (ObjectNull.isNull(version)) {
            return R.ok();
        }
        version.setVersionStatus(AppVersionStatusEnum.SUSPEND);
        service.updateById(version);
        return R.ok(true);
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation(value = "启用服务", notes = "正式版本可以启用服务")
    @PostMapping("/use")
    public R<Boolean> use(@PathVariable("appId") String appId) {
        if (Boolean.FALSE.equals(AppVersionTypeEnum.GA.equals(ModeUtils.getMode()))) {
            throw new BusinessException("非正式模式的应用不支持该功能");
        }
        // 发布应用
        appService.deploy(appId);
        // 修改版本状态
        JvsAppVersion version = service.getOne(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getJvsAppId, appId)
                // 正式版本
                .eq(JvsAppVersion::getVersionType, AppVersionTypeEnum.GA)
                // 暂停状态
                .eq(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.SUSPEND));
        if (ObjectNull.isNull(version)) {
            return R.ok();
        }
        version.setVersionStatus(AppVersionStatusEnum.USE);
        service.updateById(version);
        return R.ok(true);
    }

    @ApiOperation("历史版本")
    @GetMapping("/history/{versionType}")
    public R<List<JvsAppVersion>> switchVersion(@PathVariable("appId") String appId, @PathVariable("versionType") AppVersionTypeEnum versionType) {
        return R.ok(service.list(Wrappers.<JvsAppVersion>lambdaQuery()
                .eq(JvsAppVersion::getJvsAppId, appId)
                .eq(JvsAppVersion::getVersionType, versionType)
                .eq(JvsAppVersion::getVersionStatus, AppVersionStatusEnum.HISTORY)
                .orderByDesc(JvsAppVersion::getCreateTime)
                .last("limit 20")));
    }

    @Log(callBackClass = JvsLogServiceImpl.class)
    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("切换版本")
    @PostMapping("/switch/version")
    public R<Boolean> switchVersion(@PathVariable("appId") String appId, @Validated @RequestBody AppVersionSwitchDto dto) {
        // 获取目标版本
        JvsAppVersion sourceVersion = Optional.of(service.getById(dto.getVersionId())).orElseThrow(() -> new BusinessException("版本不存在"));
        if (Boolean.FALSE.equals(sourceVersion.getVersionType().equals(dto.getVersionType()))) {
            throw new BusinessException("回退版本失败");
        }
        if (AppVersionTypeEnum.DEV.equals(sourceVersion.getVersionType())) {
            throw new BusinessException("开发版本不能回退");
        }
        // 校验是否可迭代
        String locKey = templateService.checkCanAppIterator(sourceVersion.getAffiliationApp(), dto.getVersionType());
        // 初始化任务进度
        JvsApp jvsApp = appService.getAppById(appId);
        JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(locKey, "版本回退(" + jvsApp.getName() + ")", sourceVersion.getAffiliationApp());
        templateTaskProgressHandler.addProgress(templateTaskProgress.getId(), AppTemplateTaskProgressDetailEnum.SUMMARY, AppTemplateTaskProgressEnum.SUCCESS, null, "回退到" + sourceVersion.getAppVersion() + "版本");

        // 切换版本
        templateService.asyncSubmitVersion(locKey, VersionIterationTypeEnum.SWITCH_VERSION, templateTaskProgress, sourceVersion, dto.getVersionType());
        return R.ok(true);
    }

    @SneakyThrows
    @ApiOperation("下载版本")
    @GetMapping("/download/{versionId}")
    public void download(@PathVariable("appId") String appId, @PathVariable String versionId, HttpServletResponse response) {
        ServletOutputStream outputStream = response.getOutputStream();
        JvsAppVersion appVersion = Optional.ofNullable(service.getById(versionId)).orElseThrow(() -> new BusinessException("版本不存在"));
        String templateId = appVersion.getTemplateId();
        JvsAppTemplate jvsAppTemplate = Optional.ofNullable(templateService.getById(templateId)).orElseThrow(() -> new BusinessException("没有找到模板"));
        String data = templateService.getData(jvsAppTemplate);
        jvsAppTemplate.setId(null);
        jvsAppTemplate.setCreateTime(null);
        jvsAppTemplate.setData(data);
        appVersion.setJvsAppTemplate(jvsAppTemplate);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String fileName = jvsAppTemplate.getName() + appVersion.getAppVersion() + StringPool.DASH + appVersion.getVersionType().getValue() + ".jvs";
        String s1 = "attachment; filename=".concat(URLUtil.encode(fileName, StandardCharsets.UTF_8));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, s1);
        response.setStatus(HttpStatus.OK.value());

        Map<String, Object> map = BeanCopyUtil.beanToMap(appVersion);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream);
            jsonGenerator.writeStartObject();
            Field[] fields = JvsAppVersion.class.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                jsonGenerator.writeFieldName(name);
                jsonGenerator.writeObject(map.get(name));
            }
            jsonGenerator.writeEndObject();
            jsonGenerator.flush();
            jsonGenerator.close();
        } catch (IOException e) {
            throw new BusinessException("下载失败", e.getMessage());
        }
    }
}
