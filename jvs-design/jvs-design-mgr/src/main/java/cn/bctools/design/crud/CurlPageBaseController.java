package cn.bctools.design.crud;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.R;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.enums.FieldTypeEnum;
import cn.bctools.design.data.fields.dto.enums.QueryTypeEnum;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "列表设计 通用权限")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/base/page")
public class CurlPageBaseController {

    @ApiOperation(value = "涉及到的Label-Value", notes = "辅助接口")
    @GetMapping("/labelValue")
    public R<Map<String, Map<String, Object>>> labelValue() {
        Map<String, Object> labelValue = new HashMap<>(8);
        Map<String, Map<String, Object>> listValue = new HashMap<>(8);
        //按钮类型
        listValue.put("buttonType", Arrays.stream(ButtonTypeEnum.values()).collect(Collectors.toMap(ButtonTypeEnum::name, ButtonTypeEnum::getDescription)));
        //字段类型
        listValue.put("fieldType", Arrays.stream(FieldTypeEnum.values()).collect(Collectors.toMap(FieldTypeEnum::name, FieldTypeEnum::getDescription)));
        listValue.put("fieldTypeMore", Arrays.stream(FieldTypeEnum.values()).collect(Collectors.toMap(FieldTypeEnum::name, BeanCopyUtil::beanToMap)));
        //查询类型
        listValue.put("queryType", Arrays.stream(QueryTypeEnum.values()).collect(Collectors.toMap(QueryTypeEnum::name, QueryTypeEnum::getDescription)));
        //注意 设计时要保证key唯一
        listValue.forEach((k, v) -> labelValue.putAll(v));
        //字典 前端用于匹配
        listValue.put("dict", labelValue);
        //限制不同字段类型的查询条件类型
        listValue.put("querySpec", Arrays.stream(FieldTypeEnum.values()).filter(f -> ObjectUtil.isNotEmpty(f.getSupportQueryType())).collect(Collectors.toMap(FieldTypeEnum::name, v -> Arrays.stream(v.getSupportQueryType()).collect(Collectors.toMap(QueryTypeEnum::name, QueryTypeEnum::getDescription)))));
        //按钮请求关联
        return R.ok(listValue);
    }
}
