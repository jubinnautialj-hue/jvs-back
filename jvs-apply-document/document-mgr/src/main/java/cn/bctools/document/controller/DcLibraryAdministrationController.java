package cn.bctools.document.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.UserGroupDto;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcLibraryAdministration;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.service.DcLibraryAdministrationService;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文库管理
 * <p>
 *
 * @author Auto Generator
 */
@Api(tags = "文库管理")
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/dc/library/administration")
public class DcLibraryAdministrationController {
    private final DcLibraryAdministrationService dcLibraryAdministrationService;
    private final AuthUserServiceApi authUserServiceApi;

    @Log
    @ApiOperation("新增")
    @PostMapping("/save")
    public R<List<DcLibraryAdministration>> save(@RequestBody List<DcLibraryAdministration> dcLibraryAdministration) {
        if (dcLibraryAdministration.isEmpty()) {
            return R.ok();
        }
        //获取类型
        DcLibraryReadEnum libraryType = dcLibraryAdministration.get(0).getLibraryType();
        //判断是否存在重复添加
        List<String> ids = dcLibraryAdministration.stream().map(DcLibraryAdministration::getUserId).collect(Collectors.toList());
        List<String> saveList = dcLibraryAdministrationService.list(new LambdaQueryWrapper<DcLibraryAdministration>().in(DcLibraryAdministration::getUserId, ids)
                        .eq(DcLibraryAdministration::getLibraryType, libraryType))
                .stream()
                .map(DcLibraryAdministration::getUserId)
                .collect(Collectors.toList());
        dcLibraryAdministration = dcLibraryAdministration.stream().filter(e -> !saveList.contains(e.getUserId())).collect(Collectors.toList());
        //防止过滤后数组为空
        if (dcLibraryAdministration.isEmpty()) {
            return R.ok();
        }
        dcLibraryAdministrationService.saveBatch(dcLibraryAdministration);
        return R.ok(dcLibraryAdministration);
    }

    @Log
    @GetMapping("/page")
    @ApiOperation("分页查询文档")
    public R<Page<DcLibraryAdministration>> page(Page<DcLibraryAdministration> page, DcLibraryAdministration dcLibraryAdministration) {
        dcLibraryAdministrationService.page(page, new LambdaQueryWrapper<DcLibraryAdministration>()
                .eq(ObjectUtil.isNotNull(dcLibraryAdministration.getLibraryType()), DcLibraryAdministration::getLibraryType, dcLibraryAdministration.getLibraryType())
                .orderByDesc(DcLibraryAdministration::getCreateTime));
        return R.ok(page);
    }

    @Log
    @GetMapping("/get/count")
    @ApiOperation("获取不同类型的总条数")
    public R<Map<DcLibraryReadEnum, Long>> getCount() {
        Map<DcLibraryReadEnum, Long> map = dcLibraryAdministrationService.list()
                .parallelStream().collect(Collectors.groupingBy(DcLibraryAdministration::getLibraryType, Collectors.counting()));
        return R.ok(map);
    }

    @Log
    @ApiOperation("查询当前用户存在的权限")
    @GetMapping
    public R<List<String>> get() {
        UserDto user = UserCurrentUtils.getCurrentUser();
        log.info("当前用户信息为:{}", JSONObject.toJSONString(user));
        //角色id
        List<String> roles = UserCurrentUtils.init().getRoles();
        //群组id
        List<UserGroupDto> userGroupDtoList = Optional.ofNullable(authUserServiceApi.userGroup(user.getId()).getData()).orElse(new ArrayList<>());
        LambdaQueryWrapper<DcLibraryAdministration> eq = new LambdaQueryWrapper<DcLibraryAdministration>()
                .eq(DcLibraryAdministration::getUserType, DataAuthTypeEnum.user)
                .eq(DcLibraryAdministration::getUserId, user.getId())
                .or(StrUtil.isNotEmpty(user.getJobId()), e -> e.eq(DcLibraryAdministration::getUserType, DataAuthTypeEnum.job).eq(DcLibraryAdministration::getUserId, user.getJobId()))
                .or(!user.getDept().isEmpty(), e -> e.eq(DcLibraryAdministration::getUserType, DataAuthTypeEnum.dept).in(DcLibraryAdministration::getUserId, user.getDept().stream().map(DeptDto::getDeptId).collect(Collectors.toList())))
                .or(!roles.isEmpty(), e -> e.eq(DcLibraryAdministration::getUserType, DataAuthTypeEnum.role).in(DcLibraryAdministration::getUserId, roles))
                .or(!userGroupDtoList.isEmpty(), e -> e.eq(DcLibraryAdministration::getUserType, DataAuthTypeEnum.group)
                        .in(DcLibraryAdministration::getUserId, userGroupDtoList.stream().map(UserGroupDto::getId).collect(Collectors.toList())));
        List<DcLibraryAdministration> list = dcLibraryAdministrationService.list(eq);
        //添加默认值 前端必须要
        list.add(new DcLibraryAdministration().setLibraryType(DcLibraryReadEnum.user));
        List<String> collect = list.stream().map(e -> e.getLibraryType().getValue()).distinct().collect(Collectors.toList());
        return R.ok(collect);
    }

    @Log
    @ApiOperation("删除用户数据")
    @DeleteMapping("/del/{id}")
    public R<Boolean> check(@ApiParam(value = "id", required = true) @PathVariable String id) {
        dcLibraryAdministrationService.removeById(id);
        return R.ok(Boolean.TRUE);
    }


}
