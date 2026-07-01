package cn.bctools.design.notice.dto;

import cn.bctools.design.notice.entity.DataNoticePo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 获取模型所有通知配置响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("获取模型所有通知配置响应")
public class QueryDataNoticeRespDto extends DataNoticePo {

    @ApiModelProperty("标题-无html标签")
    private String title;
    @ApiModelProperty("内容-无html标签")
    private String content;
}
