package cn.bctools.design.project;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.project.dto.AppTemplateTaskProgressResDto;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgressDetail;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressDetailService;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author hrl
 */
@Api(tags = "模板创建应用或迭代应用进度")
@RestController
@RequestMapping("/base/app/template/task/progress")
@AllArgsConstructor
public class JvsAppTemplateTaskProgressBaseController {


    private final JvsAppTemplateTaskProgressService templateTaskProgressService;
    private final JvsAppTemplateTaskProgressDetailService templateTaskProgressDetailService;

    @Log
    @ApiOperation("查询自己发起的最近的迭代任务进度")
    @GetMapping()
    public R<List<AppTemplateTaskProgressResDto>> progress(AppTemplateTaskProgressEnum progress) {
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        List<JvsAppTemplateTaskProgress> templateTaskProgresses = templateTaskProgressService.list(Wrappers.<JvsAppTemplateTaskProgress>lambdaQuery()
                .eq(JvsAppTemplateTaskProgress::getCreateById, userDto.getId())
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

    @Log
    @ApiOperation("查询进度详情")
    @GetMapping("/{taskId}/detail")
    public R<List<JvsAppTemplateTaskProgressDetail>> progressDetail(@PathVariable String taskId) {
        return R.ok(templateTaskProgressDetailService
                .list(Wrappers.<JvsAppTemplateTaskProgressDetail>lambdaQuery()
                        .eq(JvsAppTemplateTaskProgressDetail::getTaskId, taskId)
                        .orderByAsc(Arrays.asList(JvsAppTemplateTaskProgressDetail::getCreateTime, JvsAppTemplateTaskProgressDetail::getSerialNumber))));
    }


}
