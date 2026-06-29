package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.Dept;
import cn.bctools.auth.entity.OauthOther;
import cn.bctools.auth.login.auth.OtherLoginHandler;
import cn.bctools.auth.login.dto.SyncUserDto;
import cn.bctools.auth.mapper.OauthOtherMapper;
import cn.bctools.auth.service.*;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xh
 */
@Slf4j
@Service
public class OauthOtherServiceImpl extends ServiceImpl<OauthOtherMapper, OauthOther> implements OauthOtherService {
    OauthOtherService oauthOtherService;
    DeptService deptService;
    UserService userService;
    OtherLoginHandler otherLoginHandler;

    @PostConstruct
    public void sync() {
        //获取有哪些类型
        CronUtil.schedule("0 0 */2 * * *", (Task) () -> {
            try {
                SystemThreadLocal.clear();
                List<OauthOther> collect = oauthOtherService.list().stream()
                        .filter(e -> ObjectNull.isNotNull(e.getDeptUrl()))
                        .collect(Collectors.toList());
                if (ObjectNull.isNotNull(collect)) {
                    log.info("开始自动同步获取用户组织结构");
                    for (OauthOther oauthOther : collect) {
                        String tenantId = oauthOther.getTenantId();
                        TenantContextHolder.setTenantId(tenantId);
                        SyncUserDto deptAll = otherLoginHandler.getDeptAll(oauthOther.getType());
                        // 同步部门
                        deptService.pull(UserCurrentUtils.getCurrentUser(), (List<Dept>) deptAll.getList());
                        // 同步用户
                        userService.pull(deptAll);
                    }
                }
            } catch (Exception e) {
                log.error("同步失败", e);
            }
        });

    }
}
