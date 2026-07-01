package cn.bctools.design.workflow.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyReqDto;
import cn.bctools.design.workflow.dto.proxy.PageFlowTaskProxyResDto;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import cn.bctools.design.workflow.entity.enums.ProxyTypeEnum;
import cn.bctools.design.workflow.enums.FlowTaskProxyStatusEnum;
import cn.bctools.design.workflow.mapper.FlowTaskProxyMapper;
import cn.bctools.design.workflow.service.FlowTaskProxyService;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流代理 服务实现类
 */
@Service
@AllArgsConstructor
public class FlowTaskProxyServiceImpl extends ServiceImpl<FlowTaskProxyMapper, FlowTaskProxy> implements FlowTaskProxyService {

    AuthUserServiceApi userServiceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserDto userDto, FlowTaskProxy flowTaskProxy) {
        // 校验新增代理入参
        // 被代理人与代理人不能相同
        if (flowTaskProxy.getUserId().equals(flowTaskProxy.getProxyUserId())) {
            throw new BusinessException("被代理人与代理人不能相同");
        }
        // 代理失效时间不能早于当前时间
        if (ProxyTypeEnum.DEFAULT.equals(flowTaskProxy.getProxyType()) && LocalDateTime.now().isAfter(flowTaskProxy.getEndTime())) {
            throw new BusinessException("代理结束时间不能早于当前时间");
        }

        LambdaQueryWrapper<FlowTaskProxy> queryWrapper = Wrappers.<FlowTaskProxy>lambdaQuery()
                .eq(FlowTaskProxy::getRevokeFlag, Boolean.FALSE);
        // 普通代理，查询是否有冲突代理配置
        if (ProxyTypeEnum.DEFAULT.equals(flowTaskProxy.getProxyType())) {
            queryWrapper.and(wrapper -> wrapper.and(wrapper1 -> wrapper1.ge(FlowTaskProxy::getBeginTime, flowTaskProxy.getBeginTime()).le(FlowTaskProxy::getBeginTime, flowTaskProxy.getEndTime()))
                    .or(wrapper2 -> wrapper2.le(FlowTaskProxy::getBeginTime, flowTaskProxy.getBeginTime()).ge(FlowTaskProxy::getEndTime, flowTaskProxy.getEndTime()))
                    .or(wrapper3 -> wrapper3.ge(FlowTaskProxy::getEndTime, flowTaskProxy.getBeginTime()).le(FlowTaskProxy::getEndTime, flowTaskProxy.getEndTime()))
                    .or(wrapper4 -> wrapper4.eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEPART))
            );
            // 被代理人的代理配置不允许有代理时间重叠
            boolean timeOverlap = baseMapper.selectCount(queryWrapper.clone().eq(FlowTaskProxy::getUserId, flowTaskProxy.getUserId())) >= 1;
            if (timeOverlap) {
                throw new BusinessException("存在与当前被代理人时间重叠的代理配置");
            }
        }

        // 代理人的代理配置不允许有代理时间重叠
        boolean proxyTimeOverlap = baseMapper.selectCount(queryWrapper.eq(FlowTaskProxy::getUserId, flowTaskProxy.getProxyUserId())) >= 1;
        if (proxyTimeOverlap) {
            if (ObjectNull.isNotNull(flowTaskProxy.getBeginTime())) {
                throw new BusinessException("代理人在代理时间内已将任务代理给他人请选择其他代理人", flowTaskProxy.getProxyUserName(), LocalDateTimeUtil.format(flowTaskProxy.getBeginTime(), DatePattern.NORM_DATETIME_PATTERN), LocalDateTimeUtil.format(flowTaskProxy.getEndTime(), DatePattern.NORM_DATETIME_PATTERN));
            } else {
                throw new BusinessException("代理人已将任务代理给他人请选择其他代理人", flowTaskProxy.getProxyUserName());
            }
        }

        if (ProxyTypeEnum.DEPART.equals(flowTaskProxy.getProxyType())) {
            List<FlowTaskProxy> departProxyList = baseMapper.selectList(Wrappers.<FlowTaskProxy>lambdaQuery()
                    .eq(FlowTaskProxy::getUserId, flowTaskProxy.getUserId())
                    .eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEPART));
            if (ObjectNull.isNull(departProxyList)) {
                baseMapper.insert(flowTaskProxy);
            } else {
                departProxyList.forEach(proxy -> proxy.setRevokeFlag(Boolean.FALSE).setProxyUserId(flowTaskProxy.getProxyUserId()).setProxyUserName(flowTaskProxy.getProxyUserName()));
                updateBatchById(departProxyList);
            }
        } else {
            // 保存
            baseMapper.insert(flowTaskProxy);
        }


    }

    @Override
    public Page<PageFlowTaskProxyResDto> page(UserDto userDto, Page<FlowTaskProxy> page, PageFlowTaskProxyReqDto dto) {
        Page<PageFlowTaskProxyResDto> pageDto = new Page<>(page.getCurrent(), page.getSize());
        LocalDateTime now = LocalDateTime.now();
        // 非管理员，只能查询被代理人是自己的数据
        LambdaQueryWrapper<FlowTaskProxy> queryWrapper = Wrappers.<FlowTaskProxy>lambdaQuery()
                .eq(Boolean.FALSE.equals(userDto.getAdminFlag()), FlowTaskProxy::getUserId, userDto.getId());
        // 用户名（被代理用户名 或 代理用户名）
        if (StringUtils.isNotBlank(dto.getUserName())) {
            queryWrapper.and(wrapper -> wrapper.like(FlowTaskProxy::getUserName, dto.getUserName())
                    .or().like(FlowTaskProxy::getProxyUserName, dto.getUserName()));
        }
        // 状态条件
        if (ObjectNull.isNotNull(dto.getStatus())) {
            switch (dto.getStatus()) {
                case PENDING:
                    // 待生效
                    queryWrapper.eq(FlowTaskProxy::getRevokeFlag, Boolean.FALSE).gt(FlowTaskProxy::getBeginTime, now);
                    break;
                case EFFECTIVE:
                    // 代理中
                    effectiveProxyWrapper(queryWrapper);
                    break;
                case EXPIRED:
                    // 已过期
                    queryWrapper.eq(FlowTaskProxy::getRevokeFlag, Boolean.FALSE).lt(FlowTaskProxy::getEndTime, now);
                    break;
                case REVOKED:
                    // 已撤销
                    queryWrapper.eq(FlowTaskProxy::getRevokeFlag, Boolean.TRUE);
                    break;
                default:
                    // 其它
                    break;
            }
        }
        queryWrapper.orderByDesc(FlowTaskProxy::getCreateTime);
        page(page, queryWrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return pageDto;
        }
        List<String> userIds = new ArrayList<>();
        // 修改响应状态
        List<PageFlowTaskProxyResDto> pageDtos = new ArrayList<>();
        for (FlowTaskProxy proxy : page.getRecords()) {
            PageFlowTaskProxyResDto resDto = BeanCopyUtil.copy(proxy, PageFlowTaskProxyResDto.class);
            //获取用户头像信息
            userIds.add(resDto.getUserId());
            // 已撤销
            if (resDto.getRevokeFlag()) {
                resDto.setStatus(FlowTaskProxyStatusEnum.REVOKED);
                pageDtos.add(resDto);
                continue;
            }
            // 未撤销的代理配置
            // 根据时间判断状态
            if (ProxyTypeEnum.DEFAULT.equals(resDto.getProxyType())) {
                // 待生效
                if (now.isBefore(resDto.getBeginTime())) {
                    resDto.setStatus(FlowTaskProxyStatusEnum.PENDING);
                    pageDtos.add(resDto);
                    continue;
                }
                // 已过期
                if (now.isAfter(resDto.getEndTime())) {
                    resDto.setStatus(FlowTaskProxyStatusEnum.EXPIRED);
                    pageDtos.add(resDto);
                    continue;
                }
            }
            // 以上条件都不满足，则状态为代理中
            resDto.setStatus(FlowTaskProxyStatusEnum.EFFECTIVE);
            pageDtos.add(resDto);
        }
        ArrayList<String> field = new ArrayList<>();
        field.add("id");
        field.add("headImg");
        Map<String, String> idsMap = userServiceApi.getBasicInfoById(userIds, field).getData().stream().collect(Collectors.toMap(UserDto::getId, UserDto::getHeadImg));
        pageDtos.forEach(e -> {
            e.setProxyUserHeadImg(idsMap.get(e.getUserId()));
        });

        pageDto.setRecords(pageDtos);
        pageDto.setTotal(page.getTotal());
        return pageDto;
    }

    /**
     * 已生效查询条件
     *
     * @param wrappers 查询条件
     */
    private void effectiveProxyWrapper(LambdaQueryWrapper<FlowTaskProxy> wrappers) {
        LocalDateTime now = LocalDateTime.now();
        wrappers.eq(FlowTaskProxy::getRevokeFlag, Boolean.FALSE)
                .and(w ->
                        // 离职代理，立即生效
                        w.eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEPART)
                                // 默认代理类型，根据时间判断是否生效
                                .or(o -> o.eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEFAULT)
                                        .le(FlowTaskProxy::getBeginTime, now)
                                        .ge(FlowTaskProxy::getEndTime, now))
                );
    }

    @Override
    public List<FlowTaskProxy> getEffectiveProxy() {
        LocalDateTime now = LocalDateTime.now();
        // 查询当天所有代理
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<FlowTaskProxy> wrappers = Wrappers.lambdaQuery();
        wrappers.eq(FlowTaskProxy::getRevokeFlag, Boolean.FALSE)
                .and(w ->
                        // 离职代理，立即生效
                        w.eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEPART)
                                // 默认代理类型，根据时间判断是否生效
                                .or(o -> o.eq(FlowTaskProxy::getProxyType, ProxyTypeEnum.DEFAULT)
                                        .le(FlowTaskProxy::getBeginTime, LocalDateTime.of(today, LocalTime.of(23, 59, 59)))
                                        .ge(FlowTaskProxy::getEndTime, LocalDateTime.of(today, LocalTime.of(0, 0, 0)))));
        List<FlowTaskProxy> todayFlowTaskProxyList = list(wrappers);
        // 筛选出生效的代理
        return todayFlowTaskProxyList.stream()
                .filter(proxy ->
                        ProxyTypeEnum.DEPART.equals(proxy.getProxyType())
                                ||
                                (proxy.getBeginTime().isBefore(now) && now.isBefore(proxy.getEndTime())))
                .collect(Collectors.toList());
    }


    @Override
    public FlowTaskProxy getEffectiveProxyByUserId(String userId) {
        LambdaQueryWrapper<FlowTaskProxy> wrappers = Wrappers.lambdaQuery();
        wrappers.eq(FlowTaskProxy::getUserId, userId);
        effectiveProxyWrapper(wrappers);
        return getOne(wrappers);
    }

    @Override
    public List<FlowTaskProxy> getEffectiveProxyByUserIds(Collection<String> userIds) {
        LambdaQueryWrapper<FlowTaskProxy> wrappers = Wrappers.lambdaQuery();
        wrappers.in(FlowTaskProxy::getUserId, userIds);
        effectiveProxyWrapper(wrappers);
        return list(wrappers);
    }
}
