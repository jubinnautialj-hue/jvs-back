//package cn.bctools.rule.data.es;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.HttpHost;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Map;
//
//
///**
// * @author guojing
// */
//@Slf4j
//@Service
//@Order(20)
//@Rule(
//        value = "ES",
//        group = RuleGroup.数据插件,
//        test = true,
//        testShowEnum = TestShowEnum.JSON,
//        returnType = ClassType.数组,
//        order = 21,
////        iconUrl = "rule-mongodbyunshujukuMongoDB",
//        explain = "是非关系数据库当中功能最丰富，最像关系数据库的。它支持的数据结构非常松散，是类似json的bson格式，因此可以存储比较复杂的数据类型。"
//
//)
//@AllArgsConstructor
//public class EsServiceImpl implements BaseCustomFunctionInterface<EsFunctionDto> {
//
////    SimpleElasticsearchRepository simpleElasticsearchRepository;
//
//    @Override
//    public Object execute(EsFunctionDto esFunctionDto, Map<String, Object> params) {
//        new SimpleElasticsearchRepository();
//        simpleElasticsearchRepository.search();
//        return null;
//    }
//
//    public static void main(String[] args) {
////        List<String> list = new ArrayList<>();
////        list.add("10.0.0.38:9200");
////        RestClientBuilder builder = RestClient.builder(new HttpHost("10.0.0.38", 9200));
////        RestHighLevelClient client = new RestHighLevelClient(builder);
////        AsyncSearchClient asyncSearchClient = client.asyncSearch();
////        log.info(asyncSearchClient);
//
////
////        HttpHost[] hosts = list.stream().map(e -> createHttpHost(e)).toArray(HttpHost[]::new);
////        RestClientBuilder builder = RestClient.builder(hosts);
////        builder.build().
////        builder.setHttpClientConfigCallback((httpClientBuilder) -> {
////            builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(httpClientBuilder));
////            return httpClientBuilder;
////        });
////        builder.setRequestConfigCallback((requestConfigBuilder) -> {
////            builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(requestConfigBuilder));
////            return requestConfigBuilder;
////        });
////        builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
////        return builder;
//    }
//
//    private static HttpHost createHttpHost(String uri) {
//        try {
//            return createHttpHost(URI.create(uri));
//        } catch (IllegalArgumentException ex) {
//            return HttpHost.create(uri);
//        }
//    }
//
//    private static HttpHost createHttpHost(URI uri) {
//        if (!StringUtils.hasLength(uri.getUserInfo())) {
//            return HttpHost.create(uri.toString());
//        }
//        try {
//            return HttpHost.create(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(),
//                    uri.getQuery(), uri.getFragment()).toString());
//        } catch (URISyntaxException ex) {
//            throw new IllegalStateException(ex);
//        }
//    }
//}
