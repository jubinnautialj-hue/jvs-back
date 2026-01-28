package cn.bctools.design.menu.entity.handler;

import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;


/**
 * @author jvs
 */
public class PermissionIdentificationHandler extends AbstractJsonTypeHandler<PermissionIdentificationDto> {


    @Override
    protected PermissionIdentificationDto parse(String json) {
        return JSONObject.parseObject(json, PermissionIdentificationDto.class);
    }

    @Override
    protected String toJson(PermissionIdentificationDto obj) {
        return JSONObject.toJSONString(obj);
    }
}
