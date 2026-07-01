package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowPurview;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流权限配置 服务类
 */
public interface FlowPurviewService extends IService<FlowPurview> {

    /**
     * 保存工作流权限设计
     *
     * @param flowDesign 工作流设计
     * @param node       工作流设计
     */
    void save(FlowDesign flowDesign, Node node);

    /**
     * 获取有权限的工作流设计
     *
     * @param purviewGroupEnum 权限组
     * @param user             用户
     * @return 有权限的工作流设计id集合
     */
    List<String> havePermissionDesign(PurviewGroupEnum purviewGroupEnum, UserDto user);

    /**
     * 删除工作流设计相关权限配置
     *
     * @param flowDesignId 工作流设计id
     */
    void delete(String flowDesignId);

    /**
     * 校验用户是否有指定工作流的指定权限
     *
     * @param userDto      用户
     * @param purviewGroup 权限
     * @param flowDesignId 工作流设计
     * @return TRUE-有权限，FALSE-无权限
     */
    Boolean checkPermission(UserDto userDto, PurviewGroupEnum purviewGroup, String flowDesignId);
}
