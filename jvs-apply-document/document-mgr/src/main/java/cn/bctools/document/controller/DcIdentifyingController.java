package cn.bctools.document.controller;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.dto.AddAuthConfigDto;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.IdentifyingTypeEnum;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcIdentifyingService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限标识
 * <p>
 *
 * @author Auto Generator
 */
@Api(tags = "权限标识")
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class DcIdentifyingController {

    private final DcIdentifyingService dcIdentifyingService;
    private final DcAuthConfigService dcAuthConfigService;
    private final DcLibraryService dcLibraryService;

    @Log
    @ApiOperation("获取标识数据不包括所有者权限标识")
    @GetMapping("")
    public R<List<DcIdentifying>> list(@ApiParam("类型 如果不传 表示获取所有数据") IdentifyingTypeEnum type) {
        List<DcIdentifying> list = dcIdentifyingService.list(new LambdaQueryWrapper<DcIdentifying>()
                .eq(DcIdentifying::getIsSelect, Boolean.FALSE)
                .eq(DcIdentifying::getPossessorIs, false)
                .eq(ObjUtil.isNotNull(type), DcIdentifying::getIdentifyingType, type)
                .orderByDesc(DcIdentifying::getCreateTime));
        list.stream().peek(e -> e.setName(SpringContextUtil.msg(e.getIdentifyingName()))).collect(Collectors.toList());
        return R.ok(list);
    }

    @Log
    @ApiOperation("通过文档或者文库id获取当前的用户信息")
    @GetMapping("/user/{id}")
    public R<List<AddAuthConfigDto.UserInfo>> getByUserInfo(@ApiParam("文库或文档id") @PathVariable String id) {
        List<AddAuthConfigDto.UserInfo> user = dcAuthConfigService.getUser(id);
        return R.ok(user);
    }

    @Log
    @ApiOperation("查看某个文库或文档所在的权限与人员信息")
    @GetMapping("/{id}")
    public R<List<AddAuthConfigDto>> getAll(@ApiParam("文库或文档id") @PathVariable String id) {
        //查询所有者信息
        DcLibrary dcLibrary = dcLibraryService.getById(id);
        List<DcAuthConfig> list = dcAuthConfigService.list(new LambdaQueryWrapper<DcAuthConfig>()
                .eq(DcAuthConfig::getDcId, id)
                //排除所有者信息
                .ne(dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge), DcAuthConfig::getUserId, dcLibrary.getPossessor()));
        if (list.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        //分组
        List<AddAuthConfigDto> configDto = list.stream().collect(Collectors.groupingBy(DcAuthConfig::getGroupKey, Collectors.toList()))
                .entrySet()
                .stream()
                .map(e -> {
                    List<AddAuthConfigDto.UserInfo> userInfos = e.getValue().stream()
                            .map(v -> new AddAuthConfigDto.UserInfo()
                                    .setUserId(v.getUserId())
                                    .setName(v.getName())
                                    .setHeadImg(v.getHeadImg())
                                    .setDataAuthType(v.getDataAuthType()))
                            .collect(Collectors.toList());
                    List<IdentifyingAuthDto> authSign = e.getValue().get(0).getAuthSign();
                    authSign.stream().peek(v -> v.setName(SpringContextUtil.msg(v.getIdentifyingName()))).collect(Collectors.toList());
                    return new AddAuthConfigDto()
                            .setUserInfoList(userInfos)
                            .setIdcIdentifying(authSign);
                }).collect(Collectors.toList());
        return R.ok(configDto);
    }
}
