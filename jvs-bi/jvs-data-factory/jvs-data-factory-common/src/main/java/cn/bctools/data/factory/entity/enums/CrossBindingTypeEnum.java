package cn.bctools.data.factory.entity.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CrossBindingTypeEnum {
    join, left, right,union
}
