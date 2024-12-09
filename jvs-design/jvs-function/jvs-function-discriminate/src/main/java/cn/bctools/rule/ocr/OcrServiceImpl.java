package cn.bctools.rule.ocr;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "OCR识别",
        group = RuleGroup.识别插件,
        test = true,
        returnType = ClassType.数组,
        testShowEnum = TestShowEnum.JSON,
        order = 90,
//        iconUrl = "rule-1dataOCR",
        explain = "将图片地址进行ocr识别内容进行返回"
)
public class OcrServiceImpl implements BaseCustomFunctionInterface<OcrDto> {

    @Override
    public Object execute(OcrDto dto, Map<String, Object> params) {
        byte[] bytes = HttpUtil.downloadBytes(dto.getUrl());
        String encode = Base64.encode(bytes);
        String body = HttpUtil.createPost("http://ocr-server:8089/api/tr-run/").form("compress", 1600).form("img", encode).execute().body();
        JSONArray objects = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("raw_out");
        List<Object> collect = objects.stream().map(e -> (JSONArray) (e)).map(e -> e.get(1)).collect(Collectors.toList());
        return collect;
    }
}
