package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.service.FunctionBusinessService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——表达式
 */
@Service
@AllArgsConstructor
public class FunctionBusinessAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<FunctionBusinessPo> {
    private final FunctionBusinessService functionBusinessService;

    @Override
    public List<FunctionBusinessPo> list(String jvsAppId, List<String> ids) {
        List<FunctionBusinessPo> list = functionBusinessService.list(Wrappers.query(new FunctionBusinessPo().setJvsAppId(jvsAppId)));
        List<String> collect = list.stream()
                .peek(this::clearDefaultData)
                .map(FunctionBusinessPo::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return list;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(functionBusinessService, existsIds, targetVersionTemplateBo.getFunctionBusinessPos(), FunctionBusinessPo::getId);
        // 新增或修改
        List<FunctionBusinessPo> functionBusinessPos = templateBo.getFunctionBusinessPos();
        if (ObjectNull.isNull(functionBusinessPos)) {
            return;
        }
        Map<Boolean, List<FunctionBusinessPo>> map = functionBusinessPos.stream()
                .peek(e -> {
                    clearDefaultData(e);
                    // 设置版本号
                    setAppVersion(e, FunctionBusinessPo::setAppVersion, targetAppVersion);
                })
                .collect(Collectors.groupingBy(p -> existsIds.contains(p.getId())));
        map.forEach((key, value) -> {
            if (key) {
                functionBusinessService.updateBatchById(value);
            } else {
                functionBusinessService.saveBatch(value);
            }
        });
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        functionBusinessService.update(Wrappers.<FunctionBusinessPo>lambdaUpdate().eq(FunctionBusinessPo::getJvsAppId, appId).set(FunctionBusinessPo::getAppVersion, appVersion));
    }
}
