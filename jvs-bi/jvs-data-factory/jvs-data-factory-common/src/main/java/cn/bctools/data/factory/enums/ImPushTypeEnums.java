package cn.bctools.data.factory.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息推送的类型
 *
 * @author xiaohui
 * updatevalue表示修改值
 * update 表示重新获取值
 */
@Getter
@AllArgsConstructor
public enum ImPushTypeEnums {
    updateValue, update
}
