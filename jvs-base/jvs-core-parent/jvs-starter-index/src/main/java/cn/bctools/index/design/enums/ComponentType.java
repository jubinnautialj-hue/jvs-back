package cn.bctools.index.design.enums;

import cn.bctools.index.design.ComponentBaseInfo;
import cn.bctools.index.design.component.*;
import cn.bctools.index.design.component.service.*;
import cn.bctools.index.design.component.service.ComponentFrameService;
import cn.bctools.index.dto.FormQueryParamsBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;

/**
 * 搜索
 * 目录
 * 标题
 * 时钟
 * 日历
 * 卡片
 *
 * @author jvs
 */
@Getter
@AllArgsConstructor
public enum ComponentType {
    /**
     * 普通工具
     */
    Label("标签文本", ComponentLabelService.class, ComponentLabel.class),
    Navigation("导航栏", ComponentNavigationService.class, ComponentNavigation.class),
    SearchNav("可搜索导航栏", ComponentSearchNavService.class, ComponentSearchNav.class),
    Day("日期", ComponentDayService.class, ComponentDay.class),
    Clock("时钟", ComponentClockService.class, ComponentClock.class),
    /**
     * 卡片
     */
    QuickCards("快捷卡片", ComponentQuickCardService.class, ComponentQuickCard.class),
    StandardCards("常规卡片", ComponentCardService.class, ComponentCard.class),
    DataCards("数据卡片", ComponentDataCardService.class, ComponentDataCard.class),
    StepCard("步骤卡片", ComponentStepCardService.class, ComponentStepCard.class),
    QuickNavigation("快捷导航", ComponentQuickNavigationService.class, ComponentQuickNavigation.class),
    CodeCards("二维码", ComponentCodeCardService.class, ComponentCodeCard.class),
    MediaCards("媒体卡片", ComponentMediaCardService.class, ComponentMediaCard.class),
    /**
     * 低代码
     */
    LinkNavigation("链接导航", LinkComponentNavigationService.class, LinkComponentNavigation.class),
    ProcessManagement("流程管理", ComponentProcessManagementService.class, ComponentProcessManagement.class),
    CRUD("列表页组件", ComponentCrudService.class, ComponentCrud.class),
    OA("OA组件", ComponentOaService.class, ComponentOa.class),
    OaTask("OA任务组件", ComponentOaTaskService.class, ComponentOaTask.class),
    Message("消息组件", ComponentMessageService.class, ComponentMessage.class),
    Calendar("日历组件", ComponentCalendarService.class, ComponentCalendar.class),
    Notice("公告组件", ComponentNoticeService.class, ComponentNotice.class),
    Frame("嵌入页面", ComponentFrameService.class, ComponentFrame.class),
    DesignApp("轻应用", DesignAppFrameService.class, DesignApp.class),
    ProjectNavigation("项目导航", ComponentProjectNavigationService.class, ComponentProjectNavigation.class),
    /**
     * 图表
     */
    ChartPie("饼状图", ComponentChartPieService.class, ComponentChartPie.class),
    ChartLine("折线图", ComponentChartLineService.class, ComponentChartLine.class),
    ChartHistogram("柱状图", ComponentChartHistogramService.class, ComponentChartHistogram.class),
    /**
     * banner
     */
    Banner("banner", ComponentBannerService.class, ComponentBanner.class),
    /**
     * 动态
     */
    Dynamic("动态", ComponentDynamicService.class, ComponentDynamic.class),
    ;

    /**
     * 描述
     */
    private final String desc;
    /**
     * 对应的服务类
     */
    private final Class<? extends ComponentBaseService> aClass;
    /**
     * 对应的类
     */
    private final Class<? extends ComponentBaseInfo> bClass;

    public static ComponentType bClass(Class<? extends ComponentBaseInfo> bclass) {
        for (ComponentType e : ComponentType.values()) {
            if (e.bClass.equals(bclass)) {
                return e;
            }
        }
        return null;
    }

    public static Class<? extends ComponentBaseInfo> serverClass(Class<? extends ComponentBaseService> aclass) {
        for (ComponentType e : ComponentType.values()) {
            if (e.aClass.equals(aclass)) {
                return e.getBClass();
            }
        }
        return null;
    }

}
