package cn.bctools.design.project.entity;

import cn.bctools.design.crud.entity.*;
import cn.bctools.design.data.entity.*;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.workflow.entity.*;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TemplateBo extends TemplateIdsBo {

    List<FormPo> formPoList;
    List<AppUrlPo> appUrlPos;
    List<CrudPage> crudPageList;
    List<DataFieldPo> dataFieldPos;
    List<DataIdPo> dataIdPoList;
    List<DataModelPo> dataModelPoList;
    List<DynamicDataPo> dynamicDataPos;
    List<FlowDesign> flowDesigns;
    List<FlowDesignVersion> flowDesignVersions;
    List<FlowPurview> flowPurviews;
    List<FlowTask> flowTasks;
    List<FlowTaskApprovalRecord> flowTaskApprovalRecords;
    List<FlowTaskCopied> flowTaskCarbonCopies;
    List<FlowTaskNode> flowTaskNodes;
    List<FlowTaskParallel> flowTaskParallels;
    List<FlowTaskPath> flowTaskPaths;
    List<FlowTaskPerson> flowTaskPersons;
    List<RuleDesignPo> ruleDesignPos;
    List<FunctionBusinessPo> functionBusinessPos;
    List<CrudAssociationPo> crudAssociationPos;
    List<DataEventPo> dataEventPos;
    List<DataNoticePo> dataNoticePos;
    List<PrintTemplate> printTemplates;
    List<ExternalPage> externalPages;
    List<AppMenuType> appMenuTypes;
    List<AppMenu> appMenus;
    List<Identification> identifications;
}
