package cn.bctools.design.workflow.service.impl;

import cn.bctools.auth.api.dto.SearchUserDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.util.SqlFunctionUtil;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowPurview;
import cn.bctools.design.workflow.mapper.FlowPurviewMapper;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.PurviewGroupEnum;
import cn.bctools.design.workflow.model.enums.PurviewPersonTypeEnum;
import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import cn.bctools.design.workflow.model.properties.Purview;
import cn.bctools.design.workflow.service.FlowPurviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流权限配置 服务实现类
 */
@Service
@AllArgsConstructor
public class FlowPurviewServiceImpl extends ServiceImpl<FlowPurviewMapper, FlowPurview> implements FlowPurviewService {

    private final JvsAppVersionService appVersionService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(FlowDesign flowDesign, Node node) {
        String flowDesignId = flowDesign.getId();
        List<Purview> purviewList = Optional.ofNullable(node.getProps().getPurviews()).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(purviewList)) {
            // 未配置权限，则设置默认权限
            purviewList.add(Purview.defaultPurview());
        }
        List<FlowPurview> flowPurviewList = new ArrayList<>();
        purviewList.forEach(purview -> {
            List<String> userIds = getStartPurviewIds(purview, TargetObjectTypeEnum.user);
            List<String> deptIds = getStartPurviewIds(purview, TargetObjectTypeEnum.dept);
            List<String> roleIds = getStartPurviewIds(purview, TargetObjectTypeEnum.role);
            List<String> jobIds = getStartPurviewIds(purview, TargetObjectTypeEnum.job);
            flowPurviewList.add(new FlowPurview()
                    .setFlowDesignId(flowDesignId)
                    .setJvsAppId(flowDesign.getJvsAppId())
                    .setPurviewGroup(purview.getGroup())
                    .setPersonType(purview.getPersonType())
                    .setUsers(userIds)
                    .setDepts(deptIds)
                    .setRoles(roleIds)
                    .setJobs(jobIds));
        });
        // 删除旧的权限配置
        remove(Wrappers.<FlowPurview>lambdaQuery().eq(FlowPurview::getFlowDesignId, flowDesignId));
        // 保存新的权限配置
        saveBatch(flowPurviewList);
    }

    /**
     * 解析工作流设计，得到可发起人id/部门id集合
     *
     * @param type 可发起人对象类型
     * @return
     */
    private List<String> getStartPurviewIds(Purview purview, TargetObjectTypeEnum type) {
        List<String> ids = purview.getPersonnelIdByType(type);
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        return ids;
    }

    /**
     * 构造查询用户指定权限组权限的基础sql
     *
     * @param purviewGroupEnum 权限组枚举
     * @param user 用户
     * @return 用户指定权限组权限的基础查询条件
     */
    private LambdaQueryWrapper<FlowPurview> basePermissionCondition(PurviewGroupEnum purviewGroupEnum, UserDto user) {
        // 查询发起人信息
        SearchUserDto search = new SearchUserDto();
        search.setUserIds(Collections.singletonList(user.getId()));
        String roleIdSql = roleIdsConditionSql(user.getRoleIds());
        return Wrappers.<FlowPurview>lambdaQuery()
                .eq(FlowPurview::getPurviewGroup, purviewGroupEnum)
                .and(wrapper -> wrapper.eq(FlowPurview::getPersonType, PurviewPersonTypeEnum.all)
                        .or(orUser -> orUser.apply(SqlFunctionUtil.jsonContains("users", user.getId(), "$")))
                        .or(StringUtils.isNotBlank(user.getDeptId()), orDept -> orDept.apply(SqlFunctionUtil.jsonContains("depts", user.getDeptId(), "$")))
                        .or(StringUtils.isNotBlank(roleIdSql), orRole -> orRole.apply(roleIdSql))
                        .or(StringUtils.isNotBlank(user.getJobId()), orJob -> orJob.apply(SqlFunctionUtil.jsonContains("jobs", user.getJobId(), "$")))
                );
    }

    @Override
    public List<String> havePermissionDesign(PurviewGroupEnum purviewGroupEnum, UserDto userDto) {
        // 查询当前模式应用id集合
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return Collections.emptyList();
        }
        // 查询用户的工作流权限
        LambdaQueryWrapper<FlowPurview> lambdaQueryWrapper = basePermissionCondition(purviewGroupEnum, userDto)
                .in(FlowPurview::getJvsAppId, appIds)
                .select(FlowPurview::getFlowDesignId, FlowPurview::getJvsAppId);
        List<FlowPurview> flowPurviews = list(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(flowPurviews)) {
            return Collections.emptyList();
        }
        // 有权限的工作流设计id
        return flowPurviews.stream()
                .map(FlowPurview::getFlowDesignId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 封装角色id查询sql
     *
     * @param roleIds 角色id集合
     * @return 角色id查询sql片段
     */
    private String roleIdsConditionSql(List<String> roleIds) {
        StringBuilder queryRoleIdStr = new StringBuilder();
        if (ObjectNull.isNotNull(roleIds)) {
            for (int i = 0; i < roleIds.size(); i++) {
                queryRoleIdStr.append(SqlFunctionUtil.jsonContains("roles", roleIds.get(i), "$"));
                if (i != roleIds.size() - 1) {
                    queryRoleIdStr.append(" OR ");
                }
            }
        }
        return queryRoleIdStr.toString();
    }

    @Override
    public void delete(String flowDesignId) {
        remove(Wrappers.<FlowPurview>lambdaQuery().eq(FlowPurview::getFlowDesignId, flowDesignId));
    }

    @Override
    public Boolean checkPermission(UserDto userDto, PurviewGroupEnum purviewGroup, String flowDesignId) {
        LambdaQueryWrapper<FlowPurview> lambdaQueryWrapper = basePermissionCondition(purviewGroup, userDto)
                .eq(FlowPurview::getFlowDesignId, flowDesignId)
                .select(FlowPurview::getId);
        if (ObjectNull.isNull(getOne(lambdaQueryWrapper))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
