package cn.bctools.gateway.controller;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gj
 */
@Slf4j
@Component
public class ResourceController {


    @Value("${iconPath:/icon/}")
    private String iconPath;
    @Value("${mapPath:/zhmap/}")
    private String mapPath;
    @Value("${oss.localPath:/zhmap/}")
    private String localPath;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    NacosConfigManager nacosConfigManager;

    private final static String NACOS_GROUP = "jvs_web_configuration";

    /**
     * 获取icon
     */
    @Bean
    public RouterFunction iconFunction() {
        return RouterFunctions
                .route(RequestPredicates.GET("/icon/all/**").and(RequestPredicates.accept(MediaType.ALL)),
                        request -> {
                            List<String> names = request.queryParams().getOrDefault("names[]", new ArrayList<>());

                            String path = request.path();
                            String header = "text/plain";
                            String suffix = ".css";
                            if (path.endsWith(suffix)) {
                                header = "text/css";
                            }
                            String suffix1 = ".js";
                            if (path.endsWith(suffix1)) {
                                header = "text/javascript";
                            }
                            String[] split = path.split("/");
                            File file = FileUtil.file(iconPath);
                            if (!file.exists()) {
                                return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(JSONObject.toJSONString(R.ok())));
                            }
                            byte[] bytes = "没有数据".getBytes();
                            int i = 5;
                            if (split.length == i) {
                                try {
                                    bytes = FileUtil.readBytes(new File(file.getPath() + "/" + URLDecoder.decode(split[3], Charset.defaultCharset().name()) + "/" + split[4]));
                                } catch (UnsupportedEncodingException e) {
                                    log.error("file error", e);
                                }
                                String eTag = generateETag(bytes);
                                return ServerResponse.status(HttpStatus.OK)
                                        .cacheControl(CacheControl.maxAge(14, TimeUnit.DAYS)).eTag(eTag).lastModified(Instant.now().plus(Duration.ofDays(14)))
                                        .header("Content-Type", header)
                                        .body(BodyInserters.fromValue(bytes));
                            } else {
                                Map<String, File> fileMap = Arrays.stream(file.listFiles())
                                        .collect(Collectors.toMap(File::getName, Function.identity()));
                                List<Dict> collect = new ArrayList<>();
                                if (ObjectNull.isNotNull(names)) {
                                    //根据顺序获取数据
                                    collect = names.stream().filter(e -> fileMap.containsKey(e)).map(e -> getDict(fileMap.get(e))).collect(Collectors.toList());
                                } else {
                                    //自然顺序
                                    collect = fileMap.values().stream().sorted(Comparator.comparing(e -> e.getName())).map(e -> getDict(e)).collect(Collectors.toList());
                                }
                                bytes = new String(JSONObject.toJSONString(R.ok(collect)).getBytes(), Charset.defaultCharset()).getBytes();
                                String eTag = generateETag(bytes);
                                return ServerResponse.status(HttpStatus.OK)
                                        .cacheControl(CacheControl.maxAge(14, TimeUnit.DAYS)).eTag(eTag).lastModified(Instant.now().plus(Duration.ofDays(14)))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromValue(bytes));
                            }

                        });
    }
    public static String generateETag(byte[] content) {
        try {
            // 创建 SHA-256 MessageDigest 实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 计算哈希值
            byte[] hash = digest.digest(content);
            // 将字节数组转换为十六进制字符串
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate ETag", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Dict getDict(File e) {
        Dict dict = new Dict();
        File jsFile = new File(iconPath + e.getName() + "/iconfont.js");
        if (jsFile.exists()) {
            dict.set("description", "/icon/all/" + e.getName() + "/iconfont.js");
        }
        return dict
                .set("value", "/icon/all/" + e.getName() + "/iconfont.css")
                .set("label", e.getName())
                .set("list", FileUtil.readLines(iconPath + e.getName() + "/iconfont.css", Charset.defaultCharset())
                        .stream()
                        .filter(v -> v.startsWith(".icon-") && v.contains(":"))
                        .map(v -> v.substring(1, v.indexOf(":")))
                        .collect(Collectors.toList())
                );
    }


    /**
     * 获取配置文件
     */
    @Bean
    public RouterFunction policiesFunction() {
        return RouterFunctions
                .route(RequestPredicates.path("/get/nacos/config/**").and(RequestPredicates.accept(MediaType.ALL)),
                        request -> {
                            String[] split = request.path().split("/");
                            if (split.length != BigDecimal.ROUND_HALF_DOWN) {
                                return null;
                            }
                            String dataId = split[4];
                            try {
                                String s = nacosConfigManager.getConfigService().getConfig(dataId, NACOS_GROUP, 3000);
                                return ServerResponse.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML)
                                        .body(BodyInserters.fromValue(s.getBytes()));
                            } catch (NacosException e) {
                                return ServerResponse.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML)
                                        .body(BodyInserters.fromValue("".getBytes()));
                            }

                        });
    }


    /**
     * 获取中国地图
     */
    @Bean
    public RouterFunction getZhMap() {
        return RouterFunctions
                .route(RequestPredicates.path("/get/zh/map/**").and(RequestPredicates.accept(MediaType.ALL)),
                        request -> {
                            byte[] bytes = "没有数据".getBytes();
                            String[] split = request.path().split("/");
                            if (split.length > 3) {
                                String filePath = Arrays.asList(split).subList(4, split.length).stream().collect(Collectors.joining("/"));
                                filePath = mapPath + filePath;
                                if (FileUtil.exist(filePath)) {
                                    bytes = FileUtil.readBytes(filePath);
                                }
                            }
                            return ServerResponse.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML)
                                    .body(BodyInserters.fromValue(bytes));
                        });
    }

    /**
     * 使用本地文件存储时，直接获取文件信息
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(value = "oss.name", havingValue = "local")
    public RouterFunction localOss() {
        return RouterFunctions
                .route(RequestPredicates.path("/oss/**").and(RequestPredicates.accept(MediaType.ALL)),
                        request -> {
                            String path = request.path().substring(4);
                            return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM)
                                    .body(BodyInserters.fromValue(FileUtil.readBytes(localPath + path)));
                        });
    }


}
