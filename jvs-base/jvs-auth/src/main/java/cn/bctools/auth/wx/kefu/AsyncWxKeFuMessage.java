package cn.bctools.auth.wx.kefu;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpKefuServiceImpl;
import me.chanjar.weixin.mp.api.impl.WxMpMaterialServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ：admin
 * @date ：Created in 2022/2/7 15:19
 * [description]：关注时通过客服消息异步推送消息给用户
 */
@EnableAsync
@Component
@Slf4j
public class AsyncWxKeFuMessage {
    /**
     * 微信客服消息 素材key 素材只会保留3天
     */
    private final static String JVS_AUTH_WX_MESSAGE_MATERIAL = "auth:wx:material:%s:%s";
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    SysConfigsService sysConfigService;

    /**
     * 发送文本消息
     *
     * @param userAppId   需要推送的用户id
     * @param wxMpService 微信类
     * @param content     内容
     **/
    public void asyncSetTextMessage(WxMpService wxMpService, String userAppId, String content) {
        try {
            if (StrUtil.isNotEmpty(content)) {
                log.info("异步推送消息，用户appId为:{}", userAppId);
                WxMpKefuServiceImpl wxMpKeFuService = new WxMpKefuServiceImpl(wxMpService);
                wxMpKeFuService.sendKefuMessage(WxMpKefuMessage.TEXT().content(content).toUser(userAppId).build());
            }
        } catch (Exception e) {
            log.info("异步推送消息报错", e);
        }
    }

    /**
     * 发送图片消息
     *
     * @param userAppId   需要推送的用户id
     * @param wxMpService 微信类
     **/
    @Async
    public void asyncSetImageMessage(WxMpService wxMpService, String userAppId) {


        String tenantId = TenantContextHolder.getTenantId();
        log.info("异步推送消息，用户appId为:{},租户:{} ", userAppId, tenantId);

        if (ObjectNull.isNull(tenantId)) {
            tenantId = String.valueOf(1);
        }
        SysConfigWx sysWxMpSettings = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
        if (!sysWxMpSettings.getEnable()) {
            throw new BusinessException("公众号未配置");
        }
        if (ObjectNull.isNull(sysWxMpSettings.getSubscribeUrl())) {
            return;
        }
        List<FileNameDto> subscribeUrl = sysWxMpSettings.getSubscribeUrl().parallelStream().map(e -> e.toJavaObject(FileNameDto.class)).collect(Collectors.toList());
        if (!subscribeUrl.isEmpty()) {
            subscribeUrl.forEach(e -> {
                try {
                    WxMediaUploadResult wxMediaUploadResult = this.mediaUpload(e, wxMpService);
                    WxMpKefuServiceImpl wxMpKeFuService = new WxMpKefuServiceImpl(wxMpService);
                    wxMpKeFuService.sendKefuMessage(WxMpKefuMessage.IMAGE().mediaId(wxMediaUploadResult.getMediaId()).toUser(userAppId).build());
                } catch (Exception ex) {
                    log.info("消息推送失败!", ex);
                }
            });
        }

    }

    /**
     * 推送素材
     *
     * @param fileNameDto 素材
     * @param wxMpService 微信公众号基础对象
     * @return me.chanjar.weixin.common.bean.result.WxMediaUploadResult
     */
    public WxMediaUploadResult mediaUpload(FileNameDto fileNameDto, WxMpService wxMpService) {
        WxMediaUploadResult wxMediaUploadResult;
        String redisKey = String.format(JVS_AUTH_WX_MESSAGE_MATERIAL, fileNameDto.getBucketName(), fileNameDto.getFileName());
        Object value = redisUtils.get(redisKey);
        if (!ObjectUtil.isNotEmpty(value)) {
            String url = fileNameDto.getFileLink();
            InputStream inputStream;
            try {
                inputStream = new URL(url).openStream();
            } catch (Exception e) {
                log.info("获取素材流错误", e);
                throw new BusinessException("获取素材流错误");
            }
            //获取文件后缀
            String splitStr = "\\.";
            String[] split = url.split(splitStr);
            //创建文件
            File file = FileUtil.writeFromStream(inputStream, new File("subscribe." + split[split.length - 1]));
            try {
                WxMpMaterialServiceImpl wxMpMaterialService = new WxMpMaterialServiceImpl(wxMpService);
                wxMediaUploadResult = wxMpMaterialService.mediaUpload(WxConsts.KefuMsgType.IMAGE, file);
                redisUtils.setExpire(JVS_AUTH_WX_MESSAGE_MATERIAL, JSONObject.toJSONString(wxMediaUploadResult), 3, TimeUnit.DAYS);
            } catch (Exception e) {
                log.info("素材上传到微信错误", e);
                throw new BusinessException("素材上传到微信错误");
            } finally {
                FileUtil.del(file);
            }
        } else {
            wxMediaUploadResult = JSONObject.parseObject(value.toString(), WxMediaUploadResult.class);
        }
        return wxMediaUploadResult;
    }
}
