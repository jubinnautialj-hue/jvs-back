package cn.bctools.design.data.entity;

import lombok.Data;
import lombok.experimental.Accessors;
/**
 * @author wl
 */
@Data
@Accessors(chain = true)
public class DataChangePo {
    /**
     * 用户头像
     */
    String headImg;
    /**
     * 用户名
     */
    String userName;
    /**
     * 时间
     */
    String timestamp;
    /**
     * 修改内容
     */
    String content;

}
