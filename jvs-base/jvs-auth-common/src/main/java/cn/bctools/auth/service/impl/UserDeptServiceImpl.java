package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.UserDept;
import cn.bctools.auth.mapper.UserDeptMapper;
import cn.bctools.auth.service.UserDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 角色分组服务
 *
 * @author
 */
@Service
@AllArgsConstructor
@Slf4j
public class UserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDept> implements UserDeptService {

}
