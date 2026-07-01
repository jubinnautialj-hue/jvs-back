package cn.bctools.design.workflow.mapper;

import cn.bctools.design.workflow.entity.FlowTaskNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhuxiaokang
 * 工作流流转节点 Mapper 接口
 */
@Mapper
public interface FlowTaskNodeMapper extends BaseMapper<FlowTaskNode> {

}
