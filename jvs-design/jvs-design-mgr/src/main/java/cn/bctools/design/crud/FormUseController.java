package cn.bctools.design.crud;

import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.function.component.ExpressionComponent;
import cn.bctools.function.entity.dto.ExecDto;
import cn.bctools.function.handler.IJvsFunction;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "表单设计使用")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/app/use/{appId}/form")
public class FormUseController {

    FormService formService;
    ExpressionComponent expressionComponent;

    @PostMapping("/code/data/{designId}/{useCase}")
    @ApiOperation(value = "获取二维码标签数据", notes = "前端根据标签数据生成二维码图片")
    public R<JSONObject> getQrCodeData(@ApiParam("表单设计ID") @PathVariable("designId") String designId,
                                       @ApiParam("表达式使用场景") @PathVariable("useCase") String useCase,
                                       @ApiParam("是否对公式进行扩展化处理") @RequestHeader(value = "init", required = false, defaultValue = "false") Boolean init,
                                       @RequestBody(required = false) ExecDto body) {
        // 准备调用公式的数据
        FormPo formPo = Optional.ofNullable(formService.getById(designId)).orElseGet(FormPo::new);
        Map<String, Object> businessIdPaths = JSONPath.paths(formPo.getTagSetting()).entrySet().stream().filter(e -> e.getKey().contains("businessId")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Object> params = Optional.ofNullable(body.getParams()).orElseGet(HashMap::new);
        businessIdPaths.forEach((key, value) -> params.put(String.valueOf(value), ""));
        body.setParams(params);
        // 调用公式
        SystemThreadLocal.set("designSkip", init);
        SystemThreadLocal.set(IJvsFunction.KEY_DESIGN_ID, designId);
        SystemThreadLocal.set("index", body.getIndex());
        expressionComponent.getExpression(designId, useCase, body);
        // 拼接返回数据
        businessIdPaths.forEach((key, value) -> {
            JSONPath.set(formPo.getTagSetting(), key, body.getParams().get(value));
        });
        return R.ok(formPo.getTagSetting());
    }
}
