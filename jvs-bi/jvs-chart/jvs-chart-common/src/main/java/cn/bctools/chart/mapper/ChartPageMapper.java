package cn.bctools.chart.mapper;

import cn.bctools.chart.entity.ChartPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 页面配置
 *
 * @author zqs
 */
@Mapper
public interface ChartPageMapper extends BaseMapper<ChartPage> {
    /**
     * 修改删除状态的值
     *
     * @param id 数据id
     */
    @Update("update jvs_chart_page set del_flag = 0  where id =#{id}")
    void retrieve(@Param("id") String id);
}
