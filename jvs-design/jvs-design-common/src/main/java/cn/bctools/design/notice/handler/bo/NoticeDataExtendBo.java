package cn.bctools.design.notice.handler.bo;

import cn.bctools.design.notice.handler.enums.NoticeDataExtendOprateTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author hu
 * 消息数据扩展
 */
@Data
@Accessors(chain = true)
@ApiModel("消息数据扩展")
public class NoticeDataExtendBo {

    /**
     * 扩展类型
     */
    private NoticeDataExtendOprateTypeEnum oprateType;

    /**
     * 扩展参数
     */
    private Object oprateParam;
}
