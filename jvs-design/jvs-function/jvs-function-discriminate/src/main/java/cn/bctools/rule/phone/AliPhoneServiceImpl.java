package cn.bctools.rule.phone;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.idcard.AliIdCardReturnDto;
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
@Rule(value = "手机实名",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 11,
//        iconUrl = "rule-dypns",
        explain = "三网合一手机号实名认证，输入姓名、身份证号码、手机号码，验证三要素信息是否一致，返回验证结果。"
)
public class AliPhoneServiceImpl implements BaseCustomFunctionInterface<AliPhoneDto> {

    static final String URL = "http://mobile3elements.shumaidata.com/mobile/verify_real_name";
    static final String CODE = "0";

    @Override
    public Object execute(AliPhoneDto dto, Map<String, Object> params) {
        HashMap<Object, Object> querys = new HashMap<>(1);
        querys.put("name", dto.getName());
        querys.put("idcard", dto.getIdcard());
        querys.put("mobile", dto.getMobile());
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
