package cn.bctools.chart.enums;

import cn.bctools.chart.chart.*;

/**
 * 组件类型
 *
 * @author guojing
 */
public enum ChartElementTypeEnums {

    LineChart("基础折线图", BasicsHistogramChartBo.class),
    BasicsHistogramChart("基础柱状图", BasicsHistogramChartBo.class),
    HistogramChart("直方图", HistogramChartBo.class),
    BarChart("基础条形图", BasicsBarChartBo.class),
    Pie("基础饼图", BasicsPieChartBo.class),
    FunnelChart("漏斗图", BasicsPieChartBo.class),
    AreaChart("面积图", BasicsHistogramChartBo.class),
    DashBoardChart("仪表盘", DashBoarChartBo.class),
    DataCard("数据卡片", DataCardBo.class),
    StaticCard("统计数据卡片", DataCardBo.class),
    InfoCard("信息卡片", DataCardBo.class),
    TrendCard("趋势卡片", TrendCardBo.class),
    BaseList("基础列表", BaseListChartBo.class),
    CrossTable("交叉列表", CrossListChartBo.class),
    treeChart("树形图表", TreeChartBo.class),
    ScatterChart("散点图", ScatterDiagramBo.class),
    BubbleChart("气泡图", ScatterDiagramBo.class),
    radarChart("雷达图", RadarChartBo.class),
    basicsHistogram3DChart("3d柱状图", BasicsHistogram3DChartBo.class),
    ClassifyTable("分类表格", ClassifyTableChartBo.class),
    mapChart("地图", MapChartBo.class),
    sankeySimple("基础桑葚图", SankeyChartBo.class),
    funnelAlign("对比漏斗图", FunnelAlignChartBo.class),
    boxplotLightVelocity("基础盒须图", BoxplotLightVelocityChartBo.class),
    blendCharts("多Y轴混合图", BlendChartsChartBo.class);

    String msg;
    /**
     * 不同图表的具体实现类
     */
    Class<? extends ChartElementInterface> cls;

    ChartElementTypeEnums(String msg, Class<? extends ChartElementInterface> cls) {
        this.msg = msg;
        this.cls = cls;
    }


    public Class<? extends ChartElementInterface> getCls() {
        return cls;
    }

    public String getMsg() {
        return msg;
    }

}
