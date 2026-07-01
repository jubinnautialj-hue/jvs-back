package cn.bctools.design.data.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据变更记录
 *
 * @Author: GuoZi
 */
@Slf4j
@Data
@Accessors(chain = true)
public class DataLogPo {
    @ApiModelProperty("日志id")
    private String id;

    @ApiModelProperty("数据id")
    private String dataId;

    @ApiModelProperty("数据版本")
    private String version;

    @TableField(typeHandler = Fastjson2TypeHandler.class)
    @ApiModelProperty("数据内容(Json格式)")
    private Map<String, Object> content;

    @ApiModelProperty("变更记录(Json格式)")
    private List<Object> dataChange;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("修改人id")
    private String updateById;

    @ApiModelProperty("名称")
    private String realName;
    @ApiModelProperty("头像")
    private String headImg;

    @ApiModelProperty("操作记录")
    private String operator;

    @ApiModelProperty("备注")
    private String remark;

}
