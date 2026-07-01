package cn.bctools.rule.idcard;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "身份证识别",
        group = RuleGroup.识别插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-daVrenzheng",
        explain = "返回效果:<br/>//正面返回结果\n" +
                "{\n" +
                "        \"side\": \"front\",\n" +
                "        \"front_side\": {\n" +
                "            \"name\": \"李燕强\",\n" +
                "            \"sex\": \"男\",\n" +
                "            \"nation\": \"汉\",\n" +
                "            \"date_of_birth\": \"1982-05-02\",\n" +
                "            \"address\": \"南京市雨花台区窑岗村30号2幢二单元1503室\",\n" +
                "            \"card_no\": \"142323198205026352\"\n" +
                "        }" +
                "\n" +
                "//背面返回结果\n" +
                "{\n" +
                "        \"side\": \"back\",\n" +
                "        \"front_side\": null,\n" +
                "        \"back_side\": {\n" +
                "            \"issue\": \"南京市公安局雨花台分局\",\n" +
                "            \"start_date\": \"201-12-14\",\n" +
                "            \"end_date\": \"2032-12-14\"\n" +
                "        }"
)
public class AliIdCardServiceImpl implements BaseCustomFunctionInterface<AliIdCardDto> {

    static final String URL = "http://cardiddecode.market.alicloudapi.com/api/decode_cardid_aliyun";
    static final Integer CODE = 200;

    @Override
    public Object execute(AliIdCardDto dto, Map<String, Object> params) {
        HashMap<Object, Object> querys = new HashMap<>(1);
        querys.put("imgbase64", dto.getIdCard());
        String body = HttpUtil.createPost(URL)
                .header("Authorization", "APPCODE " + dto.getAppcode())
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(JSONObject.toJSONString(querys))
                .execute().body();
        //判断是否正常
        AliIdCardReturnDto idCardReturnDtos = JSONObject.parseObject(body, AliIdCardReturnDto.class);
        log.info("返回结果为:{}", JSONObject.toJSONString(idCardReturnDtos));
        if (CODE.equals(idCardReturnDtos.getCode())) {
            return idCardReturnDtos.getData();
        }
        throw new BusinessException("身份证识别错误");
    }

}
