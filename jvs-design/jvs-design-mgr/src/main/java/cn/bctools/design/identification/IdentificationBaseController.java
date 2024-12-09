package cn.bctools.design.identification;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.design.identification.dto.GetIdentifierMappingReqDto;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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


    @ApiOperation(value = "获取标识映射")
    @PostMapping("/mappings")
    public R<Map<String, String>> getMappings(@RequestBody GetIdentifierMappingReqDto dto) {
        if (ObjectNull.isNull(dto.getIdentifiers())) {
            return R.ok(Collections.emptyMap());
        }
        //获取当前模式下所有的标识
        Map<String, String> mappings = service.getIdentificationModel()
                .stream()
                .collect(Collectors.toMap(Identification::getIdentifier, Identification::getDesignId));
        return R.ok(mappings);
    }
}
