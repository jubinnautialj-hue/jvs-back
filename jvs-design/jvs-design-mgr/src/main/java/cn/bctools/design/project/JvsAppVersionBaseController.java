package cn.bctools.design.project;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.handler.AppTemplateTaskProgressHandler;
import cn.bctools.design.project.service.JvsAppTemplateService;
import cn.bctools.design.util.ModeUtils;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhuxiaokang
 */
@Api(tags = "应用版本")
@RestController
@RequestMapping("/base/app/version")
@AllArgsConstructor
public class JvsAppVersionBaseController {

    private final JvsAppTemplateService templateService;
    private final AppTemplateTaskProgressHandler templateTaskProgressHandler;

    @SneakyThrows
    @ApiOperation("上传版本")
    @PostMapping("/fileUpload")
    @Transactional(rollbackFor = Exception.class)
    public R fileUpload(@RequestParam("file") MultipartFile file) {
        // 测试模式可以上传版本
        if (Boolean.FALSE.equals(AppVersionTypeEnum.BETA.equals(ModeUtils.getMode()))) {
            throw new BusinessException("请切换到测试模式");
        }
        byte[] bytes = IoUtil.readBytes(file.getInputStream());
        String s = new String(bytes);
        JvsAppVersion jvsAppVersion = null;
        try {
            jvsAppVersion = JSONObject.parseObject(s, JvsAppVersion.class);
        } catch (Exception e) {
            return R.failed("文件异常请使用jvs的应用版本文件");
        }
        if (ObjectNull.isNull(jvsAppVersion.getJvsAppTemplate())) {
            return R.failed("文件异常请使用jvs的应用版本文件");
        }

        // 校验是否可迭代
        String locKey = templateService.checkCanAppIterator(jvsAppVersion.getAffiliationApp(), AppVersionTypeEnum.BETA);
        // 初始化任务进度
        JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(locKey, "上传测试版本文件(" + file.getOriginalFilename() + ")", jvsAppVersion.getAffiliationApp());
        // 上传版本，只能上传到测试版
        templateService.asyncSubmitVersion(locKey, VersionIterationTypeEnum.UPLOAD_VERSION, templateTaskProgress, jvsAppVersion, AppVersionTypeEnum.BETA);
        return R.ok();
    }
}
