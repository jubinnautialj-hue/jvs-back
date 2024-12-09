package cn.bctools.rule.bankcard;

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
@Rule(value = "三要素验证",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 10,
//        iconUrl = "rule-yinxingqiasanyaosurenzheng",
        explain = "银行卡三要素验证接口是通过比对姓名、身份证号、银行卡号的一致性，核验银行卡持卡人身份信息的真伪。"
)
public class AliBankCardServiceImpl implements BaseCustomFunctionInterface<AliBankCardDto> {

    static final String URL = "http://yhkr.market.alicloudapi.com/communication/personal/1886";
    static final String CODE = "10000";

    @Override
    public Object execute(AliBankCardDto dto, Map<String, Object> params) {
        HashMap<Object, Object> querys = new HashMap<>(1);
        querys.put("name", dto.getName());
        querys.put("idcard", dto.getIdcard());
        querys.put("acct_no", dto.getAcctNo());
        String body = HttpUtil.createPost(URL)
                .header("Authorization", "APPCODE " + dto.getAppcode())
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
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
