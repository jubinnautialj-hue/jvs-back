package cn.bctools.design.platform;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.function.Get;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.FieldPublicHtml;
import cn.bctools.design.data.fields.dto.form.FormDesignHtml;
import cn.bctools.design.data.fields.dto.form.html.TableFormItemHtml;
import cn.bctools.design.data.fields.dto.form.item.TabItemHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.design.project.entity.JvsAppLog;
import cn.bctools.design.project.service.JvsAppLogService;
import cn.bctools.log.annotation.Log;
import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Api(tags = "[app]应用操作日志")
@RestController
@AllArgsConstructor
@RequestMapping("/platform/app/jvsLog")
public class JvsLogController {

    FormService formService;
    JvsAppLogService jvsAppLogService;
    Map<String, IDataFieldHandler> fieldHandlerMap;


    @Log(back = false)
    @ApiOperation(value = "分页")
    @GetMapping("/page")
    public R<Page<JvsAppLog>> page(Page<JvsAppLog> page, JvsAppLog jvsAppLog) {
        jvsAppLogService.page(page, new LambdaQueryWrapper<JvsAppLog>()
                .like(ObjectNull.isNotNull(jvsAppLog.getJvsAppName()), JvsAppLog::getJvsAppName, jvsAppLog.getJvsAppName())
                .like(ObjectNull.isNotNull(jvsAppLog.getModelName()), JvsAppLog::getModelName, jvsAppLog.getModelName())
                .select(JvsAppLog.class, e -> !"json".equals(e.getColumn()))
                .orderByDesc(JvsAppLog::getCreateTime));
        return R.ok(page);
    }

    @Log(back = false)
    @ApiOperation(value = "详情")
    @GetMapping("/{id}")
    public R byId(@PathVariable("id") String id) {
        JvsAppLog byId = jvsAppLogService.getById(id);
        FormPo formPo = formService.getById(byId.getDesignId());
        //解析表单结构，并将所有的下拉，单选 ，多选 ，级联，数据类型改为 input 类型
        if (ObjectNull.isNotNull(formPo)) {
            if (ObjectNull.isNotNull(formPo.getViewJson())) {
                FormDesignHtml formDesignHtml = DesignUtils.parseForm(formPo.getViewJson());
                List<Map<String, Object>> forms = formDesignHtml.getFormdata().get(0).getForms();
                List<Map<String, Object>> collect = forms.stream().map(e -> {
                    extracted(e);
                    return e;
                }).collect(Collectors.toList());
                formDesignHtml.getFormdata().get(0).setForms(collect);
                formPo.setViewJson(JSONObject.toJSONString(formDesignHtml));
            }
        }
        Dict set = Dict.create().set("log", byId).set("form", formPo);
        return R.ok(set);
    }

    private void extracted(Map<String, Object> e) {
        FieldPublicHtml baseDto = BeanCopyUtil.copy(e, FieldPublicHtml.class);
        e.remove("optionHttp");
        switch (baseDto.getType()) {
            case select:
            case checkbox:
            case user:
            case department:
            case role:
            case job:
            case cascader:
                //将以上类型转换为 Input
                e.put(Get.name(FieldPublicHtml::getFieldKey), DataFieldType.input.getDesc());
                break;
            case tableForm:
                TableFormItemHtml html = (TableFormItemHtml) fieldHandlerMap.get(baseDto.getType().getDesc()).toHtml(e);
                html.getTableColumn().forEach(a -> extracted(a.getDesignJson()));
                break;
            case tab:
                TabItemHtml tabItemHtml = (TabItemHtml) fieldHandlerMap.get(baseDto.getType().getDesc()).toHtml(e);
                tabItemHtml.getColumn().values().forEach(a -> a.forEach(s -> extracted(s.getDesignJson())));
            default:
        }
    }

}
