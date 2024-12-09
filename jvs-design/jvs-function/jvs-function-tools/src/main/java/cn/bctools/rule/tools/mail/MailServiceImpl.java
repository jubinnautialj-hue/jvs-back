package cn.bctools.rule.tools.mail;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigMail;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.email.EmailUtils;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 邮件发送功能
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "发送邮件", group = RuleGroup.工具插件, test = true, returnType = ClassType.布尔, testShowEnum = TestShowEnum.TEXT, order = 23,
//        iconUrl = "rule-youjian",
        explain = "给指定用户发送邮件。")
public class MailServiceImpl implements BaseCustomFunctionInterface<MailFunctionDto> {

    EmailUtils emailUtils;
    AuthTenantConfigServiceApi configServiceApi;
    AuthUserServiceApi authUserServiceApi;
    OssTemplate ossTemplate;

    @SneakyThrows
    @Override
    public Object execute(MailFunctionDto dto, Map<String, Object> params) {

        //直接获取系统租户配置信息
        R<String> key = configServiceApi.key(ConfigsTypeEnum.MAIL_CONFIGURATION, TenantContextHolder.getTenantId());
        SysConfigMail config = configServiceApi.convertKey(ConfigsTypeEnum.MAIL_CONFIGURATION, key.getData());
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到邮件配置，请核实!");
        }

        List<String> emails = new ArrayList<>();
        for (String userId : dto.getUserIds()) {
            try {
                String email = authUserServiceApi.getById(userId).getData().getEmail();
                if (ObjectNull.isNotNull(email)) {
                    emails.add(email);
                }
            } catch (Exception e) {
                //兼容邮件就是用户信息
                emails.add(userId);
            }
        }
        MailAccount copy = BeanCopyUtil.copy(config, MailAccount.class);
        if (ObjectNull.isNotNull(dto.getText())) {
            dto.getText().keySet().forEach(e -> dto.setContent(dto.getContent().replaceAll("\\$\\{" + e + "}", dto.getText().getOrDefault(e, ""))));
        }
        try {
            List<DataSource> collect = new ArrayList<>();
            if (ObjectNull.isNotNull(dto.getFiles())) {
                //获取附件
                Map<String, Object> files = dto.getFiles();
                for (String name : files.keySet()) {
                    Object o = files.get(name);
                    byte[] decode = new byte[0];
                    if (o instanceof RuleFile || o instanceof Map) {
                        RuleFile file = BeanCopyUtil.copy(o, RuleFile.class);
                        String originalName = ((RuleFile) file).getOriginalName();
                        if (ObjectNull.isNotNull(originalName)) {
                            name = originalName;
                        }
                        String link = ossTemplate.fileLink(file.getFileName(), file.getBucketName());
                        //对文件信息进行转换
                        decode = HttpUtil.downloadBytes(link);
                    } else if (o instanceof String) {
                        if (((String) o).startsWith("http")) {
                            decode = HttpUtil.downloadBytes(o.toString());
                        } else {
                            decode = Base64.decode((CharSequence) o);
                        }
                    }
                    ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(decode, "application/octet-stream");
                    byteArrayDataSource.setName(name);
                    collect.add(byteArrayDataSource);
                }
            }
            boolean b = emailUtils.sendEmailMessage(copy, dto.getTitle(), dto.getContent(), emails, collect);
            if (b) {
                return true;
            }
        } catch (Exception e) {
            log.error("发送失败", e);
            throw new BusinessException("发送失败");
        }
        return true;
    }

}
