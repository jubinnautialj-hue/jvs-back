package cn.bctools.rule.tools.paqu;//package cn.bctools.rule.tools.paqu;
//
//import cn.bctools.common.utils.ObjectNull;
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.hutool.http.HtmlUtil;
//import cn.hutool.http.HttpUtil;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
///**
// * @author guojing
// * @describe 解析一个网页内容
// */
//@Slf4j
//@AllArgsConstructor
//@Rule(value = "网页内容解析",
//        group = RuleGroup.工具插件,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 10,
////        iconUrl = "rule-wangye-web_h5",
//        explain = "根据网站地址拉取网页，利用正则提取所需数据模块范围，利用截取标签获取具体数据,网页需要是静态网页不支持动态解析"
//
//)
//public class ReptileServiceImpl implements BaseCustomFunctionInterface<ReptileFunctionDto> {
//
//    @Override
//    public Object execute(ReptileFunctionDto dto, Map<String, Object> params) {
//        if (ObjectNull.isNull(dto.getBody())) {
//            String s = HttpUtil.downloadString(dto.getUrl(), Charset.defaultCharset());
//            dto.setBody(s);
//        }
//        Document parse = Jsoup.parse(dto.getBody());
//        //切分标签
//        String[] split = dto.getTag().replaceAll(" ", "").split(">");
//        return getBody(parse.getAllElements(), 0, split).stream().map(e -> HtmlUtil.cleanHtmlTag(e.trim())).collect(Collectors.toList());
//    }
//
//    static Pattern NUMBER_PATTERN = Pattern.compile("([1-9]\\d*\\.?\\d*)");
//
//    /**
//     * 解析数据
//     * #content_views > p:nth-child(4) > img[src]
//     */
//    private static List<String> getBody(Elements element, int i, String... split) {
//        if (ObjectNull.isNull(element)) {
//            return new ArrayList<>();
//        }
//
//        String s1 = "#";
//        String s2 = ".";
//        String s3 = "_";
//        if (i + 1 == split.length) {
//            //判断是不是id
//            if (split[i].contains(s1)) {
//                //如果有Id则获取 id值
//                List<Element> collect = element.stream().map(e -> e.getElementById(split[i].replaceAll(s1, ""))).filter(Objects::nonNull).collect(Collectors.toList());
//                return collect.stream().map(e -> e.html()).distinct().collect(Collectors.toList());
//            } else if (NUMBER_PATTERN.matcher(split[i]).find()) {
//                Matcher s = NUMBER_PATTERN.matcher(split[i]);
//                s.find();
//                Integer index = Integer.valueOf(s.group());
//                //是否有属性
//                String[] split1 = split[i].split(":");
//                Elements elementsByTag = element.get(index).getElementsByTag(split1[0]);
//                return elementsByTag.stream().map(e -> e.html()).collect(Collectors.toList());
//            } else if (split[i].contains(s2)) {
//                String s = split[i].split("\\.")[0];
//                String classs = split[i].replaceAll("\\.", " ").substring(s.length() + 1, split[i].length());
//                List<Element> collect = element.stream().flatMap(e -> e.getElementsByTag(s).stream().filter(a -> a.className().equals(classs))).collect(Collectors.toList());
//                return collect.stream().map(e -> e.html()).distinct().collect(Collectors.toList());
//            } else if (split[i].contains(s3)) {
//                String[] split1 = split[i].split(s3);
//                List<String> collect = element.stream().flatMap(e -> e.getElementsByTag(split1[0]).stream().map(v -> v.attr(split1[1]))).distinct().collect(Collectors.toList());
//                return collect;
//            }
//        }
//
//
//        //判断是不是id
//        if (split[i].contains(s1)) {
//            //如果有Id则获取 id值
//            List<Element> collect = element.stream().map(e -> e.getElementById(split[i].replaceAll(s1, ""))).filter(Objects::nonNull).collect(Collectors.toList());
//            element = new Elements(collect);
//            return getBody(element, i + 1, split);
//        } else if (NUMBER_PATTERN.matcher(split[i]).find()) {
//            Matcher s = NUMBER_PATTERN.matcher(split[i]);
//            s.find();
//            Integer index = Integer.valueOf(s.group());
//            //是否有属性
//            String[] split1 = split[i].split(":");
//            Elements elementsByTag = element.get(index).getElementsByTag(split1[0]);
//            return getBody(elementsByTag, i + 1, split);
//        } else if (split[i].contains(s2)) {
//            String s = split[i].split("\\.")[0];
//            String classs = split[i].replaceAll("\\.", " ").substring(s.length() + 1, split[i].length());
//            List<Element> collect = element.stream().flatMap(e -> e.getElementsByTag(s).stream().filter(a -> a.className().equals(classs))).collect(Collectors.toList());
//            return getBody(new Elements(collect), i + 1, split);
//        } else if (split[i].contains(s3)) {
//            String[] split1 = split[i].split(s3);
//            List<String> collect = element.stream().flatMap(e -> e.getElementsByTag(split1[0]).stream().map(v -> v.attr(split1[1]))).distinct().collect(Collectors.toList());
//            return collect;
//        } else {
//            if (i + 1 == split.length) {
//                List<String> collect = element.stream().map(e -> e.getElementsByTag(split[i]).html()).distinct().collect(Collectors.toList());
//                return collect;
//            } else {
//                List<Element> collect = element.stream().flatMap(e -> e.getElementsByTag(split[i]).stream()).collect(Collectors.toList());
//                return getBody(new Elements(collect), i + 1, split);
//            }
//        }
//    }
//}
