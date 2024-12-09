package cn.bctools.rule.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.config.SystemInit;
import cn.bctools.rule.dto.JunZiQianBaseDto;
import cn.bctools.rule.dto.JunZiQianOption;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import com.junziqian.sdk.bean.ResultInfo;
import com.junziqian.sdk.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
public class JunZiQianUtils {

    /**
     * 发起请求
     *
     * @param url        接口地址
     * @param serviceUrl 生产或测试的地址
     * @param appkey     凭证
     * @param appSecret  凭证
     * @param reqBody
     * @return
     */
    public static Object post(String url, String serviceUrl, String appkey, String appSecret, Map<String, Object> reqBody) {
        ResultInfo<Object> objectResultInfo = RequestUtils.init(serviceUrl, appkey, appSecret).doPost(url, reqBody);
        if (objectResultInfo.isSuccess()) {
            return objectResultInfo.getData();
        } else {
            log.error(JSONObject.toJSONString(objectResultInfo));
            throw new BusinessException("三方平台执行异常具体信息错误码,{},{},{}", objectResultInfo.getMsg(), objectResultInfo.getData(), objectResultInfo.getResultCode());
        }
    }

    public static Object post(String path, JunZiQianBaseDto dto) {
        Set<String> fileDto = Arrays.stream(dto.getClass().getFields()).filter(e -> {
                    if (e.getAnnotation(ParameterValue.class).type().equals(InputType.files)) {
                        return true;
                    }
                    if (e.getAnnotation(ParameterValue.class).type().equals(InputType.file)) {
                        return true;
                    }
                    return false;
                })
                .map(Field::getName)
                .collect(Collectors.toSet());
        JunZiQianOption option = JSONObject.parseObject(PasswordUtil.decodedPassword(dto.getOptions(), SpringContextUtil.getKey()), JunZiQianOption.class);
        Map<String, Object> reqBody = BeanCopyUtil.beanToMap(dto);
        if (ObjectNull.isNotNull(fileDto)) {
            OssTemplate bean = SpringContextUtil.getBean(OssTemplate.class);
            //如果不为空，则直接转换
            for (String field : fileDto) {
                Object o = reqBody.get(field);
                if (o instanceof RuleFile) {
                    ByteArrayBody byteArrayBody = getByteArrayBody(bean, (RuleFile) o);
                    reqBody.put(field, byteArrayBody);
                } else if (o instanceof List) {
                    List<ByteArrayBody> collect = ((List<?>) o).stream().map(e -> getByteArrayBody(bean, (RuleFile) e)).collect(Collectors.toList());
                    if (ObjectNull.isNull(collect)) {
                        reqBody.remove(field);
                    } else {
                        reqBody.put(field, collect);
                    }
                }
            }
        }
        reqBody.remove("options");
        return JunZiQianUtils.post(path, option.getServiceUrl(), option.getAppKey(), option.getAppSecret(), reqBody);
    }

    @NotNull
    private static ByteArrayBody getByteArrayBody(OssTemplate bean, RuleFile o) {
        InputStream object = bean.getObject(o.getBucketName(), o.getFileName());
        byte[] bytes = IoUtil.readBytes(object);
        return new ByteArrayBody(bytes, Optional.of(o).map(RuleFile::getName).orElse(o.getOriginalName()));
    }
}
