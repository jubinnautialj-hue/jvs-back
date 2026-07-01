package cn.bctools.design.workflow.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 数据dto
 */
@Data
@Accessors(chain = true)
public class DataDto {

    /**
     * 数据id
     */
    private String dataId;

    /**
     * 数据版本号
     */
    private String version;

    /**
     * 数据
     */
    private JSONObject data;
}
