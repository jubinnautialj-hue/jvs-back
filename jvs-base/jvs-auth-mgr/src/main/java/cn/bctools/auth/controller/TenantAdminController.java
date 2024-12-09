package cn.bctools.auth.controller;

import cn.bctools.auth.entity.LoginLog;
import cn.bctools.auth.entity.User;
import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.auth.service.*;
import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.entity.TenantSpace;
import cn.bctools.common.entity.dto.TenantsDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysApplyConfig;
import cn.bctools.common.enums.SysConfigBase;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.gateway.entity.TenantPo;
import cn.bctools.gateway.entity.TypeEnum;
import cn.bctools.log.annotation.Log;
import cn.bctools.message.push.api.AliSmsApi;
import cn.bctools.message.push.api.WechatOfficialAccountApi;
import cn.bctools.message.push.dto.vo.AliSmsTemplateVo;
import cn.bctools.message.push.dto.vo.WxMpTemplateVo;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "租户管理员")
@RestController
@AllArgsConstructor
@RequestMapping("/tenant/admin/base")
public class TenantAdminController {
    JvsSystemConfig jvsSystemConfig;

    UserTenantService userTenantService;
    UserService userService;
    DeptService deptService;
    UserGroupService userGroupService;
    LoginLogService loginLogService;
    OssTemplate ossTemplate;
    RedisUtils redisUtils;
    WechatOfficialAccountApi wechatOfficialAccountApi;
    AliSmsApi aliSmsApi;
    SysConfigsService sysConfigService;
    TenantService tenantService;
    RoleService roleService;

    @SneakyThrows
    @ApiOperation(value = "基础信息")
    @GetMapping
    public R base() {
        String today = DateUtil.today();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = sdf.parse(today);
        //少1天
        Date st = new Date(parse.getTime() - 86400000);
        long count = loginLogService.list(new LambdaQueryWrapper<LoginLog>().select(LoginLog::getUserId).between(LoginLog::getOperateTime, st, parse).groupBy(LoginLog::getUserId)).size();

        //前7天
        Date end = new Date(parse.getTime() - 86400000 * 7);

        //查询出所有的数据,根据类型返回不同的数据结构
        List<LoginLog> listlog = loginLogService.list(new LambdaQueryWrapper<LoginLog>()
                .select(LoginLog::getClientName, LoginLog::getOperateTime, LoginLog::getClientId)
                .between(LoginLog::getOperateTime, end, new Date()));
        List<ConfigsTypeEnum> data = Arrays.stream(ConfigsTypeEnum.values()).filter(e -> ObjectNull.isNotNull(e.clientId)).collect(Collectors.toList());

        List<Dict> list = new ArrayList<>();
        data.stream().forEach(client -> {
            Map<String, Long> collect = listlog.stream()
                    .filter(e -> e.getClientId().equals(client.clientId))
                    .map(e -> {
                        LocalDate localDate = e.getOperateTime().toLocalDate();
                        Map<String, Object> map = BeanToMapUtils.beanToMap(e);
                        map.put(Get.name(LoginLog::getOperateTime), localDate);
                        return map;
                    }).collect(Collectors.groupingBy(e -> e.get(Get.name(LoginLog::getOperateTime)).toString(), Collectors.counting()));
            for (String date : collect.keySet()) {
                list.add(Dict.create().set("client", client.msg).set("date", date).set("count", collect.get(date)));
            }
        });
        list.sort(Comparator.comparing(e -> {
            Object date = e.get("date");
            return DateUtil.parse(date.toString(), "yyyy-MM-dd");
        }));
        TenantsDto tenant = UserCurrentUtils.getCurrentUser().getTenant();
        TenantPo byId = tenantService.getById(tenant.getId());
        tenant.setShortName(byId.getName());
        Dict set = Dict.create().set("tenant", tenant)
                .set("users", userTenantService.count())
                .set("depts", deptService.count())
                .set("groups", userGroupService.count())
                .set("tenantAdmin", userGroupService.count())
                .set("trend", list)
                .set("yesterdays", count);
        return R.ok(set);
    }

    @ApiOperation(value = "公众号消息模板配置", notes = "获取当前租户的公众号消息模板配置信息接口")
    @GetMapping("/WECHAT_MP/message")
    public R weChatMpMessage(@RequestParam(value = "new", defaultValue = "false") Boolean niu) {
        List<WxMpTemplateVo> allPrivateTemplate = wechatOfficialAccountApi.getAllPrivateTemplate(niu);
        return R.ok(allPrivateTemplate);
    }

    @ApiOperation(value = "短信消息模板配置", notes = "获取当前租户的公众号消息模板配置信息接口")
    @GetMapping("/SMS/login/messageCode")
    public R setLoginMessageCode(@RequestParam("templateCode") String templateCode) {
        String key = "message:push:SMS" + TenantContextHolder.getTenantId();
        //设置
        if (redisUtils.hasKey(key)) {
            SysConfigSms sysConfigSms = (SysConfigSms) sysConfigService.getConfig(ConfigsTypeEnum.SMS_CONFIGURATION);
            sysConfigSms.setTemplateCode(templateCode);
            SysConfigs one = sysConfigService.getOne(Wrappers.query(new SysConfigs().setType(ConfigsTypeEnum.SMS_CONFIGURATION)));
            one.setContent(JSONObject.toJSONString(sysConfigSms));
            sysConfigService.saveOrUpdate(one);
        }
        return R.ok();
    }

    @ApiOperation(value = "短信消息模板配置", notes = "获取当前租户的公众号消息模板配置信息接口")
    @GetMapping("/SMS/message")
    public R smsMessage(@RequestParam(value = "new", defaultValue = "false") Boolean niu, @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                        @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            String key = "message:push:SMS" + TenantContextHolder.getTenantId();
            //如果是不是最新的，则直接返回缓存数据
            List<AliSmsTemplateVo> allPrivateTemplate = new ArrayList<>();
            if (redisUtils.hasKey(key) && !niu) {
                allPrivateTemplate = (List<AliSmsTemplateVo>) redisUtils.get(key);
            } else {
                allPrivateTemplate = aliSmsApi.getAllPrivateTemplate(pageIndex, pageSize);
            }
            SysConfigSms sysConfigSms = (SysConfigSms) sysConfigService.getConfig(ConfigsTypeEnum.SMS_CONFIGURATION);
            if (sysConfigSms.getEnable()) {
                if (ObjectNull.isNotNull(sysConfigSms.getTemplateCode())) {
                    allPrivateTemplate.forEach(e -> {
                        if (e.getTemplateCode().equals(sysConfigSms.getTemplateCode())) {
                            e.setLoginCode(true);
                        } else {
                            e.setLoginCode(false);
                        }
                    });
                }
            }
            //获取缓存配置
            redisUtils.set(key, allPrivateTemplate);
            return R.ok(allPrivateTemplate);
        } catch (Exception e) {
            log.error("消息服务异常", e);
        }
        return R.ok();
    }

    @SneakyThrows
    @GetMapping("/space")
    @ApiOperation(value = "当前租户空间")
    public R space() {
        TenantSpace o = (TenantSpace) redisUtils.get(SysConstant.SYSTEM_STORAGE_INFO + UserCurrentUtils.getCurrentUser().getTenantId());
        if (ObjectNull.isNotNull(o)) {
            Map<String, Object> map = BeanToMapUtils.beanToMap(o);
            map.put(Get.name(TenantSpace::getDataSumSize), DataSizeUtil.format(o.getDataSumSize()) + "/(不限制)");
            map.put(Get.name(TenantSpace::getFileSumSize), DataSizeUtil.format(o.getFileSumSize()) + "/(不限制)");
            return R.ok(map);
        } else {
            return R.ok();
        }
    }

    @GetMapping("/check")
    @ApiOperation(value = "检查配置是否配置了如果有配置则可以使用，未配置，则不能使用")
    public R<List<SysConfigTypeEnum>> check() {
        //如果是超级管理员，需要校验，如果不是，只能是自己修改自己的
        List<SysConfigTypeEnum> collect = new ArrayList<>();
        SysConfigBase config = sysConfigService.getConfig(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            collect.add(SysConfigTypeEnum.DING_H5);
        }
        config = sysConfigService.getConfig(ConfigsTypeEnum.SMS_CONFIGURATION);
        if (config.getEnable()) {
            collect.add(SysConfigTypeEnum.SMS);
        }
        config = sysConfigService.getConfig(ConfigsTypeEnum.MAIL_CONFIGURATION);
        if (config.getEnable()) {
            collect.add(SysConfigTypeEnum.EMAIL);
        }
        config = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
        if (config.getEnable()) {
            collect.add(SysConfigTypeEnum.WECHAT_MP);
            collect.add(SysConfigTypeEnum.WECHAT_MP_MESSAGE);
        }
        config = sysConfigService.getConfig(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION);
        if (config.getEnable()) {
            collect.add(SysConfigTypeEnum.WX_ENTERPRISE);
        }
        return R.ok(collect);
    }


    @Log
    @ApiOperation(value = "注销租户", notes = "删除组织，将导致所有信息无法访问")
    @DeleteMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> delete() {
        String id;
        if (UserCurrentUtils.getCurrentUser().getAdminFlag()) {
            id = UserCurrentUtils.getCurrentUser().getTenantId();
        } else {
            return R.failed("不允许解散平台租户");
        }
        String userId = UserCurrentUtils.getUserId();

        TenantPo byId = tenantService.getById(id);
        //平台帐号
        if (ObjectNull.isNull(byId.getParentId())) {
            throw new BusinessException("不允许解散平台租户");
        }
        // 只能由管理员删除
        boolean removed = tenantService.remove(Wrappers.<TenantPo>lambdaQuery()
                .eq(TenantPo::getId, id)
                .eq(TenantPo::getAdminUserId, userId));
        if (!removed) {
            return R.failed("不允许解散平台租户");
        }
        //删除组织下所有的用户
        //删除所有的用户
        Set<String> list = userTenantService.list(new LambdaQueryWrapper<UserTenant>().select(UserTenant::getUserId)).stream().map(UserTenant::getUserId).collect(Collectors.toSet());
        userTenantService.removeByIds(list);
        //清除租户
        TenantContextHolder.clear();
        //还有的
        Set<String> userIds = userTenantService.list(new LambdaQueryWrapper<UserTenant>().select(UserTenant::getUserId).in(UserTenant::getUserId, list).groupBy(UserTenant::getUserId)).stream().map(e -> e.getUserId()).collect(Collectors.toSet());
        //删除帐号
        list.removeAll(userIds);
        if (ObjectNull.isNotNull(list)) {
            //删除帐号
            userService.removeByIds(list);
        }
        return R.ok();
    }

    @Log
    @ApiOperation(value = "获取租户", notes = "每一个用户都可以创建自己的租户,以自己为管理员, 配置域名")
    @GetMapping("/tenant")
    public R tenant() {
        TenantPo one = tenantService.getOne(new LambdaQueryWrapper<TenantPo>().eq(TenantPo::getId, UserCurrentUtils.getCurrentUser().getTenantId()));
        return R.ok(one);
    }

    @Log
    @ApiOperation(value = "修改租户", notes = "每一个用户都可以创建自己的租户,以自己为管理员, 配置域名")
    @PutMapping("/tenant")
    public R putTenant(@Valid @RequestBody TenantPo tenantPo) {
        if (UserCurrentUtils.getCurrentUser().getAdminFlag()) {
            if (ObjectNull.isNotNull(tenantPo.getAdminUserId())) {
                User user = userService.getById(tenantPo.getAdminUserId());
                tenantPo.setAdminUserName(user.getRealName());
                tenantPo.setAdminUserAccount(user.getAccountName());
                tenantPo.setAdminUserImg(user.getHeadImg());
                tenantPo.setId(UserCurrentUtils.getCurrentUser().getTenantId());
            } else {
                tenantPo.setName(tenantPo.getShortName());
            }
            tenantService.updateById(tenantPo);
            return R.ok().setMsg("修改成功");
        } else {
            return R.failed("修改失败");
        }
    }


    @Log
    @ApiOperation(value = "创建租户", notes = "每一个用户都可以创建自己的租户,以自己为管理员, 配置域名")
    @PostMapping("/tenant")
    @Transactional(rollbackFor = Exception.class)
    public R createTenant(@Valid @RequestBody TenantPo tenantPo) {
        if (!jvsSystemConfig.getMultiTenantMode()) {
            return R.failed("未启用多租户");
        }
        //如果用户已经是管理员了,那就不能重复创建
        UserDto currentUser = UserCurrentUtils.getCurrentUser();

        long count = tenantService.count(Wrappers.query(new TenantPo().setAdminUserId(currentUser.getId())));
        if (count > 0) {
            return R.failed("只允许创建一个租户");
        }

        if (StringUtils.isBlank(currentUser.getPhone())) {
            return R.failed("创建组织必须绑定手机号, 请到个人中心完善手机号");
        }
        String name = tenantPo.getName();
        count = tenantService.count(Wrappers.<TenantPo>lambdaQuery().eq(TenantPo::getName, name));
        if (count > 0) {
            return R.failed("组织名称重复");
        }
        //创建的组织默认上一层为1 保证不会存在多级
        tenantPo.setParentId("1");
        tenantPo.setAdminUserId(currentUser.getId());
        tenantService.save(tenantPo);

        Map<ConfigsTypeEnum, SysConfigBase> map = getMap();

        TenantContextHolder.setTenantId(tenantPo.getId());
        long countUser = userTenantService.count(Wrappers.query(new UserTenant().setUserId(currentUser.getId())));
        if (countUser == 0) {
            userTenantService.saveOrUpdate(new UserTenant().setUserId(currentUser.getId()).setRealName(currentUser.getRealName()));
        }
        //创建租户的相关配置信息
        //获取配置信息的终端信息
        map.keySet().forEach(e -> {
            SysApplyConfig sysApplyConfig = (SysApplyConfig) map.get(e);
            String domainName = sysApplyConfig.getDomainName();
            sysConfigService.save(new SysConfigs()
                    .setType(e)
                    .setTenantId(tenantPo.getId())
                    //随机生成一个新的域名
                    .setContent(JSON.toJSONString(new SysApplyConfig().setDomainName(IdGenerator.getIdStr(36) + "." + domainName)))
                    .setClientId(e.clientId)
                    .setConfigLevel(TypeEnum.tenant));
        });
        return R.ok().setMsg("创建成功");
    }

    private Map<ConfigsTypeEnum, SysConfigBase> getMap() {
        Map<ConfigsTypeEnum, SysConfigBase> map = new HashMap<>(8);
        //后台
        map.put(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION, sysConfigService.getConfig(ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION));
        //规则
        map.put(ConfigsTypeEnum.RULE_PERSONALIZATION_CONFIGURATION, sysConfigService.getConfig(ConfigsTypeEnum.RULE_PERSONALIZATION_CONFIGURATION));
        //物联网
        map.put(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_IOT_PLATFORMS, sysConfigService.getConfig(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_IOT_PLATFORMS));
        //文档
        map.put(ConfigsTypeEnum.ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION, sysConfigService.getConfig(ConfigsTypeEnum.ENTERPRISE_DOCUMENT_ENTERPRISE_CONFIGURATION));
        //bi
        map.put(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE, sysConfigService.getConfig(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_DATA_INTELLIGENCE_WAREHOUSE));
        //项目
        map.put(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING, sysConfigService.getConfig(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_ENTERPRISE_PLANNING));
        //会议
        map.put(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING, sysConfigService.getConfig(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_FOR_VIDEO_CONFERENCING));
        //邮件
        map.put(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE, sysConfigService.getConfig(ConfigsTypeEnum.PERSONALIZED_CONFIGURATION_OF_EMAIL_SERVICE));
        return map;
    }

}
