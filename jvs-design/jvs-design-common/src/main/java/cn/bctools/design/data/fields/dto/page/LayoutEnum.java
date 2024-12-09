package cn.bctools.design.data.fields.dto.page;

/**
 * @author guojing
 */

public enum LayoutEnum {
    /**
     * 目录
     */
    leftTree("目录"),
    /**
     * 列表
     */
    table("列表"),
    /**
     * 卡片
     */
    card("卡片");

    String msg;

    LayoutEnum(String msg) {
        this.msg = msg;
    }
}
