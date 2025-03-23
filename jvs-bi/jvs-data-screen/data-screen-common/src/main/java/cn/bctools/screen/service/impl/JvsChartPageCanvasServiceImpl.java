package cn.bctools.screen.service.impl;

import cn.bctools.common.utils.function.Get;
import cn.bctools.screen.chart.bo.ChartSettingBo;
import cn.bctools.screen.entity.JvsChartPageCanvas;
import cn.bctools.screen.enums.CanvasTypeEnum;
import cn.bctools.screen.mapper.JvsChartPageCanvasMapper;
import cn.bctools.screen.service.JvsChartPageCanvasService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 大屏-画布 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-08-18
 */
@Service
@RequiredArgsConstructor
public class JvsChartPageCanvasServiceImpl extends ServiceImpl<JvsChartPageCanvasMapper, JvsChartPageCanvas> implements JvsChartPageCanvasService {

    private final JvsChartPageCanvasMapper jvsChartPageCanvasMapper;

    @Override
    public void saveFormal(String chartId, List<JvsChartPageCanvas> canvasList, List<String> deleteCanvasIds) {
        if(CollectionUtil.isNotEmpty(deleteCanvasIds)){
            this.removeByIds(deleteCanvasIds);
        }
        saveCanvasBatch(chartId,canvasList,CanvasTypeEnum.formal);
    }

    @Override
    public void saveTemplate(String templateId, List<JvsChartPageCanvas> canvasList, List<String> deleteCanvasIds){
        if(CollectionUtil.isNotEmpty(deleteCanvasIds)){
            this.removeByIds(deleteCanvasIds);
        }
        if(CollectionUtil.isEmpty(canvasList)){
            return;
        }
        canvasList.stream().peek(e -> {
            if(StrUtil.isNotBlank(e.getDataJson())){
                List<JSONObject> setting = JSONObject.parseArray(e.getDataJson())
                        .stream()
                        .map(v -> {
                            JSONObject chart = JSONObject.parseObject(v.toString());
                            JSONObject chartSetting = chart.getJSONObject("setting");
                            if (ObjectUtil.isNotNull(chartSetting)) {
                                chartSetting.remove(Get.name(ChartSettingBo::getDataFilterJson));
                                chartSetting.remove(Get.name(ChartSettingBo::getDataSource));
                                chartSetting.remove(Get.name(ChartSettingBo::getSearchFilterJson));
                            }
                            return chart;
                        }).collect(Collectors.toList());
                e.setDataJson(JSONUtil.toJsonStr(setting));
            }
        }).collect(Collectors.toList());

        saveCanvasBatch(templateId,canvasList,CanvasTypeEnum.template);
    }

    private void saveCanvasBatch(String parentId, List<JvsChartPageCanvas> canvasList, CanvasTypeEnum type){
        //删除再新增
        if(CollectionUtil.isNotEmpty(canvasList)){
            List<JvsChartPageCanvas> collect = canvasList.stream()
                    .map(e -> e.setChartId(parentId).setCanvasType(type)).collect(Collectors.toList());
            this.saveOrUpdateBatch(collect);
        }
    }

    @Override
    public void removeByParentId(String id) {
        this.remove(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId,id));
    }

    @Override
    public void copy(String templateId, String screenId) {
        List<JvsChartPageCanvas> list = this.list(Wrappers.lambdaQuery(JvsChartPageCanvas.class).eq(JvsChartPageCanvas::getChartId, templateId));
        list.stream().peek(e -> e.setId(null).setCanvasType(CanvasTypeEnum.formal).setChartId(screenId)).collect(Collectors.toList());
        this.saveBatch(list);
    }

    @Override
    public void forcedDeletion(String chartPageId) {
        jvsChartPageCanvasMapper.deleteByChartId(chartPageId);
    }
}
