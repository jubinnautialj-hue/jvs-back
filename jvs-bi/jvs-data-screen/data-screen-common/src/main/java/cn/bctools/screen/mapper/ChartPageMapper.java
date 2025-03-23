package cn.bctools.screen.mapper;

import cn.bctools.screen.entity.ChartPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
 * 页面配置
 *
 * @author zqs
 */
public interface ChartPageMapper extends BaseMapper<ChartPage> {

    @Delete("DELETE FROM jvs_chart_page WHERE id = #{id} ")
    void forcedDeletion(String id);

}
