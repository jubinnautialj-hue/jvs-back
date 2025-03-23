package cn.bctools.remote.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestTypeEnums {
    params, body, header;
}
