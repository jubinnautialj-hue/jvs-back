package cn.bctools.auth.wx.handler;

import cn.bctools.auth.entity.po.WxKeywordData;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.props.OssProperties;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author admin
 * [description]：登录
 */
@Slf4j
@Component
public class WxMpKeywordHandler implements WxMpMessageHandler {
    @Autowired
    OssProperties ossProperties;
    @Autowired
    SysConfigsService sysConfigService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
        try {
            String tenantId = TenantContextHolder.getTenantId();
            SysConfigWx sysWxMpSettings = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
            if (!sysWxMpSettings.getEnable()) {
                throw new BusinessException("公众号未配置");
            }
            //获取对应的关键字回复内容
            log.info("获取的关键字信息为:{}", JSONUtil.toJsonStr(sysWxMpSettings));
            if (!sysWxMpSettings.getKeywordJson().isEmpty()) {
                List<WxKeywordData> data = sysWxMpSettings.getKeywordJson().parallelStream().map(e -> e.toJavaObject(WxKeywordData.class)).collect(Collectors.toList());
                data = data.parallelStream().map(e -> BeanCopyUtil.copy(e, WxKeywordData.class)).collect(Collectors.toList());
                List<WxKeywordData> collect = data.stream().filter(e -> e.getKey().equals(wxMessage.getContent())).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    WxKeywordData wxKeywordData = collect.get(BigDecimal.ROUND_UP);
                    //获取外链地址
                    FileNameDto picUrl = wxKeywordData.getPicUrl();
                    WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
                    item.setDescription(wxKeywordData.getDescription());
                    item.setTitle(wxKeywordData.getTitle());
                    String endpoint = ossProperties.getEndpoint();
                    item.setPicUrl(endpoint + picUrl.getFileLink());
                    if (StringUtils.isNotBlank(ossProperties.getOutsideEndpoint())) {
                        item.setPicUrl(ossProperties.getOutsideEndpoint() + picUrl.getFileLink());
                    }
                    item.setUrl(wxKeywordData.getUrl());
                    return WxMpXmlOutMessage.NEWS()
                            .addArticle(item)
                            .fromUser(wxMessage.getToUser())
                            .toUser(wxMessage.getFromUser())
                            .build();
                }
            }
            //如果用户的关键字 没有找到就再次把关键字信息发给用户
            return WxMpXmlOutMessage.TEXT()
                    .content(HtmlUtil.cleanHtmlTag(Jsoup.parse(sysWxMpSettings.getKeywordText()).body().toString()))
                    .fromUser(wxMessage.getToUser())
                    .toUser(wxMessage.getFromUser())
                    .build();
        } catch (Exception e) {
            log.info("关键字消息错误", e);
        }
        return null;
    }


}
