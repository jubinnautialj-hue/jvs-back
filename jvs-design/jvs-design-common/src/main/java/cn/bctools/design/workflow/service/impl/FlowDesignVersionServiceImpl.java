package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.enums.FlowDesignVersionStatusEnum;
import cn.bctools.design.workflow.mapper.FlowDesignVersionMapper;
import cn.bctools.design.workflow.service.FlowDesignVersionService;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Service
@AllArgsConstructor
public class FlowDesignVersionServiceImpl extends ServiceImpl<FlowDesignVersionMapper, FlowDesignVersion> implements FlowDesignVersionService {

    private final RedisUtils redisUtils;
    private static final String SAVE_VERSION_DESIGNING_LOCK = "saveVersionDesigningLock";
    private static final Integer LOCK_EXPIRE_TIME = 120;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveVersionDesigning(String flowDesignId, String appId, String designBody) {
        String lockKey = SystemConstant.SYSTEM_NAME + ":" + SAVE_VERSION_DESIGNING_LOCK + ":" + flowDesignId;
        boolean lock = redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME);
        if (Boolean.FALSE.equals(lock)) {
            return;
        }
        try {
            FlowDesignVersion flowDesignVersion = getOne(Wrappers.<FlowDesignVersion>lambdaQuery()
                    .eq(FlowDesignVersion::getFlowDesignId, flowDesignId)
                    .eq(FlowDesignVersion::getVersionStatus, FlowDesignVersionStatusEnum.DESIGNING));
            if (ObjectNull.isNull(flowDesignVersion)) {
                flowDesignVersion = new FlowDesignVersion()
                        .setFlowDesignId(flowDesignId)
                        .setVersionStatus(FlowDesignVersionStatusEnum.DESIGNING)
                        .setDesignVersion(0)
                        .setJvsAppId(appId);
            }
            flowDesignVersion.setDesignBody(designBody);
            saveOrUpdate(flowDesignVersion);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(String flowDesignId) {
        List<FlowDesignVersion> flowDesignVersions = list(Wrappers.<FlowDesignVersion>lambdaQuery().eq(FlowDesignVersion::getFlowDesignId, flowDesignId));
        // 得到设计中的版本
        FlowDesignVersion versionDesigning = flowDesignVersions.stream()
                .filter(v -> FlowDesignVersionStatusEnum.DESIGNING.equals(v.getVersionStatus()))
                .findAny()
                .orElseThrow(() -> new BusinessException("工作流不存在"));
        // 得到版本号
        Integer version = getNextVersion(flowDesignVersions);
        // 以设计中的版本为原本，生成使用中的版本
        FlowDesignVersion versionUse = BeanCopyUtil.copy(versionDesigning, FlowDesignVersion.class)
                .setId(null)
                .setDesignBody(versionDesigning.getDesignBody())
                .setDesignVersion(version)
                .setVersionStatus(FlowDesignVersionStatusEnum.USE);
        save(versionUse);

        // 修改其它使用中的版本为历史版本
        List<FlowDesignVersion> oldUseVersions = flowDesignVersions.stream()
                .filter(v -> FlowDesignVersionStatusEnum.USE.equals(v.getVersionStatus()))
                .map(v -> v.setVersionStatus(FlowDesignVersionStatusEnum.HISTORY))
                .collect(Collectors.toList());
        if (ObjectNull.isNull(oldUseVersions)) {
            return;
        }
        updateBatchById(oldUseVersions);
    }

    @Override
    public Integer getNextVersion(List<FlowDesignVersion> flowDesignVersions) {
        return Optional.ofNullable(flowDesignVersions).orElseGet(ArrayList::new)
                .stream()
                .max(Comparator.comparing(FlowDesignVersion::getDesignVersion))
                .map(FlowDesignVersion::getDesignVersion)
                .orElse(0) + 1;
    }

    @Override
    public FlowDesignVersion getVersion(String flowDesignId, FlowDesignVersionStatusEnum versionStatus) {
        if (FlowDesignVersionStatusEnum.HISTORY.equals(versionStatus)) {
            // 一个工作流可以有多个历史版本，所以此方法不支持根据设计id获取设计
            throw new BusinessException("获取工作流设计失败");
        }
        return getOne(Wrappers.<FlowDesignVersion>lambdaQuery()
                .eq(FlowDesignVersion::getFlowDesignId, flowDesignId)
                .eq(FlowDesignVersion::getVersionStatus, versionStatus));
    }

    @Override
    public Map<String, String> getBatchDesignBody(Collection<FlowDesign> flowDesigns, FlowDesignVersionStatusEnum versionStatus) {
        if (ObjectNull.isNull(flowDesigns)) {
            return Collections.emptyMap();
        }
        if (FlowDesignVersionStatusEnum.HISTORY.equals(versionStatus)) {
            // 一个工作流可以有多个历史版本，所以此方法不支持根据设计id批量获取设计
            throw new BusinessException("获取工作流设计失败");
        }
        // 查询工作流版本表中的设计
        List<String> flowDesignIds = flowDesigns.stream().map(FlowDesign::getId).collect(Collectors.toList());
        List<FlowDesignVersion> flowDesignVersions = list(Wrappers.<FlowDesignVersion>lambdaQuery()
                .in(FlowDesignVersion::getFlowDesignId, flowDesignIds)
                .eq(FlowDesignVersion::getVersionStatus, versionStatus));
        Map<String, String> flowVersionDesignMap = flowDesignVersions.stream()
                .collect(Collectors.toMap(FlowDesignVersion::getFlowDesignId, FlowDesignVersion::getDesignBody));

        // 旧的工作流设计，还是保存在工作流设计表中，所以需要将该部分设计也返回
        Map<String, String> oldFlowDesignMap = flowDesigns.stream()
                .filter(flowDesign -> ObjectNull.isNotNull(flowDesign.getDesigning()) && ObjectNull.isNotNull(flowDesign.getDesign()))
                .collect(Collectors.toMap(FlowDesign::getId, flowDesign -> FlowDesignVersionStatusEnum.USE.equals(versionStatus) ? flowDesign.getDesign() : flowDesign.getDesigning()));
        flowVersionDesignMap.putAll(oldFlowDesignMap);
        return flowVersionDesignMap;
    }

    @Override
    public Map<String, FlowDesignVersion> getBatchDesignVersion(Collection<String> flowDesignIds, FlowDesignVersionStatusEnum versionStatus) {
        if (FlowDesignVersionStatusEnum.HISTORY.equals(versionStatus)) {
            // 一个工作流可以有多个历史版本，所以此方法不支持根据设计id批量获取设计
            throw new BusinessException("获取工作流设计失败");
        }
        if (ObjectNull.isNull(flowDesignIds)) {
            return Collections.emptyMap();
        }
        List<FlowDesignVersion> flowDesignVersions = list(Wrappers.<FlowDesignVersion>lambdaQuery()
                .in(FlowDesignVersion::getFlowDesignId, flowDesignIds)
                .eq(FlowDesignVersion::getVersionStatus, versionStatus));

        // 数据结构：Map<工作流设计id, 工作流设计版本>
        return flowDesignVersions.stream()
                .collect(Collectors.toMap(FlowDesignVersion::getFlowDesignId, Function.identity()));
    }

    @Override
    public Map<String, List<FlowDesignVersion>> getFlowDesignAllVersion(List<String> flowDesignIds) {
        return list(Wrappers.<FlowDesignVersion>lambdaQuery().in(FlowDesignVersion::getFlowDesignId, flowDesignIds))
                .stream()
                .collect(Collectors.groupingBy(FlowDesignVersion::getFlowDesignId));
    }
}
