package cn.bctools.auth.controller.platform;


import cn.bctools.auth.constants.WxMpErrorMsgEnum;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.login.auth.ldap.LdapAuthConfig;
import cn.bctools.auth.login.auth.ldap.LdapBase;
import cn.bctools.auth.service.OauthOtherService;
import cn.bctools.auth.service.impl.SysConfigServiceImpl;
import cn.bctools.common.enums.*;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.gateway.entity.SysConfigs;
import cn.bctools.log.annotation.Log;
import cn.bctools.message.push.utils.SingletonUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySmsSignRequest;
import com.aliyun.dysmsapi20170525.models.QuerySmsSignResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.ContainerCriteria;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "独立的其它配置")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/sys/config")
public class SysConfigController {
    private final LdapAuthConfig ldapAuthConfig;
    private final SysConfigServiceImpl sysConfigService;
    private final RedisUtils redisUtils;
    private final OauthOtherService otherService;

    /**
     * 编辑或修改信息
     *
     * @param sysConfigs
     * @return
     */
    @Log
    @PostMapping("/{type}")
    @ApiOperation(value = "保存配置信息")
    public R<SysConfigs> edit(@PathVariable ConfigsTypeEnum type, @RequestBody SysConfigs sysConfigs) {
        //主动设置终端信息
        sysConfigs.setType(type);
        sysConfigs.setClientId(sysConfigs.getType().clientId);
        // TODO 工作流配置暂存到redis，后续会迁移
        if (ConfigsTypeEnum.WORKFLOW_TODO_REMINDER.equals(type) || ConfigsTypeEnum.WORKFLOW_REMINDER.equals(type)) {
            String key = "config:" + TenantContextHolder.getTenantId() + ":" + type.name();
            redisUtils.set(key, sysConfigs.getContent());
        }
        String tenantId = TenantContextHolder.getTenantId();
        check(type, sysConfigs);
        TenantContextHolder.setTenantId(tenantId);
        sysConfigService.saveOrUpdate(sysConfigs);
        return R.ok();
    }

    @Log
    @GetMapping("/{type}")
    @ApiOperation(value = "获取配置信息")
    public R<SysConfigs> edit(@PathVariable ConfigsTypeEnum type) {
        //获取公众号自动配置时,处理是否是需要
        if (type.equals(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION)) {
            SysConfigBase config = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
            //判断是否配置了公众号
            if (!config.getEnable()) {
                //返回为空
                return R.ok();
            }
        }
        return R.ok(sysConfigService.getOne(Wrappers.query(new SysConfigs().setType(type))));
    }

    @Log
    @GetMapping("/config")
    @ApiOperation(value = "获取哪些有配置,用于显示")
    public R<List<ConfigsTypeEnum>> config() {
        List<ConfigsTypeEnum> collect = sysConfigService.list(new LambdaQueryWrapper<SysConfigs>().select(SysConfigs::getType)
                        .in(SysConfigs::getType, Arrays.stream(ConfigsTypeEnum.values()).collect(Collectors.toSet())))
                .stream()
                .map(SysConfigs::getType)
                .collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 检查输入的配置是否正确
     *
     * @param type       检查类型
     * @param sysConfigs 需要检查的参数
     */
    public void check(ConfigsTypeEnum type, SysConfigs sysConfigs) throws BusinessException {
        //判断域名是否冲突
        if (ObjectNull.isNotNull(type.clientId)) {
            SysApplyConfig config = (SysApplyConfig) JSONObject.parseObject(sysConfigs.getContent(), type.cls);
            String domainName = config.getDomainName();
            if (ObjectNull.isNotNull(domainName)) {
                TenantContextHolder.clear();
                boolean b = sysConfigService.list(Wrappers.query()).stream()
                        .filter(e -> ObjectNull.isNotNull(e.getContent()))
                        .filter(e -> ObjectNull.isNotNull(e.getType().clientId))
                        .filter(e -> !e.getId().equals(sysConfigs.getId()))
                        .map(e -> (SysApplyConfig) JSONObject.parseObject(e.getContent(), e.getType().cls))
                        .map(SysApplyConfig::getDomainName)
                        .anyMatch(domainName::equals);
                if (b) {
                    //表示已经存在了
                    throw new BusinessException("域名已被使用,请重新填写地址");
                }
            }
        }
        //判断配置是否正常
        switch (type) {
            case BACKGROUND_PERSONALIZED_CONFIGURATION:
                //判断是否关闭登录页
                SysFrameApplyConfig frameApplyConfig = (SysFrameApplyConfig) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.BACKGROUND_PERSONALIZED_CONFIGURATION.cls);
                if (frameApplyConfig.getSkipLogin()) {
                    //判断是否配置了有三方登录
                    if (otherService.count(Wrappers.query(new OauthOther().setEnableDefault(true))) == 0) {
                        throw new BusinessException("未配置三方授权登录无法关闭登录页");
                    }
                }
                break;
            case ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION:
                try {
                    SysConfigEnterriseWeChat enterriseWeChat = (SysConfigEnterriseWeChat) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION.cls);
                    WxCpServiceImpl wxCpService = SingletonUtil.get(enterriseWeChat.getAppId() + enterriseWeChat.getAppSecret() + enterriseWeChat.getAgentId(), () -> {
                        WxCpDefaultConfigImpl cpConfig = new WxCpDefaultConfigImpl();
                        cpConfig.setCorpId(enterriseWeChat.getAppId());
                        cpConfig.setCorpSecret(enterriseWeChat.getAppSecret());
                        cpConfig.setAgentId(enterriseWeChat.getAgentId());
                        WxCpServiceImpl wxCpService1 = new WxCpServiceImpl();
                        wxCpService1.setWxCpConfigStorage(cpConfig);
                        return wxCpService1;
                    });
                    wxCpService.getAccessToken(true);
                } catch (WxErrorException e) {
                    throw new BusinessException("三方系统异常:" + e.getMessage());
                }
                break;
            case WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION:
                SysConfigWx wxconfig = (SysConfigWx) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION.cls);
                String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
                url = String.format(url, wxconfig.getAppKey(), wxconfig.getAppSecret());
                log.info("调用地址{}", url);
                String s = HttpUtil.get(url);
                log.info("返回数据{}", s);
                JSONObject jsonObject = JSONObject.parseObject(s);
                Integer errcode = jsonObject.getIntValue("errcode", 0);
                if (0 != errcode) {
                    String msgByCode = WxMpErrorMsgEnum.findMsgByCode(errcode);
                    if (ObjectNull.isNotNull(msgByCode)) {
                        throw new BusinessException(msgByCode);
                    } else {
                        throw new BusinessException("三方系统异常:" + jsonObject.toString());
                    }
                }
                break;
            case NAIL_APPLICATION_CONFIGURATION:
                try {
                    SysConfigDing config = (SysConfigDing) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION.cls);
                    String apiUrl = "https://oapi.dingtalk.com/gettoken";
                    DingTalkClient client = new DefaultDingTalkClient(apiUrl);
                    OapiGettokenRequest req = new OapiGettokenRequest();
                    req.setAppkey(config.getAppId());
                    req.setAppsecret(config.getAppSecret());
                    req.setHttpMethod(HttpMethod.GET.name());
                    OapiGettokenResponse rsp = client.execute(req);
                    if (0L != rsp.getErrcode().intValue()) {
                        throw new BusinessException(rsp.getErrmsg());
                    }
                } catch (ApiException e) {
                    log.error("dingtalk配置无效", e);
                    throw new RuntimeException(e);
                }
                break;
            case SMS_CONFIGURATION:
                SysConfigSms config = (SysConfigSms) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.SMS_CONFIGURATION.cls);
                try {
                    Client aliSmsClient = new Client(new Config().setAccessKeyId(config.getAccessKeyId())
                            .setAccessKeySecret(config.getAccessKeySecret())
                            .setSignatureVersion(config.getSignature()));
                    QuerySmsSignResponse resp = aliSmsClient.querySmsSign(new QuerySmsSignRequest().setSignName(config.getSignature()));
                    if (!resp.getBody().getCode().equals("OK")) {
                        throw new BusinessException(resp.getBody().message);
                    }
                } catch (TeaException error) {
                    // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
                    // 错误 message
                    log.error("短信配置错误", error);
                    // 诊断地址
                    throw new BusinessException(com.aliyun.teautil.Common.assertAsString(error.message));
                } catch (Exception error) {
                    log.error("短信配置错误", error);
                    TeaException exception = new TeaException(error.getMessage(), error);
                    // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
                    // 错误 message
                    // 诊断地址
                    throw new BusinessException(com.aliyun.teautil.Common.assertAsString(exception.message));
                }

                break;
            case MAIL_CONFIGURATION:
                SysConfigMail sysConfigMail = (SysConfigMail) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.MAIL_CONFIGURATION.cls);
                MailAccount account = BeanCopyUtil.copy(sysConfigMail, MailAccount.class);
                account.getFrom();
                account.getPass();
                try {
                    account.setSslEnable(true);
                    MailUtil.send(account, sysConfigMail.getFrom(), "【" + UserCurrentUtils.getRealName() + "】" + UserCurrentUtils.getCurrentUser().getTenant().getShortName() + "   System email testing", "Test " +
                            "successful", false);
                } catch (Exception e) {
                    log.error("邮件配置无效", e);
                    throw new BusinessException("邮件配置无效");
                }
                break;
            case LDAP_CONFIGURATION:
                SysConfigLdap sysConfigLdap = (SysConfigLdap) JSONObject.parseObject(sysConfigs.getContent(), ConfigsTypeEnum.LDAP_CONFIGURATION.cls);
                try {
                    LdapTemplate ldapTemplate = ldapAuthConfig.buildLdapTemplate(sysConfigLdap);
                    ContainerCriteria ldapQuery = LdapQueryBuilder.query().where(LdapBase.OBJECT_CLASS).is("*");
                    ldapTemplate.search(ldapQuery, (ContextMapper<String>) ctx -> "");
                } catch (Exception e) {
                    log.error("LDAP配置无效", e);
                    throw new BusinessException("LDAP配置无效");
                }
        }
    }

}
