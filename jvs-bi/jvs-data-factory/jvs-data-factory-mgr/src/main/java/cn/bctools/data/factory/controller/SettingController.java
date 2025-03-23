package cn.bctools.data.factory.controller;


import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.dto.CronDto;
import cn.bctools.data.factory.entity.enums.CronEnum;
import cn.bctools.data.factory.im.MessageImPush;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Api(tags = "系统参数的获取")
@RestController
@AllArgsConstructor
@RequestMapping("/setting")
@Slf4j
public class SettingController {
    private final MessageImPush messageImPush;

    @ApiOperation("获取cron表达式")
    @GetMapping("/cron")
    public R<List<CronDto>> cron() {
        List<CronDto> list = Arrays.stream(CronEnum.values())
                .map(e -> new CronDto().setName(e.getName()).setCron(e))
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @ApiOperation("获取im群组id")
    @GetMapping("/im/group")
    public R<String> getGroupId() {
        String groupId = messageImPush.getGroupId(TenantContextHolder.getTenantId());
        return R.ok(groupId);
    }
}
