package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import cn.bctools.design.workflow.entity.FlowPurview;
import cn.bctools.design.workflow.service.FlowPurviewService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——工作流权限
 */
@Service
@AllArgsConstructor
public class FlowPurviewAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<FlowPurview> {
    private final FlowPurviewService flowPurviewService;

    @Override
    public List<FlowPurview> list(String jvsAppId, List<String> ids) {
        List<FlowPurview> flowPurviewList = flowPurviewService.list(Wrappers.query(new FlowPurview().setJvsAppId(jvsAppId)));
        if (CollectionUtils.isEmpty(flowPurviewList)) {
            return Collections.emptyList();
        }
        List<String> listIds = flowPurviewList.stream().map(FlowPurview::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return flowPurviewList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 离线包上传的版本不处理（否则会出现设计中显示用户与表中保存的用户不一致的情况）
        VersionIterationTypeEnum type = JvsAppTemplateUtils.getContextVersionIterationType();
        if (VersionIterationTypeEnum.UPLOAD_VERSION.equals(type)) {
            return;
        }
        // 删除
        deleteByAppId(flowPurviewService, FlowPurview::getJvsAppId, jvsApp.getId());
        // 新增
        List<FlowPurview> flowPurviews = templateBo.getFlowPurviews();
        if (ObjectNull.isNull(flowPurviews)) {
            return;
        }
        saveBatch(flowPurviewService, flowPurviews);
    }
}
