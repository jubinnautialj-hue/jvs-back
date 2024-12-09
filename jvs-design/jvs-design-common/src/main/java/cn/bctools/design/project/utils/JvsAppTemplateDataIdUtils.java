package cn.bctools.design.project.utils;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.config.DesignIdentifierGenerator;
import cn.bctools.design.crud.entity.CrudPage;
import cn.bctools.design.crud.entity.FormPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.enums.DesignIdPrefixEnum;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.workflow.entity.FlowDesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 */
public class JvsAppTemplateDataIdUtils {
    private JvsAppTemplateDataIdUtils() {

    }

    private static final DesignIdentifierGenerator DESIGN_IDENTIFIER_GENERATOR = SpringContextUtil.getBean(DesignIdentifierGenerator.class);

    /**
     * 生成新id
     *
     * @param whetherNewId  true-生成新id，false-不生成新id
     * @param id            待解析的id
     * @param templateBo    模板数据
     * @return 新id
     */
    public static String getNewId(Boolean whetherNewId, String id, TemplateBo templateBo) {
        // 根据id确认设计类型
        // 优先根据前缀确定设计类型， 若前缀无法确定设计类型，则根据id，到各个集合中匹配，以确定当前id的设计类型
        DesignType designType = DesignIdPrefixEnum.getDesignTypeById(id);
        if (DesignType.other.equals(designType)) {
            // 根据id未找到类型，则根据id匹配模板中的数据以确定类型
            designType = unknownTypeFindNode(id, templateBo);
        }
        // 根据设计类型生成id
        return whetherNewId ? DESIGN_IDENTIFIER_GENERATOR.nextUUID(designType) : id;
    }

    /**
     * 根据id获取未知设计类型
     *
     * @param id
     * @param templateBo
     * @return
     */
    private static DesignType unknownTypeFindNode(String id, TemplateBo templateBo) {
        // 表单
        if (formNode(id, templateBo.getFormPoList())) {
            return DesignType.form;
        }
        // 列表
        if (pageNode(id, templateBo.getCrudPageList())) {
            return DesignType.page;
        }
        // 工作流
        if (flowNode(id, templateBo.getFlowDesigns())) {
            return DesignType.workflow;
        }
        // 逻辑
        if (ruleNode(id, templateBo.getRuleDesignPos())) {
            return DesignType.rule;
        }
        // 模型
        if (modelNode(id, templateBo.getDataModelPoList())) {
            return DesignType.data;
        }
        return null;
    }

    /**
     * 获取表单节点
     *
     * @param id
     * @param formPos
     * @return
     */
    private static Boolean formNode(String id, List<FormPo> formPos) {
        return Optional.ofNullable(formPos)
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(d -> d.getId().equals(id));
    }

    /**
     * 获取列表节点
     *
     * @param id
     * @param crudPages
     * @return
     */
    private static Boolean pageNode(String id, List<CrudPage> crudPages) {
        return Optional.ofNullable(crudPages)
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(d -> d.getId().equals(id));
    }

    /**
     * 获取工作流节点
     *
     * @param id
     * @param flowPos
     * @return
     */
    private static Boolean flowNode(String id, List<FlowDesign> flowPos) {
        return Optional.ofNullable(flowPos)
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(d -> d.getId().equals(id));
    }

    /**
     * 获取逻辑引擎节点
     *
     * @param id
     * @param ruleDesignPos
     * @return
     */
    private static Boolean ruleNode(String id, List<RuleDesignPo> ruleDesignPos) {
        return Optional.ofNullable(ruleDesignPos)
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(d -> d.getId().equals(id) || d.getSecret().equals(id));
    }

    /**
     * 获取模型节点
     *
     * @param id
     * @param dataModelPos
     * @return
     */
    private static Boolean modelNode(String id, List<DataModelPo> dataModelPos) {
        return Optional.ofNullable(dataModelPos)
                .orElseGet(ArrayList::new)
                .stream()
                .anyMatch(d -> d.getId().equals(id));
    }


}
