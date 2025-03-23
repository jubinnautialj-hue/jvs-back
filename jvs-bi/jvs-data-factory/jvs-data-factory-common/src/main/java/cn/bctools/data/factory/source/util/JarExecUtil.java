package cn.bctools.data.factory.source.util;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.source.enums.RequestTypeEnums;
import cn.bctools.data.factory.source.properties.JarExecProperties;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JarExecUtil {

    private final static String JAR_PACKAGE_CLASS = "cn.bctools.api.RequestUtil";
    private final static String JAR_FUNCTION_NAME = "exec";

    /**
     * 执行jar包
     *
     * @param jarFile jar包
     * @param json    执行需要的入参
     */
    @SneakyThrows
    public static String exec(File jarFile, String json) {
        //TODO 这里 是否需要开启一个线程  防止jar包执行时间过长 如果超过时间就直接 停止线程
        if (jarFile.length() == BigDecimal.ROUND_UP) {
            throw new BusinessException("jar包内容为空");
        }
        JarClassLoader jarClassLoader = ClassLoaderUtil.getJarClassLoader(jarFile);
        Class<?> aClass = jarClassLoader.loadClass(JAR_PACKAGE_CLASS);
        Object o = aClass.newInstance();
        Method m = aClass.getMethod(JAR_FUNCTION_NAME, String.class);
        Object invoke = m.invoke(o, json);
        jarClassLoader.close();
        return StrUtil.toString(invoke);
    }

    /**
     * 执行jar包
     *
     * @param apiDataSourceExecDto 执行需要的入参
     */
    @SneakyThrows
    public static String exec(ApiDataSourceExecDto apiDataSourceExecDto) {
        HttpRequest httpRequest;
        String method = apiDataSourceExecDto.getRequestMethod();
        if ("post".equals(method)) {
            httpRequest = HttpUtil.createPost(apiDataSourceExecDto.getUrl());
        } else {
            httpRequest = HttpUtil.createGet(apiDataSourceExecDto.getUrl());
        }
        //分类入参
        if (apiDataSourceExecDto.getInParameter() != null && !apiDataSourceExecDto.getInParameter().isEmpty()) {
            //分类
            Map<RequestTypeEnums, Map<String, Object>> map = apiDataSourceExecDto.getInParameter().stream().collect(Collectors.groupingBy(ApiDataSourceExecDto.Parameter::getInParameterType, Collectors.toMap(ApiDataSourceExecDto.Parameter::getKey, ApiDataSourceExecDto.Parameter::getValue)));
            //body
            if (!map.getOrDefault(RequestTypeEnums.body, new HashMap<>()).isEmpty()) {
                String s = JSONUtil.toJsonStr(map.get(RequestTypeEnums.body));
                httpRequest.body(s);
            }
            //路径参数 例如xxx/test?id=xxx  id就是路径参数
            if (!map.getOrDefault(RequestTypeEnums.params, new HashMap<>()).isEmpty()) {
                Map<String, Object> stringStringMap = map.get(RequestTypeEnums.params);
                httpRequest.form(stringStringMap);
            }
            //header
            if (!map.getOrDefault(RequestTypeEnums.header, new HashMap<>()).isEmpty()) {
                //转为字符串
                Map<String, String> mapHeader = new HashMap<>();
                map.get(RequestTypeEnums.header).forEach((k, v) -> mapHeader.put(k, v.toString()));
                httpRequest.addHeaders(mapHeader);

            }
        }
        //判断是否存在认证json 认证json默认传到header中 名称为key值名称
        if (StrUtil.isNotBlank(apiDataSourceExecDto.getAuthKey())) {
            Map<String, String> mapHeader = new HashMap<>();
            JSONObject jsonObject = JSONObject.parseObject(apiDataSourceExecDto.getAuthKey());
            jsonObject.forEach((e, v) -> mapHeader.put(e, v.toString()));
            httpRequest.addHeaders(mapHeader);
        }
        JarExecProperties properties = SpringContextUtil.getBean(JarExecProperties.class);

        //连接时间5秒 读取数据的超时时间为15秒
        httpRequest.setConnectionTimeout(properties.getConnectionTimeout())
                .setReadTimeout(properties.getReadTimeout());
        HttpResponse authorization = httpRequest.execute();
        if (authorization.isOk()) {
            return authorization.body();
        }
        throw new Exception("请求错误");
    }
}
