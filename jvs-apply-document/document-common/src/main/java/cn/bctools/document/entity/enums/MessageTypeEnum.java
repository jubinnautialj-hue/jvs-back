package cn.bctools.document.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {
    sms("sms", "短信"),
    email("email", "邮件"),
    inside("inside", "站内"),
    wx_template("wx_template", "微信模板");
    @EnumValue
    public final String value;
    public final String desc;
}
