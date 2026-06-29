package org.jim.core.packets;

/**
 * @author ZhuXiaoKang
 * @Description 分页数据
 * @createTime 2021/04/02 14:08
 */
public class PageData {

    /**
     * 分页参数 - 总数
     */
    private Long total;

    /**
     * 分页参数 - 每页数据量
     */
    private Long size;

    /**
     * 分页参数 - 页码
     */
    private Long current;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }
}
