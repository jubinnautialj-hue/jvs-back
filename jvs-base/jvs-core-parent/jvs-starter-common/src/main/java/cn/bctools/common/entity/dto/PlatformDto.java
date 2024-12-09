package cn.bctools.common.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class PlatformDto {

    @Data
    @Accessors(chain = true)
    public static class Block {
        String name;
        String icon;
        long size;
        long maxSize;
    }

    List<Block> list;

    String name = "jvs 快速开发平台";
    @ApiModelProperty("版本号")
    String version;
    @ApiModelProperty("设备号")
    String devices;
    @ApiModelProperty("设备号是否集群")
    Boolean servicesCluster;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime dateTime;
//    @ApiModelProperty("剩余时间通过中文")
//    String expiration;

}
