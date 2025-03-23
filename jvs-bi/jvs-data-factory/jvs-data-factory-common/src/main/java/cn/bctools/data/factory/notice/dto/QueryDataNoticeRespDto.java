package cn.bctools.data.factory.notice.dto;

import cn.bctools.data.factory.notice.po.DataNoticePo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 获取模型所有通知配置响应
 */
@Data
@Accessors(chain = true)
@ApiModel("获取模型所有通知配置响应")
public class QueryDataNoticeRespDto extends DataNoticePo {

    @ApiModelProperty("标题-无html标签")
    private String title;
    @ApiModelProperty("内容-无html标签")
    private String content;
}
