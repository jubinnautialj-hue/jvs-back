package cn.bctools.design.rule.service.impl;

import cn.bctools.design.rule.entity.RunDescribePo;
import cn.bctools.design.rule.mapper.RuleDescribeDao;
import cn.bctools.design.rule.service.RuleDescribeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author bctools.cn
 */
@AllArgsConstructor
@Slf4j
@Service
public class RuleDescribeImplServiceImpl extends ServiceImpl<RuleDescribeDao, RunDescribePo> implements RuleDescribeService {

    @Override
    public void deleteRuleDescribe(String ruleKey, String appId) {
        this.remove(Wrappers.query(new RunDescribePo().setReqRunKey(ruleKey).setJvsAppId(appId)));
    }
}

