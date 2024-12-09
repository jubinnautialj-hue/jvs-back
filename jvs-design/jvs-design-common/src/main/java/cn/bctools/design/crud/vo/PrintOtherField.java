package cn.bctools.design.crud.vo;

import cn.bctools.common.utils.function.Get;
import cn.bctools.design.workflow.dto.progress.ProgressPrintResDto;
import lombok.Data;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 表单打印模板其它字段
 */
@Data
public class PrintOtherField {
    /**
     * 字段前缀
     * 避免与表单设计的字段冲突
     */
    private static final String PREFIX = "jvs_other_";

    /**
     * 数据来源字段
     */
    public enum DataSourceType {
        // 工作流审批过程
        workflowProgress
    }

    /**
     * 工作流字段
     */
    @Getter
    public enum Flow {
        /**
         * 审批过程
         */
        WORKFLOW_PROGRESS(PREFIX + DataSourceType.workflowProgress.name(), "审批过程", "表格"),
        /**
         * 审批节点
         */
        NODE_NAME(Get.name(ProgressPrintResDto::getNodeName), "审批节点", ""),
        /**
         * 审批人
         */
        USER_NAME(Get.name(ProgressPrintResDto::getUserName), "审批人", ""),
        /**
         * 审批结果
         */
        OPERATION(Get.name(ProgressPrintResDto::getNodeOperation), "审批结果", ""),
        /**
         * 审批意见
         */
        CONTENT(Get.name(ProgressPrintResDto::getOpinionContent), "审批意见", ""),
        /**
         * 审批时间
         */
        TIME(Get.name(ProgressPrintResDto::getTime), "审批时间", ""),
        ;

        private String key;
        private String name;
        private String desc;

        Flow(String key, String name, String desc) {
            this.key = key;
            this.name = name;
            this.desc = desc;
        }
    }

}
