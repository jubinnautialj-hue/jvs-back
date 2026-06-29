package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.LoginRules;
import cn.bctools.auth.mapper.LoginRulesMapper;
import cn.bctools.auth.service.LoginRulesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 */
@Service
public class LoginRulesServiceImpl extends ServiceImpl<LoginRulesMapper, LoginRules> implements LoginRulesService {

}
