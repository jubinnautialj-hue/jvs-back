package cn.bctools.screen.mapper;

import cn.bctools.screen.entity.JvsChartPageCanvas;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 大屏-画布 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2023-08-18
 */
@Mapper
public interface JvsChartPageCanvasMapper extends BaseMapper<JvsChartPageCanvas> {

    @Delete("DELETE FROM jvs_chart_page_canvas WHERE chart_id = #{chartPageId}")
    int deleteByChartId( String chartPageId);
}
