package cn.bctools.design.project;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.project.dto.AppTemplateTaskProgressResDto;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.log.annotation.Log;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hrl
 */
@Api(tags = "模板创建应用或迭代应用进度")
@RestController
@RequestMapping("/app/design/{appId}/template/task/progress")
@AllArgsConstructor
public class JvsAppTemplateTaskProgressController {

    private final JvsAppTemplateTaskProgressService templateTaskProgressService;
    private final JvsAppVersionService appVersionService;

    @Log
    @ApiOperation("查询指定应用最近的迭代进度")
    @GetMapping()
    public R<List<AppTemplateTaskProgressResDto>> progress(@PathVariable String appId, AppTemplateTaskProgressEnum progress) {
        // 根据应用id获取“所属应用唯一标识”
        String affiliationApp = appVersionService.getAffiliationAppByAppId(appId);
        List<JvsAppTemplateTaskProgress> templateTaskProgresses = templateTaskProgressService
                .list(Wrappers.<JvsAppTemplateTaskProgress>lambdaQuery()
                        .eq(JvsAppTemplateTaskProgress::getJvsAppId, affiliationApp)
                        .eq(JvsAppTemplateTaskProgress::getProgress, progress)
                        .orderByDesc(JvsAppTemplateTaskProgress::getCreateTime)
                        .last("limit 10"));
        if (ObjectNull.isNull(templateTaskProgresses)) {
            return R.ok();
        }
        List<AppTemplateTaskProgressResDto> res = BeanCopyUtil.copys(templateTaskProgresses, AppTemplateTaskProgressResDto.class);
        templateTaskProgressService.calculateProgress(res);
        return R.ok(res);
    }



}
