package cn.bctools.design.project.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.data.mapper.DataModelMapper;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppLog;
import cn.bctools.design.project.entity.enums.AppLogTypeEnum;
import cn.bctools.design.project.mapper.JvsAppLogMapper;
import cn.bctools.design.project.mapper.JvsAppMapper;
import cn.bctools.design.project.service.JvsAppLogService;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Service
public class JvsAppLogServiceImpl extends ServiceImpl<JvsAppLogMapper, JvsAppLog> implements JvsAppLogService {

    @Resource
    DataModelMapper dataModelMapper;
    @Resource
    JvsAppMapper jvsAppMapper;

    @Value("${app.log:false}")
    Boolean log;

    @Override
    public void savelog(String modelId, AppLogTypeEnum type, Map<String, Object> beforeJson, Map<String, Object> afterJson) {
        //如果没有开启，则直接返回
        if (!log) {
            return;
        }
        DataModelPo dataModelPo = dataModelMapper.selectById(modelId);
        JvsApp o = jvsAppMapper.selectById(dataModelPo.getAppId());
        try {
            UserDto currentUser = UserCurrentUtils.getCurrentUser();
            List<String> roleIds = currentUser.getRoleIds();
            if(ObjectNull.isNotNull(roleIds)) {
                roleIds = AuthorityManagementUtils.getRolesByIds(roleIds).stream().map(e -> e.getRoleName()).collect(Collectors.toList());
            }
            save(new JvsAppLog().setJvsAppId(o.getId()).setJvsAppName(o.getName())
                    .setAccount(currentUser.getAccountName())
                    .setUserName(currentUser.getRealName())
                    .setDeptId(currentUser.getDeptId())
                    .setDeptName(currentUser.getDeptName())
                    .setType(type)
                    .setRoles(roleIds)
                    .setButtonName(DynamicDataUtils.getOperator())
                    .setUserId(currentUser.getId())
                    .setDesignId(DynamicDataUtils.getDesignId())
                    .setModeId(modelId)
                    .setBeforeJson(beforeJson)
                    .setAfterJson(afterJson)
                    .setModelName(dataModelPo.getName())
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
