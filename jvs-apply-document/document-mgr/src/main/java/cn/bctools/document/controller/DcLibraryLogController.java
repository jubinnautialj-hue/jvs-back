package cn.bctools.document.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryLog;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "业务日志")
@RestController
@RequestMapping(value = "/dcLibrary/log")
@AllArgsConstructor
public class DcLibraryLogController {

    private final DcLibraryLogService dcLibraryLogService;
    private final AuthUserServiceApi authUserServiceApi;
    private final DcAuthConfigService dcAuthConfigService;
    private final DcLibraryService dcLibraryService;

    @Log(back = false)
    @ApiOperation("分页查询日志")
    @GetMapping("/page")
    public R<Page<DcLibraryLog>> page(Page<DcLibraryLog> page, DcLibraryLog dcLibraryLog) {
        LambdaQueryWrapper<DcLibraryLog> queryWrapper = new LambdaQueryWrapper<DcLibraryLog>()
                .orderByDesc(DcLibraryLog::getCreateTime);
        if (StrUtil.isNotEmpty(dcLibraryLog.getUserId())) {
            queryWrapper.eq(DcLibraryLog::getUserId, dcLibraryLog.getUserId());
        }
        DcLibrary dcLibrary = dcLibraryService.getById(dcLibraryLog.getDcLibraryId());
        List<String> list;
        if (DcLibraryTypeEnum.isFile(dcLibrary.getType())) {
            list = Arrays.asList(dcLibrary.getId());
            queryWrapper.in(DcLibraryLog::getDcLibraryId, list);
        }else {
            queryWrapper.eq(DcLibraryLog::getKnowledgeId,dcLibraryLog.getDcLibraryId());
        }
        dcLibraryLogService.page(page, queryWrapper);
        if (!page.getRecords().isEmpty()) {
            //获取用户信息
            List<String> userIds = page.getRecords().parallelStream().map(DcLibraryLog::getUserId).distinct().collect(Collectors.toList());
            Map<String, UserDto> userMap = authUserServiceApi.getByIds(userIds).getData().parallelStream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
            page.getRecords().stream()
                    .peek(e -> e.setUserDto(userMap.getOrDefault(e.getUserId(), new UserDto().setRealName(e.getUserName()))))
                    .peek(e -> {
                        String msg = SpringContextUtil.msg(e.getOperationType());
                        log.info("当前获取的配置为:{},当前语言为:{}",msg, LocaleContextHolder.getLocale());
                        e.setOperationType(msg);
                    })
                    .collect(Collectors.toList());
        }
        return R.ok(page);
    }


    @Log
    @ApiOperation("常用文档")
    @GetMapping("/frequently")
    public R<List<DcLibraryLog>> frequently(DcLibraryLog dcLibraryLog) {
        List<DcLibraryLog> frequently = dcLibraryLogService.frequently(dcLibraryLog);
        return R.ok(frequently);
    }

    @Log
    @ApiOperation("常用文库")
    @GetMapping("/frequently/knowledge")
    public R<List<DcLibrary>> frequentlyKnowledge() {
        List<DcLibrary> frequently = dcLibraryLogService.frequentlyKnowledge().stream().filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        return R.ok(frequently);
    }
}
