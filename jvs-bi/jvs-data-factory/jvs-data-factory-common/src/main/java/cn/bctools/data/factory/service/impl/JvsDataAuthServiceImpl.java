package cn.bctools.data.factory.service.impl;

import cn.bctools.data.factory.entity.JvsDataAuth;
import cn.bctools.data.factory.mapper.JvsDataAuthMapper;
import cn.bctools.data.factory.service.JvsDataAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据权限
 *
 * @author zqs
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsDataAuthServiceImpl extends ServiceImpl<JvsDataAuthMapper, JvsDataAuth> implements JvsDataAuthService {
}
