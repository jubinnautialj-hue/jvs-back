package cn.bctools.design.workflow.mapper;

import cn.bctools.design.workflow.entity.FlowTaskPath;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

;

/**
 * @author zhuxiaokang
 * 工作流任务可执行路径  Mapper 接口
 */
@Mapper
public interface FlowTaskPathMapper extends BaseMapper<FlowTaskPath> {
}
