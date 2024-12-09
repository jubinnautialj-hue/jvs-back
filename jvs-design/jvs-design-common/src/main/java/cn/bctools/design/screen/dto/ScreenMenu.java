package cn.bctools.design.screen.dto;

import cn.bctools.design.screen.entity.ScreenPo;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScreenMenu extends ScreenPo {

    private Integer sort;
    private List<JSONObject> role;
}
