package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.service.DataFieldService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.util.CurrentAppUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——数据字段
 */
@Service
@AllArgsConstructor
public class DataFieldAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DataFieldPo> {
    private final DataFieldService dataFieldService;

    @Override
    public List<DataFieldPo> list(List<String> modelIds, List<String> ids) {
        List<DataFieldPo> dataFieldPoList = dataFieldService.list(new LambdaQueryWrapper<DataFieldPo>().in(DataFieldPo::getModelId, modelIds));
        List<String> collect = dataFieldPoList.stream()
                .peek(this::clearDefaultData)
                .map(DataFieldPo::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return dataFieldPoList;
    }

    @Override
    public List<DataFieldPo> list(String jvsAppId, List<String> ids) {
        List<DataFieldPo> dataFieldPoList = dataFieldService.list(new LambdaQueryWrapper<DataFieldPo>().eq(DataFieldPo::getJvsAppId, jvsAppId));
        List<String> collect = dataFieldPoList.stream().map(DataFieldPo::getId).collect(Collectors.toList());
        ids.addAll(collect);
        return dataFieldPoList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除字段
        dataFieldService.remove(Wrappers.<DataFieldPo>lambdaQuery()
                .eq(DataFieldPo::getJvsAppId, jvsApp.getId())
                .eq(DataFieldPo::getAppVersion, jvsApp.getUseVersion()));
        // 新增或修改
        List<DataFieldPo> dataFieldPos = templateBo.getDataFieldPos();
        if (ObjectNull.isNull(dataFieldPos)) {
            return;
        }
        JvsApp threadLocalApp = CurrentAppUtils.getApp();
        CurrentAppUtils.clear();
        Map<String, DataFieldPo> appDataFieldMap = dataFieldService.list(Wrappers.<DataFieldPo>lambdaQuery()
                        .eq(DataFieldPo::getJvsAppId, jvsApp.getId())
                        .select(DataFieldPo::getId, DataFieldPo::getAppVersion))
                .stream().collect(Collectors.toMap(DataFieldPo::getId, Function.identity()));
        CurrentAppUtils.setApp(threadLocalApp);
        Map<Boolean, List<DataFieldPo>> map = dataFieldPos.stream()
                .peek(field -> {
                    // 清空默认数据
                    clearDefaultData(field);
                    // 设置版本号
                    setAppVersion(field, DataFieldPo::setAppVersion, targetAppVersion);
                    // 旧模板可能存在应用id为空的情况，需要赋值
                    if (ObjectNull.isNull(field.getJvsAppId())) {
                        field.setJvsAppId(jvsApp.getId());
                    }
                })
                .collect(Collectors.groupingBy(p ->
                        appDataFieldMap.containsKey(p.getId())
                ));
        map.forEach((key, value) -> {
            if (key) {
                dataFieldService.updateBatchById(value);
            } else {
                dataFieldService.saveBatch(value);
            }
        });
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        dataFieldService.update(Wrappers.<DataFieldPo>lambdaUpdate().eq(DataFieldPo::getJvsAppId, appId).set(DataFieldPo::getAppVersion, appVersion));
    }
}
