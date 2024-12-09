package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.design.workflow.service.FlowDesignVersionService;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.design.workflow.utils.FlowVariableUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——工作流设计版本
 * <p>
 * 开发 -> 测试 （是同环境，用户体系一定相同）
 * 测试 -> 正式 （是同环境，用户体系一定相同）
 * 开发 -> 测试 -> 正式 （用户体系不一定相同。 因为可能测试环境是通过离线包上传的）
 */
@Service
@AllArgsConstructor
public class FlowDesignVersionAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<FlowDesignVersion> {
    private final FlowDesignVersionService flowDesignVersionService;
    private final FlowDesignService flowDesignService;

    @Override
    public List<FlowDesignVersion> list(String jvsAppId, List<String> ids) {
        // 查询未删除的工作流设计id，再根据工作流设计id获取工作流设计版本（只查询“USE”、“DESIGNING”的版本）
        List<String> flowDesignIds = flowDesignService.list(Wrappers.query(new FlowDesign().setJvsAppId(jvsAppId))).stream().map(FlowDesign::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowDesignIds)) {
            return Collections.emptyList();
        }
        List<FlowDesignVersion> flowDesignVersionList = flowDesignVersionService.list(Wrappers.<FlowDesignVersion>lambdaQuery()
                .eq(FlowDesignVersion::getJvsAppId, jvsAppId)
                .in(FlowDesignVersion::getFlowDesignId, flowDesignIds)
                .in(FlowDesignVersion::getVersionStatus, Arrays.asList(FlowDesignVersionStatusEnum.USE, FlowDesignVersionStatusEnum.DESIGNING)));
        if (CollectionUtils.isEmpty(flowDesignVersionList)) {
            return Collections.emptyList();
        }
        List<String> listIds = flowDesignVersionList.stream()
                .peek(this::clearDefaultData)
                .map(FlowDesignVersion::getId).collect(Collectors.toList());
        ids.addAll(listIds);
        return flowDesignVersionList;
    }

    @Override
    public void save(JvsApp jvsApp, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 没有工作流设计 || 没有工作流设计版本  不处理
        List<FlowDesign> flowDesignList = templateBo.getFlowDesigns();
        if (ObjectNull.isNull(flowDesignList) || ObjectNull.isNull(templateBo.getFlowDesignVersions())) {
            return;
        }
        VersionIterationTypeEnum type = JvsAppTemplateUtils.getContextVersionIterationType();
        if (ObjectNull.isNull(type)) {
            flowDesignVersionService.saveBatch(templateBo.getFlowDesignVersions());
            return;
        }

        // 模板中工作流的所有设计版本Map<工作流id, 工作流设计版本>
        Map<String, List<FlowDesignVersion>> templateFlowDesignVersionMap = templateBo.getFlowDesignVersions().stream()
                .collect(Collectors.groupingBy(FlowDesignVersion::getFlowDesignId));
        // 过滤出有版本的工作流设计。 没版本的不用处理
        List<FlowDesign> templateFlowDesigns = flowDesignList.stream()
                .filter(flowDesign -> ObjectNull.isNotNull(templateFlowDesignVersionMap.get(flowDesign.getId())))
                .collect(Collectors.toList());
        if (ObjectNull.isNull(templateFlowDesigns)) {
            return;
        }

        // 轻应用版本提交, 保存工作流版本设计
        if (VersionIterationTypeEnum.SUBMIT_VERSION.equals(type) || VersionIterationTypeEnum.SWITCH_VERSION.equals(type)) {
            submitVersionSaveFlowDesignVersion(templateFlowDesigns, templateFlowDesignVersionMap, templateBo.getFlowDesignVersions());
        }

        // 轻应用版本上传离线包，保存工作流版本设计
        if (VersionIterationTypeEnum.UPLOAD_VERSION.equals(type)) {
            uploadVersionSaveFlowDesignVersion(templateFlowDesigns, templateFlowDesignVersionMap);
        }
    }

    /**
     * 轻应用版本提交时保存工作流版本设计
     *
     * @param templateFlowDesigns          工作流设计集合
     * @param templateFlowDesignVersionMap 模板中工作流的所有设计版本Map<工作流id, 工作流设计版本>
     * @param templateFlowDesignVersions   工作流设计版本集合
     */
    private void submitVersionSaveFlowDesignVersion(List<FlowDesign> templateFlowDesigns, Map<String, List<FlowDesignVersion>> templateFlowDesignVersionMap, List<FlowDesignVersion> templateFlowDesignVersions) {
        // 查询工作流已存在的设计版本Map<工作流id, 工作流设计版本集合>
        List<String> flowDesignIds = templateFlowDesigns.stream().map(FlowDesign::getId).collect(Collectors.toList());
        Map<String, List<FlowDesignVersion>> flowDesignVersionMap = flowDesignVersionService.getFlowDesignAllVersion(flowDesignIds);

        // 待保存新的"USE"版本的工作流设计id
        List<String> newUseVersionDesignIds = new ArrayList<>();
        // 封装要保存的工作流设计版本
        for (FlowDesign templateFlowDesign : templateFlowDesigns) {
            String flowDesignId = templateFlowDesign.getId();
            // 工作流未发布
            // 移除"USE"的版本（没发布，不需要同步"USE"的版本）
            if (Boolean.FALSE.equals(templateFlowDesign.getPublished())) {
                templateFlowDesignVersions.removeIf(v -> v.getFlowDesignId().equals(flowDesignId) && FlowDesignVersionStatusEnum.USE.equals(v.getVersionStatus()));
                continue;
            }

            // 工作流已发布
            // 若工作流设计版本id已存在，表示已同步过，不需要再同步
            FlowDesignVersion templateUseDesignVersion = Optional.ofNullable(getDesignVersion(flowDesignId, FlowDesignVersionStatusEnum.USE, templateFlowDesignVersionMap)).orElseGet(FlowDesignVersion::new);
            Optional<FlowDesignVersion> optionalFlowDesignVersion = Optional.ofNullable(flowDesignVersionMap.get(flowDesignId))
                    .orElseGet(ArrayList::new).stream().filter(v -> v.getId().equals(templateUseDesignVersion.getId())).findAny();
            if (optionalFlowDesignVersion.isPresent()) {
                templateFlowDesignVersions.removeIf(v -> v.getId().equals(optionalFlowDesignVersion.get().getId()));
            } else {
                // 工作流设计版本id不已存在，表示未同步过，需要修改版本号
                List<FlowDesignVersion> flowDesignVersionList = flowDesignVersionMap.get(templateFlowDesign.getId());
                Integer versionNum = flowDesignVersionService.getNextVersion(flowDesignVersionList);
                templateFlowDesignVersions.forEach(v -> {
                    if (v.getId().equals(templateUseDesignVersion.getId())) {
                        v.setDesignVersion(versionNum);
                        newUseVersionDesignIds.add(v.getFlowDesignId());
                    }
                });
            }
        }

        // 修改"USE"的设计为"历史"设计
        if (ObjectNull.isNotNull(newUseVersionDesignIds)) {
            flowDesignVersionService.update(Wrappers.<FlowDesignVersion>lambdaUpdate()
                    .set(FlowDesignVersion::getVersionStatus, FlowDesignVersionStatusEnum.HISTORY)
                    .in(FlowDesignVersion::getFlowDesignId, newUseVersionDesignIds)
                    .eq(FlowDesignVersion::getVersionStatus, FlowDesignVersionStatusEnum.USE));
        }
        // 保存新的设计
        if (ObjectNull.isNull(templateFlowDesignVersions)) {
            return;
        }
        flowDesignVersionService.saveOrUpdateBatch(templateFlowDesignVersions);
    }


    /**
     * 轻应用版本离线包上传时保存工作流版本设计
     *
     * @param templateFlowDesigns  工作流设计集合
     * @param templateFlowDesignVersionMap 模板中工作流的所有设计版本Map<工作流id, 工作流设计版本>
     */
    private void uploadVersionSaveFlowDesignVersion(List<FlowDesign> templateFlowDesigns, Map<String, List<FlowDesignVersion>> templateFlowDesignVersionMap) {
        // 获取已存在的工作流设计（"DESIGNING"）版本。 Map<工作流id, 工作流设计版本>
        List<String> flowDesignIds = templateFlowDesigns.stream().map(FlowDesign::getId).collect(Collectors.toList());
        Map<String, FlowDesignVersion> flowDesignVersionMap = flowDesignVersionService.getBatchDesignVersion(flowDesignIds, FlowDesignVersionStatusEnum.DESIGNING);

        // 待入库的工作流设计版本
        List<FlowDesignVersion> flowDesignVersions = new ArrayList<>();
        for (FlowDesign templateFlowDesign : templateFlowDesigns) {
            String flowDesignId = templateFlowDesign.getId();
            // 模板中的设计版本（优先获取"USE"）
            FlowDesignVersion templateDesignVersion = Optional.ofNullable(getDesignVersion(flowDesignId, FlowDesignVersionStatusEnum.USE, templateFlowDesignVersionMap))
                    .orElseGet(() -> getDesignVersion(flowDesignId, FlowDesignVersionStatusEnum.DESIGNING, templateFlowDesignVersionMap));
            // 剔除模板中设计版本中的变量
            String templateExtractVariableDesignBody = FlowVariableUtil.extractVariable(templateDesignVersion.getDesignBody());

            // 已保存的“DESIGNING"版本
            FlowDesignVersion designingVersion = flowDesignVersionMap.get(flowDesignId);
            if (ObjectNull.isNull(designingVersion)) {
                // 没有"DESIGNING"版本，直接将模板中的版本设计剔除变量后保存到"DESIGNING"版本中。并修改工作流为未发布状态
                templateDesignVersion
                        .setVersionStatus(FlowDesignVersionStatusEnum.DESIGNING)
                        .setDesignBody(templateExtractVariableDesignBody)
                        .setDesignVersion(0);
                templateFlowDesign.setPublished(Boolean.FALSE).setDesignChanged(Boolean.TRUE);
                flowDesignVersions.add(templateDesignVersion);
                continue;
            }

            // 有"DESIGNING"版本，对比模板中版本
            String extractVariableDesignBody = FlowVariableUtil.extractVariable(designingVersion.getDesignBody());
            if (Boolean.FALSE.equals(FlowUtil.checkDesignChange(extractVariableDesignBody, templateExtractVariableDesignBody))) {
                // 已存储的"DESIGNING"版本 与 模板中的版本不同，对比所有节点，并重新组装设计
                String newDesignBody = FlowVariableUtil.compareAndFillVariable(designingVersion.getDesignBody(), templateExtractVariableDesignBody);
                designingVersion.setDesignBody(newDesignBody);
                // 修改工作流为未发布状态
                templateFlowDesign.setPublished(Boolean.FALSE).setDesignChanged(Boolean.TRUE);
                flowDesignVersions.add(designingVersion);
            }
        }

        if (ObjectNull.isNull(flowDesignVersions)) {
            return;
        }
        flowDesignVersionService.saveOrUpdateBatch(flowDesignVersions);
    }

    /**
     * 获取工作流设计版本
     *
     * @param flowDesignId         工作流设计id
     * @param versionStatus        工作流设计版本状态
     * @param flowDesignVersionMap Map<工作流id, 工作流设计版本>
     * @return 工作流设计版本
     */
    private FlowDesignVersion getDesignVersion(String flowDesignId, FlowDesignVersionStatusEnum versionStatus, Map<String, List<FlowDesignVersion>> flowDesignVersionMap) {
        Optional<FlowDesignVersion> flowDesignVersionOptional = flowDesignVersionMap.get(flowDesignId)
                .stream()
                .filter(v -> versionStatus.equals(v.getVersionStatus()))
                .findAny();
        return flowDesignVersionOptional.orElse(null);
    }
}
