package cn.bctools.data.factory.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FieldSetupReplaceEnum {
    //未选择 替换null 空 自定义
    noChoice,replaceNull,customize;
}
