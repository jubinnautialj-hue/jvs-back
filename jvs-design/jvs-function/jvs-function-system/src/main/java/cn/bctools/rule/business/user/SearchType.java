package cn.bctools.rule.business.user;

import lombok.Getter;

@Getter
public enum SearchType {
    Email("邮箱"), Account("帐号"), Phone("手机号");

    private final String msg;

    SearchType(String msg) {
        this.msg = msg;
    }
}
