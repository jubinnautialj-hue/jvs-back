package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.CarbonCopyReqDto;
import cn.bctools.design.workflow.dto.CarbonCopyResDto;
import cn.bctools.design.workflow.entity.FlowTaskCopied;
import cn.bctools.design.workflow.mapper.FlowTaskCarbonCopyMapper;
import cn.bctools.design.workflow.service.FlowTaskCarbonCopyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author zhuxiaokang
 * 工作流抄送
 */

@Slf4j
@Service
@AllArgsConstructor
public class FlowTaskCarbonCopyServiceImpl  extends ServiceImpl<FlowTaskCarbonCopyMapper, FlowTaskCopied> implements FlowTaskCarbonCopyService {
    private final JvsAppVersionService appVersionService;

    @Override
    public void carbonCopys(Page<CarbonCopyResDto> page, UserDto userDto, CarbonCopyReqDto dto) {
        // 查询当前模式应用id集合
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return;
        }
        QueryWrapper queryWrapper = Wrappers.query()
                .eq("cc.user_id", userDto.getId())
                .in("cc.jvs_app_id", appIds)
                .like(StringUtils.isNotBlank(dto.getTaskCode()), "t.task_code", dto.getTaskCode())
                .like(StringUtils.isNotBlank(dto.getFlowName()), "t.name", dto.getFlowName())
                .like(StringUtils.isNotBlank(dto.getSendUser()), "t.create_by", dto.getSendUser());
        baseMapper.carbonCopyPage(page, userDto.getId(), queryWrapper);
        page.getRecords().forEach(data -> data.setFlowDesign(null));
    }

    @Override
    public Integer carbonCopyCount(List<String> appIds, UserDto userDto) {
        long count = count(Wrappers.<FlowTaskCopied>lambdaQuery().eq(FlowTaskCopied::getUserId, userDto.getId()).in(FlowTaskCopied::getJvsAppId, appIds));
        return Long.valueOf(count).intValue();
    }

    @Override
    public List<String> queryUserIdsByTaskId(String taskId) {
        List<FlowTaskCopied> carbonCopies = list(Wrappers.<FlowTaskCopied>lambdaQuery()
                .eq(FlowTaskCopied::getFlowTaskId, taskId)
                .select(FlowTaskCopied::getUserId));
        if (CollectionUtils.isEmpty(carbonCopies)) {
            return Collections.emptyList();
        }
        return carbonCopies.stream().map(FlowTaskCopied::getUserId).distinct().collect(Collectors.toList());
    }

    @Override
    public Map<String, Set<String>> queryUserIdsByTaskId(List<String> taskIds) {
        List<FlowTaskCopied> carbonCopies = list(Wrappers.<FlowTaskCopied>lambdaQuery()
                .in(FlowTaskCopied::getFlowTaskId, taskIds)
                .select(FlowTaskCopied::getUserId, FlowTaskCopied::getFlowTaskId));
        if (ObjectNull.isNull(carbonCopies)) {
            return Collections.emptyMap();
        }
        return carbonCopies.stream()
                .collect(Collectors.groupingBy(FlowTaskCopied::getFlowTaskId, Collectors.mapping(FlowTaskCopied::getUserId, Collectors.toSet())));
    }

    @Override
    public Boolean exists(UserDto user, String taskId) {
        return count(Wrappers.<FlowTaskCopied>lambdaQuery()
                .eq(FlowTaskCopied::getFlowTaskId, taskId)
                .eq(FlowTaskCopied::getUserId, user.getId())) > 0;
    }
}
