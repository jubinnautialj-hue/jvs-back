package cn.bctools.rule.entity.enums;

import lombok.Getter;

@Getter
public enum RuleExceptionEnum {

    未知错误(-1, "不确定错误信息"),
    不支持调用(-1000, "只存在于低代码里面其它类型的逻辑不能直接通过 api调用的"),
    ip校验不通过(-1001, "ip校验不通过"),
    accessToken校验不通过(-1002, "accessToken校验不通过"),
    请求地址不存在或未配置标识(-1003, "请求地址不存在或未配置标识"),
    入参校验不通过(-1004, "通过 api 调用时，有入参强制校验并且校验不通过的"),
    出参校验不通过(-1005, "通过 api 调用时，有出参强制校验并且校验不通过的"),
    设计错误(-1006, "设计时可能某些情况未考虑完，或测试阶段未测试到"),
    业务错误(-1007, "当业务数据值等于某个值时结合【消息提示】节点进行错误提示的直接返回业务错误"),
    三方平台报错(-1008, "三方平台调用失败或一些网络接口返回的不正确"),
    ;
    private final String msg;
    private int code;

    RuleExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
