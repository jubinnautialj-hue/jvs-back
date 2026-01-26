package cn.bctools.design.identification;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.utils.DesignUtils;
import cn.bctools.design.data.fields.dto.FieldBasicsHtml;
import cn.bctools.design.data.fields.dto.enums.ButtonTypeEnum;
import cn.bctools.design.data.fields.dto.page.ButtonDesignHtml;
import cn.bctools.design.data.fields.dto.page.PageDesignHtml;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.identification.dto.GetIdentifierMappingReqDto;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.dto.ButtonSettingDto;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Wrapper;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Api(tags = "[标识]使用")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/base/identification")
public class IdentificationBaseController {

    private final IdentificationService service;
    private final DataFieldService fieldService;
    private final CrudPageService crudPageService;

    @ApiOperation(value = "获取标识映射")
    @GetMapping("/crud/{dataModelId}")
    public R crud(@PathVariable("dataModelId") String dataModelId) {
        List<CrudPage> list = crudPageService.list(Wrappers.query(new CrudPage().setDataModelId(dataModelId)));
        if (list.isEmpty()) {
            return R.ok();
        }
        for (int i = 0; i < list.size(); i++) {
            CrudPage e = list.get(i);
            PageDesignHtml pageDesignHtml = DesignUtils.parsePage(e.getViewJson());
            Optional<String> optional = pageDesignHtml.getButtons().stream().filter(a -> a.getType().equals(ButtonTypeEnum.btn_detail))
                    .filter(ButtonSettingDto::getEnable)
                    .map(ButtonDesignHtml::getFormId).findFirst();
            if (optional.isPresent()) {
                return R.ok(optional.get());
            }
        }
        return R.ok();
    }

    @ApiOperation(value = "获取标识映射")
    @PostMapping("/mappings")
    public R<Map<String, String>> getMappings(@RequestBody GetIdentifierMappingReqDto dto) {
        if (ObjectNull.isNull(dto.getIdentifiers())) {
            return R.ok(Collections.emptyMap());
        }
        //获取当前模式下所有的标识
        Map<String, String> mappings = service.getIdentificationModel()
                .stream()
                .filter(e -> ObjectNull.isNotNull(e.getDesignId()))
                .collect(Collectors.toMap(Identification::getIdentifier, Identification::getDesignId, (k1, k2) -> k2));
        return R.ok(mappings);
    }


    @ApiOperation(value = "获取标识的模型字段")
    @PostMapping("/mappings/field/{modelIdentification}")
    public R getField(@RequestBody List<String> field, @PathVariable("modelIdentification") String modelIdentification) {
        List<Identification> identificationModel = service.getIdentificationModel(modelIdentification);
        if (ObjectNull.isNotNull(identificationModel)) {
            Identification identification = identificationModel.get(0);
            String designId = identification.getDesignId();
            List<FieldBasicsHtml> allField = fieldService.getAllField(identification.getJvsAppId(), designId, true, true, e -> !field.contains(e.getProp()));
            //删除 id行
            allField.remove(0);
            allField.sort(Comparator.comparingInt(o -> field.indexOf(o.getProp())));
            return R.ok(allField);
        }
        return R.ok();
    }

}
