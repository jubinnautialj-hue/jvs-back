package cn.bctools.document.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @Author: admin
 * @Description: 消息推送类型 例如 文档删除 文库编辑
 */
@Getter
@AllArgsConstructor
public enum MessagePushTypeEnum {
    //文库变动信息 文档变动信息
    library_del("library_del", "文库删除", "文库变动信息"),
    library_update("library_update", "文库编辑", "文库变动信息"),
    document_del("document_del", "文档删除", "文档变动信息"),
    document_update("document_update", "文档编辑", "文档变动信息"),
    document_add("document_add", "文档新增", "文档变动信息");

    @EnumValue
    public final String value;
    public final String desc;
    public final String title;
}
