package cn.bctools.auth.service.impl;

import cn.bctools.auth.entity.Index;
import cn.bctools.auth.mapper.IndexMapper;
import cn.bctools.auth.service.IndexService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhuxiaokang
 */
@Service
public class IndexServiceImpl extends ServiceImpl<IndexMapper, Index> implements IndexService {

}
