package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.service.FlowDesignService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——工作流设计
 */
@Service
@AllArgsConstructor
public class FlowDesignAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<FlowDesign> {
    private final FlowDesignService flowDesignService;

    @Override
    public List<FlowDesign> list(String jvsAppId, List<String> ids) {
        List<FlowDesign> flowDesignList = flowDesignService.list(Wrappers.query(new FlowDesign().setJvsAppId(jvsAppId)));
        if (CollectionUtils.isEmpty(flowDesignList)) {
            return Collections.emptyList();
        }
        List<String> listIds = flowDesignList.stream()
                .peek(this::clearDefaultData)
                .map(FlowDesign::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return flowDesignList;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(flowDesignService, existsIds, targetVersionTemplateBo.getFlowDesigns(), FlowDesign::getId);
        // 新增或修改
        List<FlowDesign> flowDesigns = templateBo.getFlowDesigns();
        if (ObjectNull.isNull(flowDesigns)) {
            return;
        }
        // 清空默认数据
        flowDesigns.forEach(e -> {
            clearDefaultData(e);
            // 设置版本号
            setAppVersion(e, FlowDesign::setAppVersion, targetAppVersion);
        });
        saveOrUpdate(flowDesignService, existsIds, flowDesigns, FlowDesign::getId);
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        flowDesignService.update(Wrappers.<FlowDesign>lambdaUpdate().eq(FlowDesign::getJvsAppId, appId).set(FlowDesign::getAppVersion, appVersion));
    }
}
