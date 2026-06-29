package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 权限标识枚举
 *
 * @author xiaohui
 */

@Getter
public enum IdentifyingKeyEnum {
    view("view", "查看"),
    library_sort("library_sort", "文档排序"),
    library_update("library_update", "文库编辑"),
    library_transfer("library_transfer", "文库转移项目"),
    document_share("document_share", "文档分享"),
    library_remind_settings("library_remind_settings", "文库消息设置"),
    library_down("library_down", "文库下载"),
    library_delete("library_delete", "文库删除"),
    library_auth_settings("library_auth_settings", "文库权限设置"),
    document_update("document_update", "文档编辑"),
    document_add("document_add", "新建文档"),
    document_remind_settings("document_remind_settings", "文档消息设置"),
    document_move("document_move", "文档移动"),
    document_down("document_down", "文档下载"),
    document_auth_settings("document_auth_settings", "文档权限设置"),
    document_delete("document_delete", "文档删除"),
    document_top("document_top", "文档置顶"),
    document_auth("document_auth", "文档权限设置");
    @EnumValue
    public final String value;
    public final String desc;

    IdentifyingKeyEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

}
