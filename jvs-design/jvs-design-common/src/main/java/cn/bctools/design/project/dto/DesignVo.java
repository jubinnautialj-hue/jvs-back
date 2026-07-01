package cn.bctools.design.project.dto;

import cn.bctools.design.data.fields.enums.DesignType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 应用设计数据
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
public class DesignVo implements Comparable<DesignVo> {

    @ApiModelProperty("设计名称")
    private String id;

    @ApiModelProperty("设计名称")
    private String name;

    @ApiModelProperty(value = "设计类型", required = true)
    private DesignType designType;
    private String designTypeName;

    @ApiModelProperty("创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Override
    public int compareTo(DesignVo o) {
        if (createTime == null) {
            return -1;
        }
        if (o.getCreateTime() == null) {
            return 1;
        }
        return createTime.compareTo(o.getCreateTime());
    }

}
