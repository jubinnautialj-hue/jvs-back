package cn.bctools.auth.feign;

import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.auth.entity.EnvironmentVariable;
import cn.bctools.auth.service.EnvironmentVariableService;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 环境变量的feign接口实现类
 *
 * @author xc
 */
@RequestMapping
@RestController
@AllArgsConstructor
public class EnvironmentVariableApiImpl implements EnvironmentVariableApi {
    EnvironmentVariableService environmentVariableService;

    @Override
    public R<EnvironmentVariableDto> getByKey(String key, ModeTypeEnum mode) {
        if (ObjectNull.isNull(mode)) {
            mode = ModeTypeEnum.GA;
        }
        EnvironmentVariable one = environmentVariableService.getOne(Wrappers.query(new EnvironmentVariable().setMode(mode).setLabel(key)));
        return R.ok(BeanCopyUtil.copy(one, EnvironmentVariableDto.class));
    }

    @Override
    public R<List<EnvironmentVariableDto>> getAll(ModeTypeEnum mode) {
        //根据模式获取环境变量的配置信息,此处兼容低代码多模式


        List<EnvironmentVariable> list = environmentVariableService.list(Wrappers.query(new EnvironmentVariable().setMode(mode)));
        if (ObjectNull.isNotNull(list)) {
            List<EnvironmentVariableDto> collect = list.stream()
                    .map(e -> new EnvironmentVariableDto().setLabel(e.getLabel()).setType(e.getType()).setValue(e.getValue().toString()).setRemark(e.getRemark()))
                    .collect(Collectors.toList());
            return R.ok(collect);
        }
        return R.ok(new ArrayList<>());
    }

    @Override
    public R save(EnvironmentVariableDto variableDto) {
        EnvironmentVariable one = environmentVariableService.getOne(Wrappers.query(new EnvironmentVariable().setLabel(variableDto.getLabel())));
        one.setValue(variableDto.getValue());
        one.setType(variableDto.getType());
        environmentVariableService.saveOrUpdate(one);
        return R.ok();
    }

    @Override
    public R cover(List<EnvironmentVariableDto> variableDto) {
        List<String> labels = variableDto.stream().map(EnvironmentVariableDto::getLabel).collect(Collectors.toList());
        List<EnvironmentVariable> list = environmentVariableService.list(Wrappers.lambdaQuery(EnvironmentVariable.class).eq(EnvironmentVariable::getMode,ModeTypeEnum.GA).in(EnvironmentVariable::getLabel, labels));
        Map<String, EnvironmentVariable> collect = list.stream().collect(Collectors.toMap(EnvironmentVariable::getLabel, Function.identity(), (v1, v2) -> v1));

        List<EnvironmentVariable> imports = variableDto.stream().map(e -> {
            EnvironmentVariable environmentVariable;
            if (collect.containsKey(e.getLabel())) {
                environmentVariable = collect.get(e.getLabel());
                environmentVariable.setType(e.getType()).setMode(ModeTypeEnum.GA).setValue(e.getValue());
            } else {
                environmentVariable = new EnvironmentVariable()
                        .setLabel(e.getLabel())
                        .setType(e.getType())
                        .setMode(ModeTypeEnum.GA)
                        .setRemark("导入")
                        .setValue(e.getValue());
            }
            return environmentVariable;
        }).collect(Collectors.toList());
        environmentVariableService.saveOrUpdateBatch(imports);
        return R.ok();
    }
}
