package cn.bctools.data.factory.controller;


import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.utils.R;
import cn.bctools.data.factory.notice.dto.DataNoticeDto;
import cn.bctools.data.factory.notice.dto.NoticeFieldDto;
import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
import cn.bctools.data.factory.notice.enums.TriggerTypeEnum;
import cn.bctools.data.factory.notice.po.DataNoticePo;
import cn.bctools.data.factory.notice.service.DataNoticeService;
import cn.bctools.data.factory.notice.service.MessageVariableService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
@Api(tags = "[jvs-data-factory]数据集-消息通知")
@RestController
@RequestMapping("/notice")
@AllArgsConstructor
public class NoticeController {
    private final AuthTenantConfigServiceApi tenantConfigApi;
    private final DataNoticeService dataNoticeService;
    private final MessageVariableService messageVariableService;

    @ApiOperation("支持的消息通知类型")
    @GetMapping("/trigger/list")
    public R<List<Map<String, String>>> getTriggerList() {
        List<Map<String, String>> collect = Arrays.stream(TriggerTypeEnum.values()).map(e -> {
            Map<String, String> item = new HashMap<>(2);
            item.put("type", e.getValue());
            item.put("label", e.getDesc());
            return item;
        }).collect(Collectors.toList());
        return R.ok(collect);
    }

    @ApiOperation("支持的消息通知类型")
    @GetMapping("/type")
    public R<List<Map<String, String>>> getType() {
        List<ConfigsTypeEnum> data = tenantConfigApi.keys().getData();
        List<NoticeTypeEnum> noticeTypeEnums = new ArrayList<>();
        noticeTypeEnums.add(NoticeTypeEnum.SYSTEM);
        if (data.contains(ConfigsTypeEnum.SMS_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.SMS);
        }
        if (data.contains(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.WECHAT_MP);
        }
        if (data.contains(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.WECHAT_ENTERPRISE);
        }
        if (data.contains(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.DING);
        }
        if (data.contains(ConfigsTypeEnum.MAIL_CONFIGURATION)) {
            noticeTypeEnums.add(NoticeTypeEnum.EMAIL);
        }
        //根据已经配置了的配置，返回支持的消息类型
        List<Map<String, String>> enableAll = NoticeTypeEnum.getEnableAll(noticeTypeEnums::contains);
        return R.ok(enableAll);
    }

    @ApiOperation("查询数据集的消息配置")
    @GetMapping("/{dataFactoryId}/list")
    public R<List<DataNoticePo>> getAll(@ApiParam("数据集id") @PathVariable("dataFactoryId") String dataFactoryId) {
        List<DataNoticePo> list = dataNoticeService.list(Wrappers.lambdaQuery(DataNoticePo.class).eq(DataNoticePo::getDataFactoryId, dataFactoryId));
        return R.ok(list);
    }

    @ApiOperation("保存消息配置")
    @PostMapping("/save")
    public R save(@Validated @RequestBody DataNoticeDto dto) {
        dataNoticeService.saveDataNotice(dto);
        return R.ok();
    }

    @ApiOperation("删除消息配置")
    @DeleteMapping("/{dataFactoryId}/{settingId}/remove")
    public R<Boolean> del(@ApiParam("数据集id") @PathVariable("dataFactoryId") String dataFactoryId, @ApiParam("消息配置id") @PathVariable("settingId") String settingId) {
        return R.ok(dataNoticeService.remove(Wrappers.lambdaQuery(DataNoticePo.class).eq(DataNoticePo::getDataFactoryId, dataFactoryId).eq(DataNoticePo::getId, settingId)));
    }

    @ApiOperation("查询可选字段")
    @GetMapping("/fields")
    public R<List<NoticeFieldDto>> getField() {
        List<NoticeFieldDto> noticeFieldDtos = messageVariableService.list()
                .stream()
                .map(e -> new NoticeFieldDto().setKey(e.getVariableName()).setName(e.getVariableExplain()))
                .collect(Collectors.toList());
        return R.ok(noticeFieldDtos);
    }


}