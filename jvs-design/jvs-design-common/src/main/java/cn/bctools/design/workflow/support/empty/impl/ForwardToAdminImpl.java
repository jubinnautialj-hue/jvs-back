package cn.bctools.design.workflow.support.empty.impl;

import cn.bctools.auth.api.api.AuthTenantServiceApi;
import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SysTenantDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodePropertiesUserEmptyEnum;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.empty.ApproverEmptyInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author zhuxiaokang
 * 审批人为空时——转发到超管
 */
@Slf4j
@Component
@AllArgsConstructor
public class ForwardToAdminImpl implements ApproverEmptyInterface {

    private final AuthTenantServiceApi authTenantServiceApi;
    private final AuthUserServiceApi authUserServiceApi;


    @Override
    public String getType() {
        return NodePropertiesUserEmptyEnum.TO_ADMIN.getValue();
    }

    @Override
    public List<UserDto> execute(Node node, RuntimeData runtimeData) {
        // 获取超管id
        SysTenantDto tenantPo = authTenantServiceApi.getById(runtimeData.getUser().getTenantId()).getData();
        String adminUserId = tenantPo.getAdminUserId();
        // 获取超管信息
        UserDto userDto = authUserServiceApi.getById(adminUserId).getData();
        return Collections.singletonList(userDto);
    }
}
