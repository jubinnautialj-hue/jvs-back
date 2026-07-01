package cn.bctools.design.workflow.mapper;

import cn.bctools.design.workflow.entity.FlowTaskParallel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 工作流并行任务 Mapper 接口
 */
@Mapper
public interface FlowTaskParallelMapper extends BaseMapper<FlowTaskParallel> {
}
