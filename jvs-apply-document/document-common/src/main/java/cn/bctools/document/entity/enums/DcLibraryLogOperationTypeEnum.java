package cn.bctools.document.entity.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DcLibraryLogOperationTypeEnum {
    ADD("新增"),
    UPDATE("修改"),
    MOVE("移动"),
    DELETE("删除"),
    SHARE("分享"),
    SEE("查看"),
    DOWNLOAD("下载");
    @EnumValue
    public final String value;

    public static DcLibraryLogOperationTypeEnum get(String value){
        for (DcLibraryLogOperationTypeEnum s : values()) {
            if (s.getValue().equals(value)) {
                return s;
            }
        }
        return null;
    }
}
