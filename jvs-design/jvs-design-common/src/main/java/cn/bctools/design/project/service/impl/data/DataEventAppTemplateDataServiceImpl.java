package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataEventPo;
import cn.bctools.design.data.service.DataEventService;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——数据事件
 */
@Service
@AllArgsConstructor
public class DataEventAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<DataEventPo> {
    private final DataEventService dataEventService;

    @Override
    public List<DataEventPo> list(String jvsAppId, List<String> ids) {
        List<DataEventPo> dataEventPoList = dataEventService.list(Wrappers.<DataEventPo>lambdaQuery().eq(DataEventPo::getAppId, jvsAppId));
        if (CollectionUtils.isEmpty(dataEventPoList)) {
            return Collections.emptyList();
        }
        // 清空默认数据
        List<String> listIds = dataEventPoList.stream()
                .peek(this::clearDefaultData)
                .map(DataEventPo::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return dataEventPoList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(dataEventService, existsIds, targetVersionTemplateBo.getDataEventPos(), DataEventPo::getId);
        // 新增或修改
        List<DataEventPo> dataEventPos = templateBo.getDataEventPos();
        if (ObjectNull.isNull(dataEventPos)) {
            return;
        }
        // 清空默认数据
        dataEventPos.forEach(e -> {
            clearDefaultData(e);
            // 设置版本号
            setAppVersion(e, DataEventPo::setAppVersion, targetAppVersion);
        });
        saveOrUpdate(dataEventService, existsIds, dataEventPos, DataEventPo::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        dataEventService.update(Wrappers.<DataEventPo>lambdaUpdate().eq(DataEventPo::getAppId, appId).set(DataEventPo::getAppVersion, appVersion));
    }
}
