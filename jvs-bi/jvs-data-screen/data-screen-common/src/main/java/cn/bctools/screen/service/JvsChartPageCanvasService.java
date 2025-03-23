package cn.bctools.screen.service;

import cn.bctools.screen.entity.JvsChartPageCanvas;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 大屏-画布 服务类
 * </p>
 *
 * @author admin
 * @since 2023-08-18
 */
public interface JvsChartPageCanvasService extends IService<JvsChartPageCanvas> {

    /**
     * 保存画布
     * @param chartId
     * @param canvasList
     * @param deleteCanvasIds
     */
    void saveFormal(String chartId, List<JvsChartPageCanvas> canvasList, List<String> deleteCanvasIds);

    /**
     * 保存画布 模板
     * @param templateId 模板id
     * @param canvasList
     * @param deleteCanvasIds
     */
    void saveTemplate(String templateId, List<JvsChartPageCanvas> canvasList, List<String> deleteCanvasIds);

    /**
     * 按 大屏或模板id删除画布
     * @param id
     */
    void removeByParentId(String id);

    /**
     * 复制画布
     * @param templateId
     * @param screenId
     */
    void copy(String templateId,String screenId);

    void forcedDeletion(String chartPageId);
}
